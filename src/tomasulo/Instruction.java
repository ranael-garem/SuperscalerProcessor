package tomasulo;

public class Instruction {
	
	int rs;
	int rt;
	int rd;
	int imm;
	
	String type;
	String status; // Fetched, Issued, Executed, Written, Committed
	
	int PC_value;

	public Instruction(String instruction) {
		String opcode = instruction.substring(0,3);
		if(opcode.equals("000"))
			load(instruction);
		if(opcode.equals("001"))
			store(instruction);
		if(opcode.equals("010"))
			beq(instruction);
		if(opcode.equals("011"))
			jump(instruction);
		if(opcode.equals("100"))
			jalr(instruction);
		if(opcode.equals("101"))
			ret(instruction);
		if(opcode.equals("110"))
			addi(instruction);
		if(opcode.equals("111"))
			arithmetic(instruction);
	}
	private void arithmetic(String instruction) {
		// Add, Sub, nand mult rd, rs, rt
		String function_code = instruction.substring(12,16);
		if(function_code.equals("0000"))
			this.type = "add";
		else if(function_code.equals("0001"))
			this.type = "subtract";
		else if(function_code.equals("0010"))
			this.type = "nand";
		else if(function_code.equals("0011"))
			this.type = "multiply";

		this.rd = (int) Long.parseLong(instruction.substring(3, 6));
		this.rs = (int) Long.parseLong(instruction.substring(6, 9));
		this.rt = (int) Long.parseLong(instruction.substring(9, 12));

			
	}
	private void ret(String instruction) {
		// ret rs
		this.type = "return";
		this.rs = (int) Long.parseLong(instruction.substring(3, 6));
		this.rt = -1;
		this.rd = -1;
	}
	
	private void jump(String instruction) {
		// jmp rs, imm
		this.type = "jump";
		this.rs = (int) Long.parseLong(instruction.substring(3, 6));
		this.rd = -1;
		this.rt = -1;
		String imm = instruction.substring(6,13);
		this.imm = convertToDecimal(imm);
	}
	
	private void addi(String instruction) {
		// addi rd, rs, imm
		this.type = "addi";
		this.rd = (int) Long.parseLong(instruction.substring(3, 6));
		this.rs = (int) Long.parseLong(instruction.substring(6, 9));
		this.rt = -1;
		String imm = instruction.substring(9);
		this.imm = convertToDecimal(imm);		
	}
	
	private void beq(String instruction) {
		// BEQ rs, rt, imm
		this.type = "branch";
		this.rs = (int) Long.parseLong(instruction.substring(3, 6));
		this.rt = (int) Long.parseLong(instruction.substring(6, 9));
		this.rd = -1;
		String imm = instruction.substring(9);
		this.imm = convertToDecimal(imm);
		
	}
	
	private void jalr(String instruction) {
		// jalr rd, rs
		this.type = "jalr";
		this.rd = (int) Long.parseLong(instruction.substring(3, 6));
		this.rs = (int) Long.parseLong(instruction.substring(6, 9));
		this.rt = -1;
	}
	
	private void store(String instruction) {
		// SW rs, rt, imm
		this.type = "store";
		this.rs = (int) Long.parseLong(instruction.substring(3, 6));
		this.rt = (int) Long.parseLong(instruction.substring(6, 9));
		String imm = instruction.substring(9);
		this.imm = convertToDecimal(imm);
		this.rd = -1;
	}
	
	private void load(String instruction) {
		//LW, rd, rs, imm
		this.type = "load";
		this.rd = (int) Long.parseLong(instruction.substring(3, 6));
		this.rs = (int) Long.parseLong(instruction.substring(6, 9));
		this.rt = -1;
		String imm = instruction.substring(9);
		this.imm = convertToDecimal(imm);
	}
	
	public Instruction(int rs, int rt, int rd, int imm, String type) {
		this.rs = rs;
		this.rd = rd;
		this.rt = rt;
		this.imm = imm;
		this.type = type;
	}
	
	public static int convertToDecimal(String number) {		
		String result;
		long x;
		
		if (isNegative(number)) {
			result = twosComplementConverter(number);
			x = Long.parseLong(result, 2);
			x = (x * -1);
		} 
		else {
			x = Long.parseLong(number, 2);
		}
		
		return (int) x;
	}
	
	public static boolean isNegative(String number) {
		if (number.charAt(0) == '1') {
			return true;
		}
		else {
			 return false;
		}
	}
	
	public static String twosComplementConverter (String number) {
		String result ="";
		boolean flagOne = false;
		for(int i = number.length() -1 ; i >= 0; i--) {
			if (!flagOne) { // didn't find 1
			if (number.charAt(i) == '1' ) {
				flagOne = true;
				result= "1" + result;
			}
			else if (number.charAt(i) == '0') {
				result= "0" + result;
			}
			} else { // found 1 so we start flipping
				if (number.charAt(i) == '1' ) {
					result= "0" + result;
				}
				else if (number.charAt(i) == '0') {
					result= "1" + result;
				}
			}
		}
		//System.out.println(result);
		return result;
	}
	

}
