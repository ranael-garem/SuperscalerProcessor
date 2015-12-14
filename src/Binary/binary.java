package Binary;

public class binary {

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
	
	public static String convertToBinary(int x) {
		String temp = Integer.toBinaryString(x);
		if (x >= 0) {
			while(temp.length() < 16)
				temp = "0" + temp;
		}
		else {
			temp = temp.substring(16);
		}
		return temp;
	}
	

}
