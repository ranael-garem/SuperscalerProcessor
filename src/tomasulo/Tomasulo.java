package tomasulo;
import java.util.ArrayList;

import Binary.binary;


import memoryHierarchy.Cache;
import memoryHierarchy.MemoryHierarchy;

import functionalUnits.AddFunctinalUnit;
import functionalUnits.JALRFunctionalUnit;
import functionalUnits.LoadFunctionalUnit;
import functionalUnits.MultiplyFunctionalUnit;

public class Tomasulo {
	public MemoryHierarchy mem_hierarchy;
	
	ReservationStation [] reservation_stations;
	public RegisterFile register_file;
	RegisterStatusTable register_statuses;
	ReorderBufferTable reorder_buffer;
	InstructionBuffer instruction_buffer;
	public int way; //Number of instructions issued simultaneously
	

	int cycles; // Number of cycles spanned
	int instructions_completed; //Number of instructions completed
	int PC; // PC register pointing to the instruction to be fetched
	int PC_END; //Address of PC where program ends
	int instructions_executed; // Number of instructions executed
	ArrayList<Integer> RS_indices; // Indices of RSs in the order they were issued 
	
	int branches;
	int mispredictions;
	
	boolean jump_stall;
	
	boolean low_byte_set; // for fetch
	String low_byte; // for fetch
	
	Instruction jump_instruction; // save the jump instruction to be able to get it after stalling

	public Tomasulo (int no_reorder_buffer_entries,
			int instruction_buffer_entries, String[] functional_unit_info, int PC_ORG, int PC_END, int way) {

		this.register_file = new RegisterFile();
		this.register_statuses = new RegisterStatusTable();
		this.reorder_buffer = new ReorderBufferTable(no_reorder_buffer_entries);
		this.instruction_buffer = new InstructionBuffer(instruction_buffer_entries);
		this.way = way;
		
		int no_reservation_stations = 0;
		//User enters array of Strings with specification of each functional unit
		//e.g: Add,3,6  (3 is the number of ADDFus and 6 is number of cycles, the FU takes to execute
		for(int i = 0; i < functional_unit_info.length; i++) {
			String [] functional_unit_data = functional_unit_info[i].split(",");
			int FU_count = Integer.parseInt(functional_unit_data[1]);
			
			no_reservation_stations += FU_count; // Calculate number of reservation stations

		}
		this.reservation_stations = new ReservationStation[no_reservation_stations];
		this.RS_indices = new ArrayList<>();
		int counter = 0;
		// Initialize the reservation stations table with each entry's name that refers to the functional unit
		for(int i = 0; i < functional_unit_info.length; i++) {
			String [] functional_unit_data = functional_unit_info[i].split(",");
			String type = functional_unit_data[0];
			int FU_count = Integer.parseInt(functional_unit_data[1]);
			int cycles = Integer.parseInt(functional_unit_data[2]);

			for(int j = 0; j < FU_count; j++ ) {
				this.reservation_stations[counter] = new ReservationStation(type+(j+1), cycles);
				
				if(type.toLowerCase().equals("add") ) {
					this.reservation_stations[counter].FU = new AddFunctinalUnit(cycles); //Initialize AndFU with number of cycles
				}
				else if(type.toLowerCase().equals("mul")) {
					this.reservation_stations[counter].FU = new MultiplyFunctionalUnit(cycles);
				}
				else if(type.toLowerCase().equals("lw")) {
					this.reservation_stations[counter].FU = new LoadFunctionalUnit(cycles);
				}
				else if(type.toLowerCase().equals("jalr")) {
					this.reservation_stations[counter].FU = new JALRFunctionalUnit(cycles);
				}
				counter++;
			}
		}
		
		this.PC = PC_ORG;
		this.PC_END = PC_END + 2;
		this.low_byte = null;

	}
	
	public void fetch() {

		if(jump_stall) {
			Instruction I = this.jump_instruction;
			if(jumpFetchCheck(I)) // Change PC to Jump address if instruction is Jump
				return;
			if(returnFetchCheck(I)) // Change PC to Jump address if instruction is Return
				return;
			if(jalrFetchCheck(I)) // Change PC to Jump address if instruction is JALR
				return;
			if(branchFetchCheck(I))  // Change PC to branch address if instruction is BREQ and offset is negative(Branch Taken)
				return;	
		}
		
		if(PC != PC_END && !jump_stall && !this.instruction_buffer.isFull()) {
			String instruction = getInstructionFromMem(PC);
			if(instruction == null) {
				return;
			}
			Instruction I = new Instruction(instruction);
			this.instruction_buffer.addToBuffer(I);

			if(jumpFetchCheck(I)) // Change PC to Jump address if instruction is Jump
				return;
			if(returnFetchCheck(I)) // Change PC to Jump address if instruction is Return
				return;
			if(jalrFetchCheck(I)) // Change PC to Jump address if instruction is JALR
				return;
			if(branchFetchCheck(I))  // Change PC to branch address if instruction is BREQ and offset is negative(Branch Taken)
				return;
			PC +=2;

		}
		else return; // Program ended, No More instructions to fetch
	}
	
	public String getInstructionFromMem(int PC) {
		String address = Integer.toBinaryString(PC);
		String high_byte = "";
		while(address.length() < 16)
			address = "0" + address;
		if(!low_byte_set) {
				low_byte = mem_hierarchy.fetchInstruction(address);
				if(low_byte != null)
					low_byte_set = true;
				
			return null;
		}
		else {
			address = Integer.toBinaryString(PC + 1);
			while(address.length() < 16)
				address = "0" + address;

			high_byte = mem_hierarchy.fetchInstruction(address);	
			if(high_byte == null)
				return null;
		
		}
		
		String result = high_byte + low_byte;
		low_byte = null;
		low_byte_set = false;
		return result;
	}
	
	private boolean branchFetchCheck(Instruction I) {
		if(I.type.toLowerCase().equals("branch") &&  (I.imm < 0 )) {
			I.PC_value = PC+2;
			this.PC = PC + 2 +I.imm;
			return true;
		}
		I.PC_value = PC + 2 + I.imm;
		return false; //Instruction is not branch or branch predicted to be not taken
	}

	private boolean jalrFetchCheck(Instruction I) {
		if(I.type.toLowerCase().equals("jalr")) {
			I.PC_value = PC + 2;
			this.jump_instruction = I;
			int b = this.register_statuses.ROBTag[I.rs];
			if(b != -1) {
				if(this.reorder_buffer.entries[b].ready) {
					PC = this.reorder_buffer.entries[b].value;
					this.jump_stall = false;
				}
				else { this.jump_stall = true;}
			}
			else {
				PC = binary.convertToDecimal(this.register_file.registers[I.rs]);
				this.jump_stall = false;
			}

			return true;
		}
		return false; //Instruction is not JALR
	}

	public boolean jumpFetchCheck(Instruction I) {
		if(I.type.toLowerCase().equals("jump")) {
			this.jump_instruction = I;
			int b = this.register_statuses.ROBTag[I.rs];
			if(b != -1) {
				if(this.reorder_buffer.entries[b].ready) {
					PC = PC + 2 + this.reorder_buffer.entries[b].value + I.imm;
					this.jump_stall = false;
				}
				else { this.jump_stall = true;}
			}
			else {

				PC = PC + 2 + binary.convertToDecimal(this.register_file.registers[I.rs]) + I.imm;
				this.jump_stall = false;
			}
			return true;
		}
		return false; //Instruction is not Jump
	}
	
	public boolean returnFetchCheck(Instruction I) {
		if(I.type.toLowerCase().equals("return")) {
			int b = this.register_statuses.ROBTag[I.rs];
			
			if(b != -1) {
				if(this.reorder_buffer.entries[b].ready) {
					PC = this.reorder_buffer.entries[b].value;
					this.jump_stall = false;
				}
				else { this.jump_stall = true;}
			}
			else {
				PC = binary.convertToDecimal(this.register_file.registers[I.rs]);
				this.jump_stall = false;
			}

			return true;
		}
		return false; // Intruction is not return
	}

	public void issue() {
		
		for(int j = 0; j < this.way; j++) {
			if(this.instruction_buffer.getInstruction() == null) {
				return; //No instruction available in the Instruction Buffer
			}
			Instruction i = this.instruction_buffer.getInstruction();
			if(i.type.toLowerCase().equals("jump") || i.type.toLowerCase().equals("return")) {
				if(this.reorder_buffer.entries[reorder_buffer.tail] == null) {	//Check for available ROB only
					ReorderBufferEntry reorder_entry = new ReorderBufferEntry(i.type, -1, true);
					this.reorder_buffer.addToBuffer(reorder_entry);
					this.instruction_buffer.removeFromBuffer();
				}else {
					return; // No Available ROB so CANT issue
				}
			}
			else {
				// Check for available RS and ROB entry
				if((this.checkAvailableRS(i) == -1) || this.reorder_buffer.entries[reorder_buffer.tail] != null) {
					return; // No RS or ROB slot available for Instruction,	Then Stall			
				}
				else {
					this.instruction_buffer.removeFromBuffer();
					ReservationStation RS = this.reservation_stations[this.checkAvailableRS(i)];
					RS_indices.add(this.checkAvailableRS(i)); // Add index of RS in ArrayList
					int b = reorder_buffer.tail; 
					RS.busy = true;
					RS.op = i.type;
					if(i.type.toLowerCase().equals("jalr")) {
						RS.dest = b;
						RS.Vk = i.PC_value; // The value of PC+2 which is saved when fetching
						ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
						this.reorder_buffer.addToBuffer(reorder_entry);
						this.register_statuses.ROBTag[i.rd] = b;
					}
					else {
						checkRegisterReadyRs(i, RS);
						if(i.type.toLowerCase().equals("load")) {
							RS.A = i.imm;
							this.register_statuses.ROBTag[i.rd] = b;
							RS.dest = b;
							ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
							this.reorder_buffer.addToBuffer(reorder_entry);
						}
						else if(i.type.toLowerCase().equals("store")) {
							checkRegisterReadyRt(i, RS);
							RS.dest = b;
							RS.A = i.imm;
							ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, -1 , false);
							this.reorder_buffer.addToBuffer(reorder_entry);

						} else if(i.type.toLowerCase().equals("branch")) {
							checkRegisterReadyRt(i, RS);
							RS.dest = b;
							RS.A = i.imm;
							ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, -1, false);
							reorder_entry.PC_value = i.PC_value;
							this.reorder_buffer.addToBuffer(reorder_entry);
						}
						else { //Arithmetic Operations 
							checkRegisterReadyRt(i, RS);
							RS.dest = b;
							ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
							this.reorder_buffer.addToBuffer(reorder_entry);
							this.register_statuses.ROBTag[i.rd] = b;
						}
						if(RS.Qj == -1 && RS.Qk == -1)
							RS.start_execute = true;
						}
					}

			}
			
		}

	}
	
	public void checkRegisterReadyRs(Instruction i, ReservationStation r) {
		if(this.register_statuses.ROBTag[i.rs] != -1) {
			int ROBindex = this.register_statuses.ROBTag[i.rs];
			if(this.reorder_buffer.entries[ROBindex].ready) {
				r.Vj = this.reorder_buffer.entries[ROBindex].value;
				r.Qj = -1;
			} else {
				r.Qj = ROBindex;
			} 
		}
		else {
			r.Vj = binary.convertToDecimal(this.register_file.registers[i.rs]);
			r.Qj = -1;
		}
	}
	
	public void checkRegisterReadyRt(Instruction i, ReservationStation r) {
		if(i.type.toLowerCase().equals("addi")) {
			r.Vk = i.imm;
			r.Qk = -1;
			return;
		}
		if(this.register_statuses.ROBTag[i.rt] != -1) {
			int ROBindex = this.register_statuses.ROBTag[i.rt];
			if(this.reorder_buffer.entries[ROBindex].ready) {
				r.Vk = this.reorder_buffer.entries[ROBindex].value;
				r.Qk = -1;
			} else {
				r.Qk = ROBindex;
			} 
		}
		else {
			r.Vk = binary.convertToDecimal(this.register_file.registers[i.rt]);
			r.Qk = -1;
		}
	}
	
	public  int checkAvailableRS(Instruction i) {
		String type = "";
		if(i.type.equals("add") || i.type.equals("nand") || i.type.equals("subtract") || i.type.equals("branch")  || i.type.equals("addi")) {
			type = "add";
		}
		else if(i.type.equals("multiply")) {
			type = "mul";
		}
		else if(i.type.equals("load") || i.type.equals("store")) {
			type = "lw";
		}
		else if(i.type.equals("jalr")) {
			type = "jalr";
		}
		for(int j = 0; j < this.reservation_stations.length; j++) {
			if(this.reservation_stations[j].name.toLowerCase().contains(type) && !this.reservation_stations[j].busy) {
				System.out.println(j);
				return j;
			}
		}
		return -1;
	}
	
	public void execute() {

		for(int i = 0; i < this.reservation_stations.length; i++) {
			ReservationStation RS = this.reservation_stations[i];
			if(RS.busy) {
				if(RS.op.toLowerCase().equals("load")) {
					if(RS.Qj == -1) {
						String address = Integer.toBinaryString(RS.A);
						while(address.length() < 16)
							address = "0" + address;
						String address1 = Integer.toBinaryString(RS.A + 1);
						while (address1.length() < 16)
							address1 = "0" + address1;
						
						if(RS.lowByteSet)
							address = address1;
						
						if(RS.calculated_address && storeCheck(RS.A)) {
							if(RS.cache_level == this.mem_hierarchy.caches.length) { // memory
								if(!this.mem_hierarchy.memory.being_accessed || RS.accessed_cache) {
									RS.accessed_cache = true;
									if(this.mem_hierarchy.cacheCyclesLeft(RS.cache_level, address) == 0) {
										RS.cache_level--;
										RS.accessed_cache = false;
									}
								}
							}
							else if(!this.mem_hierarchy.caches[RS.cache_level].being_accessed || RS.accessed_cache) {
								RS.accessed_cache = true;
								if(RS.cache_level == 1) {
									String Byte = this.mem_hierarchy.loadValue(address);
									if(Byte != null) {
										RS.accessed_cache = false;
										if(!RS.lowByteSet) {
											RS.lowByte = Byte;
											RS.lowByteSet = true;
											RS.cache_level = this.mem_hierarchy.loadCacheLevel(address1);
										}else {
											String result = Byte + RS.lowByte;
											RS.result = binary.convertToDecimal(result);
											RS.calculated_address = false;
											instructions_executed++;
											RS.lowByte = null;
											RS.lowByteSet = false;
											RS.exec_cycles_left = 0;
										}
									}
								}
								else if(this.mem_hierarchy.cacheCyclesLeft(RS.cache_level, address) == 0) {
									RS.cache_level--;
									RS.accessed_cache = false;
								}
							}
						}
						else {
							RS.A = RS.FU.execute("calculate_addr", RS.Vj, RS.A);
							RS.calculated_address = true;
							RS.cache_level = this.mem_hierarchy.loadCacheLevel(address);
						}
					}
				}
				else if(RS.op.toLowerCase().equals("store")) {
					if(RS.Qj == -1 && !RS.start_store) {
						this.reorder_buffer.entries[RS.dest].Dest = RS.Vj + RS.A;
						String address = Integer.toBinaryString(RS.Vj + RS.A);
						while (address.length() < 16)
							address = "0" + address;
						RS.cache_level = mem_hierarchy.loadCacheLevel(address);
						RS.start_store = true;
						instructions_executed++;
					}
				}
				
				else if(RS.op.toLowerCase().equals("jalr")) {
					RS.result = RS.Vk;
					RS.exec_cycles_left = 0;
					instructions_executed++;
				}
				else { //Arithmetic Operations and Branch
				if(RS.start_execute) {
					if(RS.exec_cycles_left > 0)
						RS.exec_cycles_left--;
					if(RS.exec_cycles_left == 0) {
						RS.result = RS.FU.execute(RS.op, RS.Vj, RS.Vk);
						instructions_executed++;
						if(RS.op.toLowerCase().equals("branch")) {
							if(RS.result == 1 && (RS.A < 0)) // 2 operands are equal, and Branch was taken
								RS.result = 1; // Correct Prediction
							else if(RS.result == 1 && (RS.A >= 0)) { // 2 operands are equal, and Branch was not taken
								RS.result = 0; // mispredictions
								this.mispredictions++;
							}
							else if(RS.result == 0 && (RS.A < 0)) { // 2 operands are not equal and Branch was taken
								RS.result = 0;
								this.mispredictions++;
							}
							else if(RS.result == 0 && (RS.A >= 0))  // 2 operands are not equal and branch was not taken
								RS.result = 1; // Correct Prediction
						}
					}
				}
				else if (RS.Qj == -1 && RS.Qk == -1) {
					RS.start_execute = true;
				}
			}
		}
	}
	}
	
	public boolean storeCheck(int Address) {
		//Checks that all stores have a different memory address, then the Load mem address
		for(int i = 0; i < this.reorder_buffer.entries.length; i++) {
			ReorderBufferEntry b = this.reorder_buffer.entries[i];
			if(b!= null && b.type.toLowerCase().equals("store") && b.Dest == Address)
				return false;
		}
		return true;
	}
	
	public void write() {
		int writes = 0; // Number of instruction that can be written
		for(int i = 0; i < this.RS_indices.size(); i++ ) {
			ReservationStation RS = this.reservation_stations[RS_indices.get(i)];
			if(RS.op.toLowerCase().equals("store")) {
				if(RS.Qk == -1) {
					if(RS.start_store) {
						String address = Integer.toBinaryString(RS.A);
						while(address.length() < 16)
							address = "0" + address;

						if(RS.write_low_byte) {
							storeLowByte(RS.Vk, RS.A);
							RS.write_low_byte = false;
							RS.write_high_byte = true;
						}else if(RS.write_high_byte) {
							storeHighByte(RS.Vk, RS.A + 1);
							int b = RS.dest;
							this.reorder_buffer.entries[b].value = RS.Vk;
							this.reorder_buffer.entries[b].ready = true;
							
							RS.start_store = false;
							RS.flushRS();
							RS.write_high_byte = false;	
											
							RS_indices.remove(i);
							writes++;
							if(writes == this.way)
								return;
							else
								i--;
						}
						else if(RS.cache_level == this.mem_hierarchy.caches.length) { // memory
							if(!this.mem_hierarchy.memory.being_accessed || RS.accessed_cache) {
								RS.accessed_cache = true;
								if(this.mem_hierarchy.cacheCyclesLeft(RS.cache_level, address) == 0) {
									RS.cache_level--;
									RS.accessed_cache = false;
								}
							}
						}
						else if(!this.mem_hierarchy.caches[RS.cache_level].being_accessed || RS.accessed_cache) {
							RS.accessed_cache = true;
							if(RS.cache_level == 1 && this.mem_hierarchy.cacheCyclesLeft(RS.cache_level, address) == 0) {
								RS.write_low_byte = true;
							}
							else if(this.mem_hierarchy.cacheCyclesLeft(RS.cache_level, address) == 0) {
								RS.cache_level--;
								RS.accessed_cache = false;
							}
						}
					}
				}
			}
			// All instructions but Store
			else if(RS.exec_cycles_left == 0) {
				int b = RS.dest;
				
				this.reorder_buffer.entries[b].ready = true;
				this.reorder_buffer.entries[b].value = RS.result;
				
				for (int j = 0; j < this.reservation_stations.length; j++) {
					ReservationStation RS2 = this.reservation_stations[j];
					if(RS2.Qj == b) {
						RS2.Vj = RS.result;
						RS2.Qj = -1;
					}
					if(RS2.Qk == b) {
						RS2.Vk = RS.result;
						RS2.Qk = -1;
					}
				}
				RS.flushRS();
				RS_indices.remove(i);
				writes++;
				if(writes == this.way)
					return;
				else
					i--;
			}
		}
	}
	
	private void storeLowByte(int vk, int A) {
		String address = Integer.toBinaryString(A);
		while (address.length() < 16)
			address = "0" + address;
		
		String value = binary.convertToBinary(vk);
		this.mem_hierarchy.write(address, value.substring(8));
	}
	
	private void storeHighByte(int vk, int A) {
		String address = Integer.toBinaryString(A);
		while (address.length() < 16)
			address = "0" + address;
		
		String value = binary.convertToBinary(vk);
		this.mem_hierarchy.write(address, value.substring(0,8));
	}

	public void commit() {
		for(int i = 0; i < this.way; i++) {
			ReorderBufferEntry ROB_entry = this.reorder_buffer.getROB();
			if(ROB_entry == null)
				return;
			
			int d = ROB_entry.Dest; //Reg or Mem Address if store
			if(ROB_entry.ready) {
				if(ROB_entry.type.toLowerCase().equals("store")) {
					this.reorder_buffer.removeFromBuffer();
					instructions_completed++;
				}
				else if(ROB_entry.type.toLowerCase().equals("jump") || ROB_entry.type.toLowerCase().equals("return")) {
					this.reorder_buffer.removeFromBuffer();
					instructions_completed++;
				}
				else if(ROB_entry.type.toLowerCase().equals("branch")) {
					this.branches++;
					instructions_completed++;
					if(ROB_entry.value == 1) { // Branch was predicted correctly
						this.reorder_buffer.removeFromBuffer();

					} else {
						this.PC = ROB_entry.PC_value;
						this.reorder_buffer.flush();
						this.register_statuses.flush();
						flushRss();
						this.instruction_buffer.flush();
						this.low_byte = null;
						this.low_byte_set = false;
						return;
					}
				}
				else {
					this.register_file.registers[d] = binary.convertToBinary(ROB_entry.value);
					this.register_statuses.ROBTag[d] = -1; 
					this.reorder_buffer.removeFromBuffer();
					instructions_completed++;
				}

			}
		}
	}
	
	private void flushRss() {
		for (int i = 0; i < this.reservation_stations.length; i++) {
			reservation_stations[i].flushRS();
		}
		
	}

	public void Simulation() {
		this.cycles = 0;
		while(! (this.cycles > 1 
				&& this.reorder_buffer.isEmpty() 
				&& this.instruction_buffer.isEmpty()
				&& PC == PC_END
				)) {
//		while(!this.instruction_buffer.isEmpty()) {
//		while(cycles < 40) {
			cycles++;

			commit();
			
			write();
			
			execute();
			
			issue();

			fetch();
			
			System.out.println("Cycle:"+ cycles);
			System.out.println("---------------------");
			print();
//			this.mem_heirarchy.printCacheAccesses();
		}
//		this.mem_heirarchy.memory.print_part_memory(0,6);
		printOutputs();
//		this.mem_heirarchy.caches[0].printCache();
//		System.out.println("-----------------------------------");
//		this.mem_heirarchy.caches[1].printCache();
//		System.out.println("-----------------------------------");
//		this.mem_heirarchy.caches[2].printCache();

	}
	
	private void printOutputs() {
		System.out.println("Performance Metrics");
		System.out.println("---------------------");

		System.out.println("Total Execution Time: " + this.cycles + " cycles");
		System.out.println("IPC: " + (double)this.instructions_completed/ (double)this.cycles);
		System.out.println("AMAT: " + AMAT(this.mem_hierarchy.caches.length));


		System.out.println("Branch Misprediction Percentage: " + ((double)mispredictions/(double)branches) * 100 +"%");
		
		for(int i = 0; i < this.mem_hierarchy.caches.length; i++) {
			String cacheName;
			Cache cache = this.mem_hierarchy.caches[i];

			if(i == 0)
				cacheName = "1 (Instruction) -->";
			else if (i == 1)
				cacheName = "1 (Data) -->";
			else
				cacheName = ""+i+" -->";
			
			double hitRatio = (double) cache.hits /((double)cache.hits + (double) cache.misses);
			System.out.println("Cache " + cacheName + " hit ratio: " + hitRatio);
		}
		System.out.println("----------------------------------------");

	}

	public void print() {
		System.out.println("Instrcutions Completed: " + instructions_completed);
		System.out.println("Instrcutions Executed: " + instructions_executed);
		System.out.println("---------------------");
		System.out.println("Branches Encountered: " + branches);
		System.out.println("---------------------");
		System.out.println("Branches Mispredictions: " + mispredictions);
		System.out.println("---------------------");
		System.out.println("Total Cycles spent to access Memory: " + this.mem_hierarchy.memory.total_cycles + " cycles");
		System.out.println("---------------------");
		this.mem_hierarchy.printCacheAccesses();
		System.out.println("---------------------");
		this.instruction_buffer.printInstructionBuffer();
		System.out.println("---------------------");
		this.reorder_buffer.printROB();
		System.out.println("----------------------");
		printRSs();
		System.out.println("---------------------");
		this.register_statuses.printRegisterStatusTable();
		System.out.println("---------------------");
		this.register_file.printRegisterFile();
		System.out.println("---------------------------------------------------------------------");
	}
	
	public double AMAT(int cacheLevel) {
		if(cacheLevel == 1)
			return (double) this.mem_hierarchy.caches[1].cycles;
		else {
			double result = 0;
			if(cacheLevel == this.mem_hierarchy.caches.length)
				 result = this.mem_hierarchy.memory.access_time;
			else
			     result = this.mem_hierarchy.caches[cacheLevel].cycles;
			for(int i = 1; i < cacheLevel; i++) {
				double missRate;
				if(i == 1) {
					missRate = (this.mem_hierarchy.caches[0].missRate() + this.mem_hierarchy.caches[1].missRate()) / (double) 2;
				}
				else {
					missRate = this.mem_hierarchy.caches[i].missRate();
				}
				result *= missRate;
			}
			return result + AMAT(cacheLevel - 1);
		}
	}
	
	public void printRSs() {
		System.out.println("Reservation Stations");
		for (int i = 0; i < this.reservation_stations.length; i++) {
			ReservationStation RS = this.reservation_stations[i];
			System.out.println("Name: "+RS.name+ ", Busy:" + RS.busy + ", Op:" + RS.op + ", Vj: " + RS.Vj + ", VK: " +RS.Vk +
					", Qj: " + RS.Qj +", Qk: " + RS.Qk + ", Dest:" + RS.dest + ", A: " + RS.A);
		}
	}

	public int getCycles() {
		return cycles;
	}
	
	public int getNumberOfInstructions() {
		return instructions_completed;
	}
	

    

}
