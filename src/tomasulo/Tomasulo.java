package tomasulo;

public class Tomasulo {
	ReservationStation [] reservation_stations;
	RegisterFile register_file;
	RegisterStatusTable register_statuses;
	ReorderBufferTable reorder_buffer;
	InstructionBuffer instruction_buffer;
	
	AddFunctinalUnit addUnit;
	MultiplyFunctionalUnit multiplyUnit;
	
	int cycles; // Number of cycles spanned
	int instructions_completed; //Number of instrucstions completed
	int PC; // PC register pointing to the instruction to be fetched
	
	public Tomasulo (int no_reorder_buffer_entries,
			int instruction_buffer_entries, String[] functional_unit_info, int PC_Value) {

		this.register_file = new RegisterFile();
		this.register_statuses = new RegisterStatusTable();
		this.reorder_buffer = new ReorderBufferTable(no_reorder_buffer_entries);
		this.instruction_buffer = new InstructionBuffer(instruction_buffer_entries);
		
		int no_reservation_stations = 0;
		//User enters array of Strings with specification of each functional unit
		//e.g: Add,3,6  (3 is the number of ADDFus and 6 is number of cycles, the FU takes to execute
		for(int i = 0; i < functional_unit_info.length; i++) {
			String [] functional_unit_data = functional_unit_info[i].split(",");
			no_reservation_stations += Integer.parseInt(functional_unit_data[1]); // Calculate number of reservation stations
			if(functional_unit_data[0].equals("Add")) {
				this.addUnit = new AddFunctinalUnit(Integer.parseInt(functional_unit_data[2])); //Initialize AndFU with number of cycles
			}
			else if(functional_unit_data[0].equals("Multiply")) {
				this.multiplyUnit = new MultiplyFunctionalUnit(Integer.parseInt(functional_unit_data[2]));
			}
		}
		this.reservation_stations = new ReservationStation[no_reservation_stations];
		int counter = 0;
		// Initialize the reservation stations table with each entry's name that refers to the functional unit
		for(int i = 0; i < functional_unit_info.length; i++) {
			String [] functional_unit_data = functional_unit_info[i].split(",");
			for(int j = 0; j < Integer.parseInt(functional_unit_data[1]); j++ ) {
				this.reservation_stations[counter] = new ReservationStation(functional_unit_data[0]+(j+1));
				counter++;
			}
		}
		
		this.PC = PC_Value;

	}
	
	public void fetch() {
		//TODO readFromMemory(PC) Will return 2 bytes
		//TODO PC+2
		//TODO change 2 bytes to Instruction Object
		this.instruction_buffer.addToBuffer(null); // TODO add Instruction Object
	}
	
	//TODO NOW ONLY FOR ADD AND MULTIPLY
	public void issue() {
		Instruction i = this.instruction_buffer.removeFromBuffer();
		if(i != null) {
			// Check for available RS and ROB entry
			if((this.checkAvailableRS(i) != -1) && this.reorder_buffer.entries[reorder_buffer.tail] == null ) {
				ReservationStation r = this.reservation_stations[this.checkAvailableRS(i)];
				int b = reorder_buffer.tail; 
				r.busy = true;
				checkRegisterReadyRs(i, r);
				checkRegisterReadyRt(i, r);
				r.dest = reorder_buffer.tail;
				ReorderBufferEntry reorder_entry= new ReorderBufferEntry(i.type, i.rd, false);
				this.reorder_buffer.addToBuffer(reorder_entry);
				this.register_statuses.ROBTag[i.rd] = b;
			}
			else {
				return;
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
	
	public void Simulation() {
		this.cycles = 1;
		while(true) {
			fetch();
			issue();
			
			cycles++;
			
		}
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
