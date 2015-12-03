package functionalUnits;

public class BranchFunctionalUnit extends FunctionalUnit {
	
	int cycles;
	
	public BranchFunctionalUnit(int cycles) {
		this.cycles = cycles;
	}


	@Override
	public int execute(String type, int op1, int op2) {
		if (op1 == op2)
			return 1;
		else
			return 0;
	}

}
