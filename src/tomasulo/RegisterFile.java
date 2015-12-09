package tomasulo;

public class RegisterFile {
	
	public int [] registers; // Each Register 16 bits
	
	public RegisterFile() {
		this.registers = new int[8];
	}

	public void printRegisterFile() {
		System.out.println("Register File");
		for (int i = 0; i < this.registers.length; i++) {
			System.out.println("R"+i+": " + registers[i] );
		}		
	}
}
