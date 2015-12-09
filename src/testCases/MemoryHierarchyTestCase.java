package testCases;
import memoryHierarchy.*;
public class MemoryHierarchyTestCase {

	public MemoryHierarchyTestCase() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	
	public static String convert_to_binary(int decimal) {
		String res = "";
		int rem = 0;
		while (decimal != 0) {
			rem = decimal % 2;
			res = rem + res;
			decimal = decimal / 2;
		}
		while(res.length() < 16)
			res = 0 + res;
		return res;
	}
	public static void main(String[] args) {
		String [] cacheInfo = {"64,8,1,writeBack,writeAllocate,3"};
		MemoryHierarchy memoryHeirarchy = new MemoryHierarchy(10, 1, cacheInfo);
		memoryHeirarchy.printHierarchyInfo();
		memoryHeirarchy.memory.WriteToMemory(4, 4);
		memoryHeirarchy.memory.WriteToMemory(536, 536);
		memoryHeirarchy.memory.WriteToMemory(848, 848);
		memoryHeirarchy.memory.WriteToMemory(540, 540);
		memoryHeirarchy.memory.WriteToMemory(852, 852);
		memoryHeirarchy.memory.WriteToMemory(648, 648);
		memoryHeirarchy.memory.WriteToMemory(644, 644);
		memoryHeirarchy.memory.WriteToMemory(8, 8);
		memoryHeirarchy.memory.WriteToMemory(175, 175);
		memoryHeirarchy.memory.WriteToMemory(164, 164);
		memoryHeirarchy.memory.WriteToMemory(884, 884);
//		memoryHeirarchy.memory.print_part_memory(0, 890);
		
		int [] addresses = {4,536,848,4, 540, 852, 648, 644, 8, 175, 164, 884};
		String [] addresses_str = new String[addresses.length];
		for(int i = 0; i < addresses.length; i++) {
			addresses_str[i] = convert_to_binary(addresses[i]);
		}
		for(int j = 0; j < addresses_str.length; j++)
			System.out.println(memoryHeirarchy.read_data(addresses_str[j]));
		
		memoryHeirarchy.caches[0].printCache();

	}

}
