package tomasulo;

public class ReorderBufferEntry {
	String type;
	int Dest; // Destination
	int value;
	boolean ready;
	
	int PC_value;
	
	public ReorderBufferEntry() {
		this.type = "";
	}
	
	public ReorderBufferEntry(String type, int Dest, boolean  ready) {
		this.type = type;
		this.Dest = Dest;
		this.ready = ready;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
