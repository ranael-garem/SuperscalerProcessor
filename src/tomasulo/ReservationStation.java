package tomasulo;

import functionalUnits.FunctionalUnit;

public class ReservationStation {
	
	String name;
	boolean busy;
	String op;
	int Vj;
	int Vk;
	int Qj;
	int Qk;
	int dest;
	int A;
	int result;

	boolean start_execute;
	int cycles; //Number of cycles To execute by FU
	int exec_cycles_left; //Number of execution cycles left
	
	
	FunctionalUnit FU;
	
	
	boolean calculated_address; //For Load Reservation Stations
	int cache_level; // For load instructions
	boolean accessed_cache;
	String lowByte;
	boolean lowByteSet;
	
//	int store_cycles_left;
	boolean start_store;
	boolean write_low_byte;
	boolean write_high_byte;

	
	public ReservationStation(String name, int cycles) {
		this.name = name;
		this.busy = false;
		this.op = "";
		this.cycles = cycles;
		this.Qj = -1;
		this.Qk = -1;
		this.calculated_address = false;
		this.exec_cycles_left = this.cycles;
	}
	
	public void flushRS() {
		this.busy = false;
		this.op = "";
		this.Qj = -1;
		this.Qk = -1;
		this.calculated_address = false;
		this.exec_cycles_left = this.cycles;
		this.result = 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
