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
	boolean start_execute;
	int cycles; //Number of cycles To execute by FU
	int exec_cycles_left; //Number of execution cycles left
	
	boolean calculated_address; //For Load Reservation Stations
	
	int result;
	
	FunctionalUnit FU;

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
