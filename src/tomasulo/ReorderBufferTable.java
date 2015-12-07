package tomasulo;

public class ReorderBufferTable {
	int size; // Number of entries
	int head;
	int tail;
	ReorderBufferEntry[] entries;

	public ReorderBufferTable(int size) {
		this.size = size;
		this.entries = new ReorderBufferEntry[size];
//		for(int i = 0; i < this.entries.length; i++)
//			this.entries[i] = new ReorderBufferEntry();
		
	}

	public void printROB() {
		System.out.println("ROB");
		System.out.println("Head: " + this.head);
		System.out.println("Tail: " + this.tail);

		for(int i = 0; i < this.entries.length; i++) {
			ReorderBufferEntry b = this.entries[i];
			if(!(b == null))
				System.out.println("type:" + b.type+",Dest:" + b.Dest + ",Value:"+ b.value + ", Ready:"+b.ready);
		}
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
	
	public ReorderBufferEntry getROB() {
		return this.entries[this.head];
	}
	
	public boolean isEmpty() {
		for(int i = 0; i < this.entries.length; i++) {
			if(this.entries[i] != null)
				return false;
		}
		return true;
	}

	public void flush() {
		for(int i = 0; i < this.entries.length; i++) {
			this.entries[i] = null;
		}
		this.head = 0;
		this.tail = 0;
	}

}
