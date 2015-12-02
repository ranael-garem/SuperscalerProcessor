package memoryHierarchy;

public class Block {

	public String tag;
	public int validBit;
	int dirtyBit;
	public int [] bytes;

	public Block(int blockSize) {
		this.bytes = new int[blockSize];
        this.validBit = 0;

	}

}
