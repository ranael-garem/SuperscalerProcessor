package testCases;
import memoryHierarchy.*;;

public class MemoryTestCase {

	public MemoryTestCase() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Memory memory = new Memory(100);
		memory.print_memory();
		memory.WriteToMemory(10, 500);
		System.out.println(memory.ReadFromMemory(10));
		memory.print_part_memory(0, 100);
	}

}
