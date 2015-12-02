package memoryHierarchy;


public class MemoryHierarchy {
	public Memory memory;
	public Cache[] caches;
	
	public MemoryHierarchy(int mem_access_time, int cacheLevels, String[] cacheInfo) {
		this.memory = new Memory(mem_access_time);
		this.caches = new Cache[cacheLevels];
		for (int i = 0; i < this.caches.length; i++) {
			String [] cacheAttrs = cacheInfo[i].split(",");
			
			int cacheSize = Integer.parseInt(cacheAttrs[0]);
			int blockSize = Integer.parseInt(cacheAttrs[1]);
			int m = Integer.parseInt(cacheAttrs[2]);
			String writePolicyHit = cacheAttrs[3];
			String writePolicyMiss = cacheAttrs[4];
			int cycles = Integer.parseInt(cacheAttrs[5]);
			
			this.caches[i] = new Cache(cacheSize, blockSize, m, writePolicyHit, writePolicyMiss, cycles);
		}
	}
	

	public  int read(String address) {
		// Takes a binary address and returns corresponding value of address
		Block to_be_cached;
		for (int i = 0; i <= this.caches.length; i++) {
			if (i == this.caches.length) { 
				to_be_cached = read_block_from_memory(address);
				String indexBits = address.substring(this.caches[i-1].getTag(), 16 - this.caches[i-1].getOffset());
				writeBlock(to_be_cached, indexBits, i - 1); // write block to the level where it missed
				i = i-2;
			} else {
			if (caches[i].hitOrMiss(address)) {
				if (i == 0) {
					// Hit in the first level
					return caches[i].read(address);
				} else {
						to_be_cached = read_from_lower_level(i, address);	// Read block from lower level
					
						String indexBits = address.substring(this.caches[i-1].getTag(), 16 - this.caches[i-1].getOffset());
						writeBlock(to_be_cached, indexBits, i - 1); // write block to the level where it missed
						i = i-2;
				}
			} 
			}
		}
		return 0;
		
	}
	
	public Block read_from_lower_level(int i, String address) {
		// Reads a block from cache, corresponding to an address
		Cache current_cache = this.caches[i]; // The cache data is read from
		int target_block_size = this.caches[i-1].blockSize; //Size of block in the cache data will be written to
		int address_value = (int) Long.parseLong(address, 2); // Decimal value of the address of the target byte
		int address_pointer = address_value % target_block_size; // Index of the byte in the block to be returned
		int address_beg = address_value - address_pointer; // The address of the first byte in the block to be returned
		Block to_be_cached = new Block(target_block_size); // Block to be returned
		for(int j = 0; j < to_be_cached.bytes.length; j++) {
			String address_binary = convert_to_binary(j +address_beg); 
			to_be_cached.bytes[j] = current_cache.read(address_binary);
		}
		
		String tagBits = address.substring(0, this.caches[i-1].getTag());
		to_be_cached.tag = tagBits;
		to_be_cached.validBit = 1;
		return to_be_cached;
	}

	public Block read_block_from_memory(String address) {
		// Given an address, reads block from memory with size of the last level of cache
		int target_block_size = this.caches[this.caches.length - 1].blockSize; //Size of block in last level
		int address_value = (int) Long.parseLong(address, 2); // Decimal value of the address of the target byte
		int address_pointer = address_value % target_block_size; // Index of the byte in the block to be returned
		int address_beg = address_value - address_pointer; // The address of the first byte in the block to be returned
		Block to_be_cached = new Block(target_block_size); // Block to be returned
		for(int j = 0; j < to_be_cached.bytes.length; j++) {
			to_be_cached.bytes[j] = this.memory.ReadFromMemory(address_beg + j); // Read from memory byte by byte
		}
		
		String tagBits = address.substring(0, this.caches[this.caches.length-1].getTag());
		to_be_cached.tag = tagBits;
		to_be_cached.validBit = 1;
		return to_be_cached;
	}
	
	public void writeBlock(Block block, String indexBits, int cacheLevel) {
		// Writes a block in a given set, Using random replacement
		int index_value = (int) Long.parseLong(indexBits, 2);
		Set set = this.caches[cacheLevel].sets[index_value]; // Set where block should be written
		int i;
		for(i = 0; i < set.blocks.length; i++) {
			if (set.blocks[i].validBit == 0) {
				set.blocks[i] = block; // replace block if an invalid block is found
				return;
			}
		}
		
		int random_index_block = (int) (Math.random() * this.caches[cacheLevel].m);
		Block block_to_be_replaced = set.blocks[random_index_block];
		if (set.blocks[random_index_block].dirtyBit == 1) {
			// block address
			String block_to_be_replacded_addr = block_to_be_replaced.tag + indexBits + (mask(16-indexBits.length()-block_to_be_replaced.tag.length()));
			replaceBlock(i + 1, block_to_be_replacded_addr, set.blocks[random_index_block]);
		}
		set.blocks[random_index_block] = block;
		return;
	}
	
	public void replaceBlock(int cacheLevel, String block_address, Block block_to_be_replaced) {
		// Write the replaced dirty block in cacheLevel or memory
		Cache cache = this.caches[cacheLevel]; // Cache Level where block will e=be written to
		if(cacheLevel == this.caches.length - 1) { // If last level of cache
			int address_value = (int) Long.parseLong(block_address,2);
			for(int i = 0; i < this.caches[cacheLevel -1].blockSize; i++) { //Write block byte by byte to memory
				memory.WriteToMemory(address_value, block_to_be_replaced.bytes[i]);
				address_value++;
			}
			return;
		} else {
			String block_address_save = block_address;
		for(int i = 0; i < this.caches[cacheLevel -1].blockSize; i++) { // Write block byte by byte in cache
			cache.write(block_address, block_to_be_replaced.bytes[i]);
			int block_address_value = (int) Long.parseLong(block_address, 2);
			block_address_value++;
			block_address = convert_to_binary(block_address_value);
		}
		if (this.caches[cacheLevel].writePolicyHit.equals("writeBack"))
			return; // if Policy is writeBack, Stop writing through
		else
			replaceBlock(cacheLevel + 1, block_address_save, block_to_be_replaced);//Continue writing through
		}
		
	}


	
	public void write(String address, int data) {
		if (caches[0].hitOrMiss(address)) {
			write_to_cache(0, address, data);
		}
		else {
			read(address);
			write_to_cache(0, address, data);
		}
	}
	
	public void write_to_cache(int cache_level, String address, int data) {
		// Write data in cache, and continues writing through if cache's writePolicyHit is writeThrough
		if(cache_level == this.caches.length - 1) {
			int address_value = (int) Long.parseLong(address,2);
			memory.WriteToMemory(address_value, data);
			return;
		}
		this.caches[cache_level].write(address, data);
		if (this.caches[cache_level].writePolicyHit.equals("writeBack")) {
			return;
		}
		else {
			write_to_cache(cache_level + 1, address, data);
		}
	}
	
	public static String mask(int y) {
		String x ="";
		for(int i = 0; i < y; i++)
			x += '0';
		return x;
	}
	
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
	
	public  void printHierarchyInfo() {
		for (int i = 0; i < this.caches.length; i++) {
			System.out.println("Cache Level " + i +": ");
			this.caches[i].printCacheInfo();
			System.out.println("---------------------");
		}
	}
}
