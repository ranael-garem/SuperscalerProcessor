package tomasulo;

import java.util.ArrayList;

public class Tomasulo {
	ReservationStation [] reservation_stations;
	RegisterFile register_file;
	RegisterStatusTable register_statuses;
	ReorderBufferTable reorder_buffer;
	InstructionBuffer instruction_buffer;
	
	boolean CDB_available; //Common Data Bus

	int cycles; // Number of cycles spanned
	int instructions_completed; //Number of instructions completed
	int PC; // PC register pointing to the instruction to be fetched
	int PC_END; //Address of PC where program ends
	
	ArrayList<Integer> RS_indices; // Indices of RSs in the order they were issued
	
	int no_instr_completed; // Number of instructions completed
	boolean memory_stall;

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
		int counter = 0;
		// Initialize the reservation stations table with each entry's name that refers to the functional unit
		for(int i = 0; i < functional_unit_info.length; i++) {
			String [] functional_unit_data = functional_unit_info[i].split(",");
			String type = functional_unit_data[0];
			int FU_count = Integer.parseInt(functional_unit_data[1]);
			int cycles = Integer.parseInt(functional_unit_data[2]);

			for(int j = 0; j < FU_count; j++ ) {
				this.reservation_stations[counter] = new ReservationStation(type+(j+1), cycles);
				if(type.equals("Add")) {
					this.reservation_stations[counter].FU = new AddFunctinalUnit(cycles); //Initialize AndFU with number of cycles
				}
				else if(type.equals("Multiply")) {
					this.reservation_stations[counter].FU = new MultiplyFunctionalUnit(cycles);
				}
				counter++;
			}
		}
		
		this.PC = PC_ORG;
		this.PC_END = PC_END;

	}
	
	public void fetch() {
		if(PC != PC_END) {
			//TODO readFromMemory(PC) Will return 2 bytes
			PC +=2;
			//TODO change 2 bytes to Instruction Object
			this.instruction_buffer.addToBuffer(null); // TODO add Instruction Object

		}
		else return; // Program ended, No More instructions to fetch
	}
	
	//TODO NOW ONLY FOR ADD AND MULTIPLY AND LOAD
	public void issue() {
		
		if(this.instruction_buffer.getInstruction() == null) {
			return; //No instruction available in the Instruction Buffer
		}
		

		Instruction i = this.instruction_buffer.getInstruction();
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

				checkRegisterReadyRs(i, RS);
				if(i.type.toLowerCase().equals("load")) {
					RS.A = i.imm;
					this.register_statuses.ROBTag[i.rt] = b;
					RS.dest = b;
					ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rt, false);
					this.reorder_buffer.addToBuffer(reorder_entry);
				}
				else {
				checkRegisterReadyRt(i, RS);
				RS.dest = b;
				ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
				this.reorder_buffer.addToBuffer(reorder_entry);
				this.register_statuses.ROBTag[i.rd] = b;
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
		for(int j = 0; j < this.reservation_stations.length; j++) {
			if(this.reservation_stations[j].name.contains(i.type) && !this.reservation_stations[j].busy) {
				return j;
			}
		}
		return -1;
	}
	
	public void execute() {
		for(int i = 0; i < this.reservation_stations.length; i++) {
			ReservationStation RS = this.reservation_stations[i];
			if(RS.busy) {
				if(RS.start_execute) {
					if(RS.exec_cycles_left == 0) {
						RS.result = RS.FU.execute(RS.op, RS.Vj, RS.Vk);
						
					}else {
						RS.exec_cycles_left--;
					}
				}
				else if (RS.Qj == 0 && RS.Qk == 0) {
					RS.start_execute = true;
				}
			}
		}
	}
	
	public void write() {
		for(int i = 0; i < this.RS_indices.size(); i++ ) {
			ReservationStation RS = this.reservation_stations[RS_indices.get(i)];
			if(RS.exec_cycles_left == 0) {
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
						RS2.Qj = 0;
					}
					if(RS2.Qk == b) {
						RS2.Vk = RS.result;
						RS2.Qk = 0;
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
		
		int d = ROB_entry.Dest;
		if(ROB_entry.ready) {
			this.register_file.registers[d] = ROB_entry.value;
			this.register_statuses.ROBTag[d] = -1; 
			this.reorder_buffer.removeFromBuffer();
			instructions_completed++;
		}
	}
	
	public void Simulation() {
		this.cycles = 1;
		while(!(this.cycles > 1 && this.reorder_buffer.isEmpty() && this.instruction_buffer.isEmpty() && !this.memory_stall)  ) {
			commit();
			
			write();
			
			execute();
			
			issue();

			fetch();
			
			cycles++;
			
		}
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
