package memoryHierarchy;

public class Memory {

	String [] mem_array; // Represents the memory itself
	int access_time; // Number of cycles to access memory
	
	int fetch_cycles_left;
	
	int load_cycles_left;
	public boolean being_accessed;
	
	public int total_cycles; // Total number of cycles spent to access memory (All program)
	
	public Memory(int access_time) {
		this.mem_array = new String[65536];
		this.access_time = access_time;
		this.fetch_cycles_left = access_time;
		this.load_cycles_left = access_time;
	}
	
	
	public void WriteToMemory(int address, String data) {
		this.mem_array[address] = data;
	}
	public int getAccess_time() {
		return access_time;
	}


	public void setAccess_time(int access_time) {
		this.access_time = access_time;
	}


	public String ReadFromMemory(int address) {
		return this.mem_array[address];
	}
	
	public void print_memory() {
		for(int i = 0; i < this.mem_array.length; i++) {
			System.out.print("Addr: "+i+ " Value: "+ mem_array[i]+", ");
			if (i % 10 == 0)
				System.out.println();
		}
		System.out.println();
	}
	
	public void print_part_memory(int beg, int end) {
		for(int i = beg; i <= end; i++) {

			System.out.print("Addr: "+i+ " Value: "+ mem_array[i]+", ");
			if (i % 10 == 0)
				System.out.println();
		}
		System.out.println();
	}

}
