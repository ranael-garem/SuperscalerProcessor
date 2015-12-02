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
					//System.out.println(cacheInfo[i]);
					//System.out.println("Pointer "+pointer);
				}
			}
			//System.out.println("Caches are Over!!");
			pointer++;
			memoryCycles = Integer.parseInt(
					input.get(pointer).substring(17));
			//System.out.println("Main memory cycles" + memoryCycles);
			pointer++;
		}
		m = new MemoryHierarchy(memoryCycles,numberOfCacheLevels, cacheInfo );
		//m.printHierarchyInfo();
	}

	public void parseHardwareOrganization(ArrayList<String> input) {
		int ROBentries = 0;
		int instruction_buffer_entries = 0;
		int simulInstrCount = 1;
		System.out.println(pointer);
		String[] FUinfo = null;
		if(input.get(pointer).equals("HardwareOrganization")) {
			pointer++;
			simulInstrCount = Integer.parseInt(
					input.get(pointer).substring(33));
			pointer++;
			instruction_buffer_entries = Integer.parseInt(
					input.get(pointer).substring(22)); 
			pointer++;
			ROBentries = Integer.parseInt(
					input.get(pointer).substring(10));
			pointer++;
			FUinfo = new String[(input.indexOf("AssemblyProgram") - pointer)/2];
			while(!input.get(pointer).equals("AssemblyProgram")) {
				for(int i = 0; i < FUinfo.length; i++) {
					if(input.get(pointer).substring(0,2).equals("SW")) {
						FUinfo[i] = "SW,";
						FUinfo[i] += input.get(pointer).substring(5)+",";
						pointer++;
						FUinfo[i] += input.get(pointer).substring(9);
						pointer++;
					}
					else if(input.get(pointer).substring(0,2).equals("LW")) {
						FUinfo[i] = "LW,";
						FUinfo[i] += input.get(pointer).substring(5)+",";
						pointer++;
						FUinfo[i] += input.get(pointer).substring(9);
						pointer++;
					}
					else if(input.get(pointer).substring(0,4).equals("JALR")) {
						FUinfo[i] = "JALR,";
						FUinfo[i] += input.get(pointer).substring(7)+",";
						pointer++;
						FUinfo[i] += input.get(pointer).substring(11);
						pointer++;
					}
					else if(input.get(pointer).substring(0,4).equals("ADDI")) {
						FUinfo[i] = "ADDI,";
						FUinfo[i] += input.get(pointer).substring(7)+",";
						pointer++;
						FUinfo[i] += input.get(pointer).substring(11);
						pointer++;
					}
					else if(input.get(pointer).substring(0,4).equals("NAND")) {
						FUinfo[i] = "NAND,";
						FUinfo[i] += input.get(pointer).substring(7)+",";
						pointer++;
						FUinfo[i] += input.get(pointer).substring(11);
						pointer++;
					}
					else {
						FUinfo[i] = input.get(pointer).substring(0,3)+",";
						FUinfo[i] += input.get(pointer).substring(6)+",";
						pointer++;
						//System.out.println(pointer);
						FUinfo[i] += input.get(pointer).substring(10);
						pointer++;
					}
					System.out.println(FUinfo[i]);
				}
			}
		}
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
