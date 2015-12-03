package functionalUnits;


public class LoadFunctionalUnit extends FunctionalUnit {
	
	int cycles;
	public LoadFunctionalUnit(int cycles) {
		// TODO Auto-generated constructor stub
		this.cycles = cycles;
	}


	public int calculateAddress(int op1, int op2) {
		return op1 + op2;
	}
	
	@Override
	public int execute(String type, int op1, int op2) {
		if(type.toLowerCase().equals("calculate_addr")) {
			return calculateAddress(op1, op2);			
		}
		else return 10;
	}

}
