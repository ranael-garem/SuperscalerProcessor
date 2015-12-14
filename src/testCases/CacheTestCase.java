package testCases;
import memoryHierarchy.*;
public class CacheTestCase {

	public CacheTestCase() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Cache cache = new Cache(256, 4, 4, "writeBack", "writeAllocate", 3);
		System.out.println("Number of Blocks: " + cache.getNumberOfBlocks());
		System.out.println("Number of Sets: " + cache.getNumberOfSets());

		cache.sets[1].blocks[0].bytes[0] = "00000000";
		cache.sets[1].blocks[0].tag = "0000000000";
		cache.sets[1].blocks[0].validBit = 1;

		System.out.println(cache.hitOrMiss("0000000000000100"));
		cache.write("0000000000000100", "00000000");
		System.out.println(cache.read("0000000000000100"));
		cache.printCache();
		System.out.println(cache.hitOrMiss("1000000000000110"));

		System.out.println(cache.read("1000000000000110"));


	}

}
