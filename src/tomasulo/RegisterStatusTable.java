package tomasulo;

public class RegisterStatusTable {
	
	int [] ROBTag;
	
	public RegisterStatusTable() {
		this.ROBTag = new int [8];
		for(int i = 0; i < this.ROBTag.length; i++)
			this.ROBTag[i] = -1;
	}
	
	public void printRegisterStatusTable() {
		System.out.println("Register Status");
		for (int i = 0; i < this.ROBTag.length; i++) {
			System.out.println("R"+i+": " + ROBTag[i] );
	}


	}

	public void flush() {
		for (int i = 0; i < this.ROBTag.length; i++) {
			ROBTag[i] = -1;
		}
	}
}
