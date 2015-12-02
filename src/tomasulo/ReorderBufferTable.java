package tomasulo;

public class ReorderBufferTable {
	int size; // Number of entries
	int head;
	int tail;
	ReorderBufferEntry[] entries;

	public ReorderBufferTable(int size) {
		this.size = size;
		this.entries = new ReorderBufferEntry[size];
		for(int i = 0; i < this.entries.length; i++)
			this.entries[i] = new ReorderBufferEntry();
		
	}

	public void addToBuffer(ReorderBufferEntry i) {
		if (this.entries[tail] == null) {
			this.entries[tail] = i;
			tail++;
		}
			
			if(tail == this.entries.length)
				tail = 0;
	
	}
	
	public ReorderBufferEntry removeFromBuffer() {
		ReorderBufferEntry i = this.entries[head];
		if(i == null)
			return null;

		this.entries[head] = null;
		head++;
		if(head == this.entries.length)
			head = 0;
		return i;
	}

}
