package tomasulo;

public class Instruction {
	
	int rs;
	int rt;
	int rd;
	int imm;
	
	String type;
	String status; // Fetched, Issued, Executed, Written, Committed
	
	int PC_value;

	public Instruction() {
		
	}
	public Instruction(int rs, int rt, int rd, int imm, String type) {
		this.rs = rs;
		this.rd = rd;
		this.rt = rt;
		this.imm = imm;
		this.type = type;
	}

}
