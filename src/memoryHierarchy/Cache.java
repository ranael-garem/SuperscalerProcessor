package memoryHierarchy;


public class Cache {
	
	int Size; // Size of Cache (Power of 2)
	int blockSize; // (Power of 2)
	int m; // associativity
	int cycles; // access time to data
	String writePolicyHit; // writeBack or WriteThrough
	String writePolicyMiss; // writeAllocate or writeAround 
	
	public Set [] sets;
	
	public Cache(int Size, int blockSize, int m, String writePolicyHit, String writePolicyMiss, int cycles) {
		this.Size = Size;
		this.blockSize = blockSize;
		this.m = m;
		this.writePolicyHit = writePolicyHit;
		this.writePolicyMiss = writePolicyMiss;
		this.cycles = cycles;
		this.sets = new Set[getNumberOfSets()];
		
		for (int i=0; i < this.sets.length; i++) {
			this.sets[i] = new Set(m, blockSize);
			
		}		
	}
	
	public int getNumberOfBlocks() {
		return this.Size / this.blockSize;
	}
	
	public int getNumberOfSets() {
		return this.getNumberOfBlocks() / m;
	}
	
	public int getIndex() {
		return log2(getNumberOfSets());
	}

	public int getOffset() {
		return log2(this.blockSize);
	}
	
	public int getTag() {
		return 16 - (getIndex() + getOffset());
	}
	public int read(String address) {
		// Returns a byte corresponding to a binary address //
		String tagBits = address.substring(0, getTag());
		String indexBits = address.substring(getTag(), 16 - getOffset());
		String offsetBits = address.substring(16 - getOffset(), 16);
		int index = (int) Long.parseLong(indexBits, 2);
		int offset = (int) Long.parseLong(offsetBits, 2);
		Set target = this.sets[index];
		Block target_block;
		for (int i = 0; i < target.blocks.length; i++) {
			if (tagBits.equals(target.blocks[i].tag) && target.blocks[i].validBit == 1) {
				target_block = target.blocks[i];
				return target_block.bytes[offset];
			}
		}
		
		return -1;		
	}
	
	public void write(String address, int data) {
		// writes a single byte corresponding to a binary address //
		String tagBits = address.substring(0, getTag());
		String indexBits = address.substring(getTag(), 16 - getOffset());
		String offsetBits = address.substring(16 - getOffset(), 16);
		int index = (int) Long.parseLong(indexBits, 2);
		int offset = (int) Long.parseLong(offsetBits, 2);
		Set target = this.sets[index];
		Block target_block;
		for (int i = 0; i < target.blocks.length; i++) {
			if (tagBits.equals(target.blocks[i].tag) && target.blocks[i].validBit == 1) {
				target_block = target.blocks[i];
				target_block.bytes[offset] = data;
				if (this.writePolicyHit.equals("writeBack"))
					target_block.dirtyBit = 1;
			}
		}
	}

	public boolean hitOrMiss(String address) {
		String tagBits = address.substring(0, getTag());
		String indexBits = address.substring(getTag(), 16 - getOffset());
		int index = (int) Long.parseLong(indexBits, 2);
		int tag = (int ) Long.parseLong(tagBits, 2);
		
		Set target = this.sets[index];
		for (int i = 0; i < target.blocks.length; i++) {
			if (tagBits.equals(target.blocks[i].tag) && target.blocks[i].validBit == 1) {
				System.out.println("HIT");
				return true;
			}
		}
		System.out.println("MISS");
		return false;
	}

	
	//TODO DIRTY BIT
	public void writeBlock(Block block, int index) {
		// Writes a block in a given set, Using random replacement
		Set set = this.sets[index]; // Set where block should be written
		for(int i = 0; i < set.blocks.length; i++) {
			if (set.blocks[i].validBit == 0) {
				set.blocks[i] = block; // replace block if an invalid block is found
				return;
			}
		}
		
		int random_index_block = (int) (Math.random() * this.m);
		set.blocks[random_index_block] = block;
		return;
	}
	
	public void printCache() {
		for (int i = 0; i < this.sets.length; i++) {
			System.out.print("Set "+i+": ");
			
			for(int j = 0; j < this.sets[i].blocks.length; j++) {
				System.out.print("Block " + j + ": ");
				for(int k = 0; k < this.sets[i].blocks[j].bytes.length; k++)
					System.out.print(this.sets[i].blocks[j].bytes[k] + ",");
				System.out.print(" ");
			}
			System.out.println(" ");
		}
	}
	
	public void printCacheInfo() {
		System.out.println("Size: " + this.Size);
		System.out.println("blockSize: " + this.blockSize);
		System.out.println("Number of Sets: " + this.getNumberOfSets());
		System.out.println("m: " + this.m);
		System.out.println("writePolicyHit: " + this.writePolicyHit);
		System.out.println("writePolicyMiss: " + this.writePolicyMiss);
		System.out.println("cycles: " + this.cycles);

	}
	
	public static int log2(int number) {
		int count = 0;
		while (number != 1) {
			if (number % 2 == 0) {
			number = number / 2;
			count++;
			}
			else {
				return -1;
			}
		}
		return count;
	}
	
	
}
