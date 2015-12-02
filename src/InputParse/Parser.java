package InputParse;

import java.util.ArrayList;

import memoryHierarchy.MemoryHierarchy;

public class Parser {
private int pointer = 0;
MemoryHierarchy m;
	public void parseAll() {
		ArrayList<String> input = BufferedReaderExample.returnContents();
		parseMemoryHierarchy(input);
		parseHardwareOrganization(input);
	}
	public void parseMemoryHierarchy(ArrayList<String> input) {
		int memoryCycles = 0;
		int numberOfCacheLevels = 0;
		String [] cacheInfo = null;
		if(input.get(0).equals("MemoryHierarchy")) {
			numberOfCacheLevels = Integer.parseInt(
					input.get(1).substring(20));
			cacheInfo = new String [numberOfCacheLevels];
			for (int i = 0; i < numberOfCacheLevels; i++) {
				if(input.get(i*7+2).equals("Cache"+(i+1))) {
					cacheInfo[i]= input.get(i*7+2+1).substring(2)+",";
					cacheInfo[i]+= input.get(i*7+2+2).substring(2)+",";
					cacheInfo[i]+= input.get(i*7+2+3).substring(2)+",";
					cacheInfo[i]+= input.get(i*7+2+4).substring(15)+",";
					cacheInfo[i]+= input.get(i*7+2+5).substring(16)+",";
					cacheInfo[i]+= input.get(i*7+2+6).substring(12);
					pointer = i*7+2+6;
					System.out.println(cacheInfo[i]);
					System.out.println("Pointer "+pointer);
				}
			}
			System.out.println("Caches are Over!!");
			pointer++;
			memoryCycles = Integer.parseInt(
					input.get(pointer).substring(17));
			System.out.println("Main memory cycles" + memoryCycles);
			pointer++;
		}
		m = new MemoryHierarchy(memoryCycles,numberOfCacheLevels, cacheInfo );
		m.printHierarchyInfo();
	}

	public void parseHardwareOrganization(ArrayList<String> input) {
		
	}
	public void parseAssemblyProgram() {

	}
	public void parseProgramData() {

	}
	public static void main(String[] args) {
		Parser s = new Parser();
		s.parseAll();
	}
}
