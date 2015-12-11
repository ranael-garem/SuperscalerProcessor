package memoryHierarchy;


public class MemoryHierarchy {
	public Memory memory;
	public Cache[] caches;
	
	public MemoryHierarchy(int mem_access_time, int cacheLevels, String[] cacheInfo) {
		this.memory = new Memory(mem_access_time);
		this.caches = new Cache[cacheLevels + 1];
		int j = 0;
		for (int i = 0; i < cacheInfo.length; i++) {
			String [] cacheAttrs = cacheInfo[i].split(",");
			
			int cacheSize = Integer.parseInt(cacheAttrs[0]);
			int blockSize = Integer.parseInt(cacheAttrs[1]);
			int m = Integer.parseInt(cacheAttrs[2]);
			String writePolicyHit = cacheAttrs[3];
			String writePolicyMiss = cacheAttrs[4];
			int cycles = Integer.parseInt(cacheAttrs[5]);
			if(i == 0) {
				this.caches[j] = new Cache(cacheSize / 2, blockSize, m, writePolicyHit, writePolicyMiss, cycles); //Instruction Cache
				this.caches[j+1] = new Cache(cacheSize / 2, blockSize, m, writePolicyHit, writePolicyMiss, cycles); // Data Cache
				j += 2;
			}
			else {
				this.caches[j] = new Cache(cacheSize, blockSize, m, writePolicyHit, writePolicyMiss, cycles);
				j++;
			}
		}
	}
	
	public int loadCacheLevel(String address) {
		for(int i = 1; i <= this.caches.length; i++) {
			if (i == this.caches.length) {
				return i;
			}

			if(caches[i].hitOrMiss(address)) {
				System.out.println("HIT");
				caches[i].hits++;
				return i;
			}
			else {
				caches[i].misses++;
			}
		}
		return -1;
	}
	
	public String loadValue(String address) {
		this.caches[1].load_cycles_left--;
		if(this.caches[1].load_cycles_left == 0) {
			this.caches[1].being_accessed = false;
			this.caches[1].load_cycles_left = this.caches[1].cycles;
			return caches[1].read(address);
		}
		else {
			this.caches[1].being_accessed = true;
			return null;
		}
	}
	
	public int cacheCyclesLeft(int cacheLevel, String address) {
		if(cacheLevel == this.caches.length) { //Memory
			this.memory.load_cycles_left--;
			this.memory.total_cycles++;
			if(this.memory.load_cycles_left == 0) {
				this.memory.being_accessed = false;
				this.memory.load_cycles_left = this.memory.access_time;
				Block to_be_cached = read_block_from_memory(address);
				String indexBits = address.substring(this.caches[cacheLevel-1].getTag(), 16 - this.caches[cacheLevel-1].getOffset());
				writeBlock(to_be_cached, indexBits, cacheLevel - 1); // write block to the level where it missed
				return 0;
			}else {
				this.memory.being_accessed = true;
				return this.memory.load_cycles_left;
			}
		}
		this.caches[cacheLevel].load_cycles_left--;
		if(this.caches[cacheLevel].load_cycles_left == 0) {
			this.caches[cacheLevel].being_accessed = false;
			this.caches[cacheLevel].load_cycles_left = this.caches[cacheLevel].cycles;
			Block to_be_cached = read_from_lower_level(cacheLevel, address, false);	// Read block from lower level
			
			String indexBits = address.substring(this.caches[cacheLevel-1].getTag(), 16 - this.caches[cacheLevel-1].getOffset());
			writeBlock(to_be_cached, indexBits, cacheLevel - 1); // write block to the level where it missed
			return 0;
			
		}
		else {
			this.caches[cacheLevel].being_accessed = true;
			return this.caches[cacheLevel].load_cycles_left;
		}
	}
	
	public String temp(String address) {
		for(int i = 0; i <= this.caches.length; i++) {
			if(i == 1)
				i = 2;
			if (i == this.caches.length) {
				this.memory.total_cycles++;
				this.memory.fetch_cycles_left--;
				if(this.memory.fetch_cycles_left == 0) {
					this.memory.fetch_cycles_left = this.memory.access_time;
					resetCaches();
					return read_instruction(address);
				}
				return null;
			}
			if(!caches[i].fetch_accessed) {
				caches[i].fetch_cycles_left--;
				if(caches[i].fetch_cycles_left == 0) {
					caches[i].fetch_accessed = true;
					caches[i].fetch_cycles_left = caches[i].cycles;
					if(caches[i].hitOrMiss(address)) {
						caches[i].hits++;
						resetCaches();
						return read_instruction(address);
					}
					else {
						caches[i].misses++;
					}
				}
				else {
					return null;
				}
			}
		}
		return null;
	}
	
	
	private void resetCaches() {
		for(int i = 0; i < this.caches.length; i++) {
			this.caches[i].fetch_accessed = false;
			this.caches[i].fetch_cycles_left = this.caches[i].cycles;
		}
	}

	
	public int load_cycles_left(String address) {
		int cycles = 0;
		for (int i = 1; i <= this.caches.length; i++) {
				if(i == this.caches.length) {
					cycles += this.memory.access_time;
					break;
				}
					
				if (caches[i].hitOrMiss(address)) {
					cycles += caches[i].cycles;
//					System.out.println("cache " + this.fetch_cycles_left);
					break;
				}
				else {
					cycles += caches[i].cycles;
				}
			}
		return cycles;
	}
	
	public int store_cycles_left(String address) {
		int cycles = load_cycles_left(address); // Write misses cycles
		for (int i = 1; i <= this.caches.length; i++) {
			if(i == this.caches.length) {
				cycles += this.memory.access_time;
				break;
			}
				
			if (caches[i].writePolicyHit.toLowerCase().equals("writeBack")) {
				cycles += caches[i].cycles;
				break;
			}
			else {
				cycles += caches[i].cycles;
			}
		}
	return cycles;
	}
	
	public  String read_instruction(String address) {
		// Takes a binary address and returns corresponding value of address
		Block to_be_cached;
		int k;
		
		for (int i = 0; i <= this.caches.length; i++) {
			if((i - 1) == 1 )
				k = 0;
			else
				k = i - 1;
			
			if (i != 1) { // Skip Data cache
				if (i == this.caches.length) { 
					to_be_cached = read_block_from_memory(address);
					String indexBits = address.substring(this.caches[k].getTag(), 16 - this.caches[k].getOffset());
					writeBlock(to_be_cached, indexBits, k); // write block to the level where it missed
					i = i-2;
					if(i == 0)
						i = -1;
				} else {
				if (caches[i].hitOrMiss(address)) {
					if (i == 0) {
						// Hit in the first level
						return caches[i].read(address);
					} else {
							to_be_cached = read_from_lower_level(i, address, true);	// Read block from lower level
						
							String indexBits = address.substring(this.caches[k].getTag(), 16 - this.caches[k].getOffset());
							writeBlock(to_be_cached, indexBits, k); // write block to the level where it missed
							i = i-2;
							if(i == 0)
								i = -1;
						}
					}
				}
			}
		}
		return null;
	}

	public  String read_data(String address) {
		// Takes a binary address and returns corresponding value of address
		Block to_be_cached;
		boolean flag = false;

		for (int i = 1; i <= this.caches.length; i++) {
			if (i == this.caches.length) { 
				to_be_cached = read_block_from_memory(address);
				String indexBits = address.substring(this.caches[i-1].getTag(), 16 - this.caches[i-1].getOffset());
				writeBlock(to_be_cached, indexBits, i - 1); // write block to the level where it missed
				i = i-2;
				flag = true;
			} else {
			if (caches[i].hitOrMiss(address)) {
				if(!flag) {
					caches[i].hits++;
					flag = true;
				}
				if (i == 1) {
					// Hit in the first level
					return caches[i].read(address);
				} else {
						to_be_cached = read_from_lower_level(i, address, false);	// Read block from lower level
					
						String indexBits = address.substring(this.caches[i-1].getTag(), 16 - this.caches[i-1].getOffset());
						writeBlock(to_be_cached, indexBits, i - 1); // write block to the level where it missed
						i = i-2;
					}
				}  else {
					if(!flag)
						caches[i].misses++;
				}
			}
		}
		return null;
	}
	
	public Block read_from_lower_level(int i, String address, boolean instructionOrNot) {
		//boolean instructionOrNot ==> Reading an instruction
		// Reads a block from cache, corresponding to an address
		Cache current_cache = this.caches[i]; // The cache data is read from
		int target_block_size;

		if (instructionOrNot && i == 2)
			target_block_size = this.caches[0].blockSize; //Size of block in the cache data will be written to
		else
			target_block_size = this.caches[i - 1].blockSize;
		
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
		int index_value;
		if(indexBits.equals(""))
			index_value = 0;
		else
		   index_value = (int) Long.parseLong(indexBits, 2);
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
			// block address: First byte of the block to be replaced
			String block_to_be_replacded_addr = block_to_be_replaced.tag + indexBits + (mask(16-indexBits.length()-block_to_be_replaced.tag.length()));
			if(cacheLevel == 0)
				replaceBlock(cacheLevel + 2, block_to_be_replacded_addr, set.blocks[random_index_block]);
			else 
				replaceBlock(cacheLevel + 1, block_to_be_replacded_addr, set.blocks[random_index_block]);

		}
		set.blocks[random_index_block] = block;
		return;
	}
	
	public void replaceBlock(int cacheLevel, String block_address, Block block_to_be_replaced) {
		// Write the replaced dirty block in cacheLevel or memory
		if(cacheLevel == this.caches.length) { // If last level of cache
			int address_value = (int) Long.parseLong(block_address,2);
			for(int i = 0; i < this.caches[cacheLevel -1].blockSize; i++) { //Write block byte by byte to memory
				memory.WriteToMemory(address_value, block_to_be_replaced.bytes[i]);
				address_value++;
			}
			return;
		} else {
			Cache cache = this.caches[cacheLevel]; // Cache Level where block will e=be written to

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

	
	public void write(String address, String data) {
		if (caches[1].hitOrMiss(address)) {
			caches[1].hits++;
			write_to_cache(1, address, data);
		}
		else {
			read_data(address);
			write_to_cache(1, address, data);
		}
	}
	
	public void write_to_cache(int cache_level, String address, String data) {
		// Write data in cache, and continues writing through if cache's writePolicyHit is writeThrough
		if(cache_level == this.caches.length) {
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
	
	public void printCacheAccesses() {
		for(int i = 0; i < this.caches.length; i++) {
			System.out.println("Cache " + i +": Accesses: " + (caches[i].hits + caches[i].misses) + ", Misses: " + caches[i].misses);
		}
	}


	public int fetchCacheLevel(String address) {
		// TODO Auto-generated method stub
		return 0;
	}
}
