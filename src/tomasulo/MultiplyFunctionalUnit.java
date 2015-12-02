package tomasulo;

public class MultiplyFunctionalUnit {

	int cycles;
	
	public MultiplyFunctionalUnit(int cycles) {
		this.cycles = cycles;
	}

	
	public int multiply(int op1, int op2) {
		return op1 * op2;
	}

}
