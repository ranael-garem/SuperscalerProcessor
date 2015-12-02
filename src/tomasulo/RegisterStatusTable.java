package tomasulo;

public class RegisterStatusTable {
	
	int [] ROBTag;
	
	public RegisterStatusTable() {
		this.ROBTag = new int [8];
		for(int i = 0; i < this.ROBTag.length; i++)
			this.ROBTag[i] = -1;
	}


}
