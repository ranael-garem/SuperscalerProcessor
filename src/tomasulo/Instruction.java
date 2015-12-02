package tomasulo;

public class Instruction {
	
	int rs;
	int rt;
	int rd;
	int imm;
	
	String type;
	String status; // Fetched, Issued, Executed, Written, Committed
	
	public Instruction() {
		
	}

}
