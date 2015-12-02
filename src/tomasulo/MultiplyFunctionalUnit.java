package tomasulo;

public class MultiplyFunctionalUnit extends FunctionalUnit {

	int cycles;
	
	public MultiplyFunctionalUnit(int cycles) {
		this.cycles = cycles;
	}

	public int execute(String type, int op1, int op2) {
		if(type.toLowerCase().equals("multiply")) {
			return multiply(op1, op2);
		}
		return 1000;
	}
	public int multiply(int op1, int op2) {
		return op1 * op2;
	}

}
