package InputParse;

public class RISCDecoder {
	public static String decode(String x) {
		String res = "";
		if(x.substring(0,2).equals("LW"))
		{
			res+="101";
			res+= RISCDecoder.register(x.substring(3, 7));
			res+= RISCDecoder.register(x.substring(8, 12));
			if(Integer.parseInt(x.substring(13)) < 0) {
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(13))).substring(25);}
			else {
				for(int i = Integer.toBinaryString(Integer.parseInt(x.substring(13))).length();
						i < 7; i++){
					res += "0";
				}
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(13)));
			}
		}
		else if(x.substring(0,2).equals("SW"))
		{
			res+="100";
			res+= RISCDecoder.register(x.substring(3, 7));
			res+= RISCDecoder.register(x.substring(8, 12));
			if(Integer.parseInt(x.substring(13)) < 0) {
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(13))).substring(25);}
			else {
				for(int i = Integer.toBinaryString(Integer.parseInt(x.substring(13))).length();
						i < 7; i++){
					res += "0";
				}
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(13)));
			}		}
		else if(x.substring(0,4).equals("JALR")){
			res+="000";
			res+= RISCDecoder.register(x.substring(5, 9));
			res+= RISCDecoder.register(x.substring(10, 14));
			res+= "0000000";
		}
		else if(x.substring(0,3).equals("BEQ")) {
			res+="001";
			res+= RISCDecoder.register(x.substring(4, 8));
			res+= RISCDecoder.register(x.substring(9, 13));
			if(Integer.parseInt(x.substring(14)) < 0) {
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(14))).substring(25);}
			else {
				for(int i = Integer.toBinaryString(Integer.parseInt(x.substring(14))).length();
						i < 7; i++){
					res += "0";
				}
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(14)));
			}
		}
		else if(x.substring(0,4).equals("ADDI")){
			res+="010";
			res+= RISCDecoder.register(x.substring(5, 9));
			res+= RISCDecoder.register(x.substring(10, 14));
			if(Integer.parseInt(x.substring(15)) < 0) {
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(15))).substring(25);}
			else {
				for(int i = Integer.toBinaryString(Integer.parseInt(x.substring(15))).length();
						i < 7; i++){
					res += "0";
				}
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(15)));
			}
		}
		else if(x.substring(0,3).equals("ADD")){
			res+="011";
			res+= RISCDecoder.register(x.substring(4, 8));
			res+= RISCDecoder.register(x.substring(9, 13));
			res+="0000";
			res+= RISCDecoder.register(x.substring(14));
		}
		else if(x.substring(0,3).equals("MUL")){
			res+="100";
			res+= RISCDecoder.register(x.substring(4, 8));
			res+= RISCDecoder.register(x.substring(9, 13));
			res+="0000";
			res+= RISCDecoder.register(x.substring(14));
		}
		else if(x.substring(0,4).equals("NAND")){
			res+="101";
			res+= RISCDecoder.register(x.substring(5, 9));
			res+= RISCDecoder.register(x.substring(10, 14));
			res+="0000";
			res+= RISCDecoder.register(x.substring(15));
		}
		else if(x.substring(0,3).equals("SUB")){
			res+="110";
			res+= RISCDecoder.register(x.substring(4, 8));
			res+= RISCDecoder.register(x.substring(9, 13));
			res+="0000";
			res+= RISCDecoder.register(x.substring(14));
		}
		else if(x.substring(0,3).equals("JMP")){
			res+="111";
			res+= RISCDecoder.register(x.substring(4, 8));
			if(Integer.parseInt(x.substring(10)) < 0) {
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(10))).substring(22);}
			else {
				for(int i = Integer.toBinaryString(Integer.parseInt(x.substring(10))).length();
						i < 7; i++){
					res += "0";
				}
				res+= Integer.toBinaryString(Integer.parseInt(x.substring(10)));
			}
		}
		else if(x.substring(0,3).equals("RET")){
			res+="111";
			res+= RISCDecoder.register(x.substring(4, 8));
			res+= "0000000000";
		}
		return res;
	}
	public static String register(String y) {
		if(y.equals("reg0")) {
			return "000";
		}
		else if(y.equals("reg1")) {
			return "001";
		}
		else if(y.equals("reg2")) {
			return "010";
		}
		else if(y.equals("reg3")) {
			return "011";
		}
		else if(y.equals("reg4")) {
			return "100";
		}
		else if(y.equals("reg5")) {
			return "101";
		}
		else if(y.equals("reg6")) {
			return "110";
		}
		else {
			return "111";
		}
	}
}
