package functionalUnits;

public class AddFunctinalUnit extends FunctionalUnit {
	
	int cycles;
	
	public AddFunctinalUnit(int cycles) {
		this.cycles = cycles;
	}
	
	public int execute(String type, int op1, int op2) {
		if(type.toLowerCase().equals("add") || type.toLowerCase().equals("addi")) {
			return add(op1, op2);
		}
		else if(type.toLowerCase().equals("subtract")) {
			return subtract(op1, op2);
		}
		else if(type.toLowerCase().equals("branch")) {
			return branch(op1, op2);
		}else {
			
			return nand(op1, op2);
		}
	}

	private int branch(int op1, int op2) {
		if (op1 == op2)
			return 1;
		else
			return 0;
	}

	public int add(int op1, int op2) {
		return op1 + op2;
	}
	
	public int subtract(int op1, int op2) {
		return op1 - op2;
	}
	
	public int nand(int op1, int op2) {
		String bin_op1 = convert_to_binary(op1);
		String bin_op2 = convert_to_binary(op2);
		
		String result = "";
		for(int i = 0; i < 16; i++) {
			if(bin_op1.charAt(i) == '1' && bin_op2.charAt(i) == '1')
				result += '0';
			else
				result += '1';
				
		}
		return (int) Long.parseLong(result);

		}
	
	public static String convert_to_binary(int decimal) {
		String res = "";
		int rem = 0;
		while (decimal != 0) {
			rem = decimal % 2;
			res = rem + res;
			decimal = decimal / 2;
		}
		while(res.length() < 16)
			res = 0 + res;
		return res;
	}

}
