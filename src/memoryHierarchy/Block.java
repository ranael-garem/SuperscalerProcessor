package memoryHierarchy;

public class Block {

	public String tag;
	public int validBit;
	int dirtyBit;
	public String [] bytes;

	public Block(int blockSize) {
		this.bytes = new String[blockSize];
        this.validBit = 0;

	}

}
