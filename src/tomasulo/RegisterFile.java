package tomasulo;

public class RegisterFile {
	
	public String [] registers; // Each Register 16 bits
	final String value_zero = "0000000000000000";

	public RegisterFile() {
		this.registers = new String[8];
		for(int i = 1; i < registers.length; i++) {
			registers[i] = "00000000000000000"; 
		}
		registers[0] = value_zero;
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

	public void printRegisterFile() {
		System.out.println("Register File");
		for (int i = 0; i < this.registers.length; i++) {
			System.out.println("R"+i+": " + convertToDecimal(registers[i]) );
		}		
	}
}
