package tomasulo;

import java.util.ArrayList;

import memoryHierarchy.MemoryHierarchy;

import functionalUnits.AddFunctinalUnit;
import functionalUnits.JALRFunctionalUnit;
import functionalUnits.LoadFunctionalUnit;
import functionalUnits.MultiplyFunctionalUnit;

public class Tomasulo {
	public MemoryHierarchy mem_heirarchy;
	
	ReservationStation [] reservation_stations;
	public RegisterFile register_file; //TODO REGISTER 0 is Final
	RegisterStatusTable register_statuses;
	ReorderBufferTable reorder_buffer;
	InstructionBuffer instruction_buffer;
	
	boolean CDB_available; //Common Data Bus

	int cycles; // Number of cycles spanned
	int instructions_completed; //Number of instructions completed
	int PC; // PC register pointing to the instruction to be fetched
	int PC_END; //Address of PC where program ends
	
	ArrayList<Integer> RS_indices; // Indices of RSs in the order they were issued 
	
	int branches;
	int mispredictions;
	
	boolean memory_stall;
	boolean jump_stall;
	
	boolean low_byte_set;
	String low_byte;

	public Tomasulo (int no_reorder_buffer_entries,
			int instruction_buffer_entries, String[] functional_unit_info, int PC_ORG, int PC_END) {

		this.register_file = new RegisterFile();
		this.register_statuses = new RegisterStatusTable();
		this.reorder_buffer = new ReorderBufferTable(no_reorder_buffer_entries);
		this.instruction_buffer = new InstructionBuffer(instruction_buffer_entries);
		
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
				
				if(type.toLowerCase().equals("add")) {
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
			Instruction I = this.instruction_buffer.instruction_buffer[this.instruction_buffer.tail - 1];
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
			if(mem_heirarchy.done_fetching(address)) {
				System.out.println("ay 7aga");
				low_byte = Integer.toBinaryString(mem_heirarchy.read_instruction(address));	
				while(low_byte.length() < 8)
					low_byte = "0" + low_byte;
				low_byte_set = true;
			}
			return null;
		}
		else {
			address = Integer.toBinaryString(PC + 1);
			while(address.length() < 16)
				address = "0" + address;
			if(mem_heirarchy.done_fetching(address)) {
				high_byte = Integer.toBinaryString(mem_heirarchy.read_instruction(address));	
				while(high_byte.length() < 8)
					high_byte = "0" + high_byte;
			}
			else {
				return null;
			}
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
			I.rt = PC + 2;
			int b = this.register_statuses.ROBTag[I.rs];
			if(b != -1) {
				if(this.reorder_buffer.entries[b].ready) {
					PC = this.reorder_buffer.entries[b].value;
					this.jump_stall = false;
				}
				else { this.jump_stall = true;}
			}
			else {
				PC = this.register_file.registers[I.rs];
				this.jump_stall = false;
			}
			return true;
		}
		return false; //Instruction is not JALR
	}

	public boolean jumpFetchCheck(Instruction I) {
		if(I.type.toLowerCase().equals("jump")) {
			int b = this.register_statuses.ROBTag[I.rs];
			if(b != -1) {
				if(this.reorder_buffer.entries[b].ready) {
					PC = PC + 2 + this.reorder_buffer.entries[b].value + I.imm;
					this.jump_stall = false;
				}
				else { this.jump_stall = true;}
			}
			else {
				System.out.println("IMM" + I.imm);

				PC = PC + 2 + this.register_file.registers[I.rs] + I.imm;
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
				PC = this.register_file.registers[I.rs];
				this.jump_stall = false;
			}
			return true;
		}
		return false; // Intruction is not return
	}

	public void issue() {
		
		if(this.instruction_buffer.getInstruction() == null) {
			return; //No instruction available in the Instruction Buffer
		}

		Instruction i = this.instruction_buffer.getInstruction();
			if(i.type.toLowerCase().equals("jump") || i.type.toLowerCase().equals("return")) {
				if(this.reorder_buffer.entries[reorder_buffer.tail] == null) {	//Check for available ROB only
					ReorderBufferEntry reorder_entry = new ReorderBufferEntry(i.type, -1, true);
					this.reorder_buffer.addToBuffer(reorder_entry);
					this.instruction_buffer.removeFromBuffer();
				}
				return;
			}
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
					ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
					this.reorder_buffer.addToBuffer(reorder_entry);
					this.register_statuses.ROBTag[i.rd] = b;
					return;
				}
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
				}
		}
	
	public void checkRegisterReadyRs(Instruction i, ReservationStation r) {
		System.out.println(i.rs);
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
			r.Vj = this.register_file.registers[i.rs];
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
			r.Vk = this.register_file.registers[i.rt];
			r.Qk = -1;
		}
	}
	
	public  int checkAvailableRS(Instruction i) {
		String type = "";
		if(i.type.equals("add") || i.type.equals("nand") || i.type.equals("subtract")) {
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
						if(RS.calculated_address && storeCheck(RS.A)) {
							System.out.println("EXEC" + RS.exec_cycles_left);
							if(RS.exec_cycles_left == 0) {
								RS.result = mem_heirarchy.read_data(address);
								System.out.println("RESULT: " +  RS.result);
							}
							else {
								RS.exec_cycles_left--;
							}
						} else {
						RS.A = RS.FU.execute("calculate_addr", RS.Vj, RS.A);
						RS.calculated_address = true;
						RS.exec_cycles_left = this.mem_heirarchy.load_cycles_left(address);
						}
					}
				}
				else if(RS.op.toLowerCase().equals("store")) {
					if(RS.Qj == -1) {
						this.reorder_buffer.entries[RS.dest].Dest = RS.Vj + RS.A;
						RS.exec_cycles_left = 0;
					}
				}
				else if(RS.op.toLowerCase().equals("jalr")) {
					RS.result = RS.Vk;
					RS.exec_cycles_left = 0;
				}
				else { //Arithmetic Operations and Branch
				if(RS.start_execute) {
					if(RS.exec_cycles_left == 0) {
						RS.result = RS.FU.execute(RS.op, RS.Vj, RS.Vk);
						if(RS.op.toLowerCase().equals("branch")) {
							if(RS.result == 1 && (RS.A < 0)) // 2 operands are equal, and Branch was taken
								RS.result = 1; // Correct Prediction
							if(RS.result == 1 && (RS.A >= 0)) { // 2 operands are equal, and Branch was not taken
								RS.result = 0; // mispredictions
								this.mispredictions++;
							}
							if(RS.result == 0 && (RS.A < 0)) { // 2 operands are not equal and Branch was taken
								RS.result = 0;
								this.mispredictions++;
							}
							if(RS.result == 0 && (RS.A >= 0))  // 2 operands are not equal and branch was not taken
								RS.result = 1; // Correct Prediction
						}

					}else {
						RS.exec_cycles_left--;
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
		for(int i = 0; i < this.RS_indices.size(); i++ ) {
			ReservationStation RS = this.reservation_stations[RS_indices.get(i)];
			if(RS.op.toLowerCase().equals("store")) {
				if(RS.Qk == -1) {
					int b = RS.dest;
					this.reorder_buffer.entries[b].value = RS.Vk;
					this.reorder_buffer.entries[b].ready = true;

					RS.busy = false;
					RS.exec_cycles_left = RS.cycles;
					RS.start_execute = false;
					RS.op = "";
					RS.Qj = -1;
					RS.Qk = -1;
									
					this.reorder_buffer.entries[b].ready = true;
					this.reorder_buffer.entries[b].value = RS.result;
					RS_indices.remove(i);
					return;
				}
			}
			// All instructions but Store
			if(RS.exec_cycles_left == 0) {
				System.out.println("WRITE" + RS.exec_cycles_left);
				int b = RS.dest;
				RS.busy = false;
				RS.exec_cycles_left = RS.cycles;
				RS.start_execute = false;
				RS.op = "";
				RS.Qj = -1;
				RS.Qk = -1;
								
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
				RS_indices.remove(i);
				return;
			}
		}
	}
	
	//TODO BRANCH
	public void commit() {
		ReorderBufferEntry ROB_entry = this.reorder_buffer.getROB();
		if(ROB_entry == null)
			return;
		
		int d = ROB_entry.Dest; //Reg or Mem Address if store
		if(ROB_entry.ready) {
			if(ROB_entry.type.toLowerCase().equals("store")) {
				//Write in memory
				//TODO increment instruction only when no stall from writing
				//TODO Remove from ROB
				instructions_completed++;
				return;
			}
			if(ROB_entry.type.toLowerCase().equals("jump") || ROB_entry.type.toLowerCase().equals("return")) {
				this.reorder_buffer.removeFromBuffer();
				instructions_completed++;
				return;
			}
			if(ROB_entry.type.toLowerCase().equals("branch")) {
				if(ROB_entry.value == 1) { // Branch was predicted correctly
					this.reorder_buffer.removeFromBuffer();
					instructions_completed++;
					return;
				} else {
					this.PC = ROB_entry.PC_value;
					this.reorder_buffer.flush();
					this.register_statuses.flush();
					flushRss();
				}
				return;
			}
			this.register_file.registers[d] = ROB_entry.value;
			this.register_statuses.ROBTag[d] = -1; 
			this.reorder_buffer.removeFromBuffer();
			instructions_completed++;
		}
	}
	
	private void flushRss() {
		for (int i = 0; i < this.reservation_stations.length; i++) {
			reservation_stations[i].flushRS();
		}
		
	}

	public void Simulation() {
		//TODO Removed memory_stall
		this.cycles = 1;
//		while(! (this.cycles > 1 && this.reorder_buffer.isEmpty() && this.instruction_buffer.isEmpty())  ) {
//		while(!this.instruction_buffer.isEmpty()) {
		while(cycles < 40) {
			commit();
			
			write();
			
			execute();
			
			issue();

			fetch();
			
			System.out.println("Cycle:"+ cycles);
			System.out.println("---------------------");
			print();
			cycles++;


		}
	}
	
	public void print() {
		this.instruction_buffer.printInstructionBuffer();
		System.out.println("---------------------");
		this.reorder_buffer.printROB();
		System.out.println("----------------------");
		printRSs();
		System.out.println("---------------------");
		this.register_statuses.printRegisterStatusTable();
		System.out.println("---------------------");
		this.register_file.printRegisterFile();
		System.out.println("---------------------");

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
