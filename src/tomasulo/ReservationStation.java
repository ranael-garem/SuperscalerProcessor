package tomasulo;

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
	
	int result;
	
	FunctionalUnit FU;

	public ReservationStation(String name, int cycles) {
		this.name = name;
		this.busy = false;
		this.op = "";
		this.cycles = cycles;
		this.Qj = -1;
		this.Qk = -1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
