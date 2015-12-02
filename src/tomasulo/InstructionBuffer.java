package tomasulo;

public class InstructionBuffer {

	Instruction[] instruction_buffer;
	int head;
	int tail;
	
	public InstructionBuffer(int size) {
		this.instruction_buffer = new Instruction[size];
	}

	public boolean addToBuffer(Instruction i) {
		if (this.instruction_buffer[tail] == null) {
			this.instruction_buffer[tail] = i;
			tail++;
			
			if(tail == this.instruction_buffer.length)
				tail = 0;
			return true;
		}
		else {
			return false;
		}
	}
	
	public Instruction removeFromBuffer() {
		Instruction i = this.instruction_buffer[head];
		if(i == null)
			return null;

		this.instruction_buffer[head] = null;
		head++;
		if(head == this.instruction_buffer.length)
			head = 0;
		return i;
	}
	
	public Instruction getInstruction() {
		return this.instruction_buffer[this.head];
	}
	
	public boolean isEmpty() {
		for(int i = 0; i < this.instruction_buffer.length; i++) {
			if(this.instruction_buffer[i] != null)
				return false;
		}
		return true;
	}
	public static void main(String[] args) {
	}

}
