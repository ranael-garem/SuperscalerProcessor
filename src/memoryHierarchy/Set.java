package memoryHierarchy;


public class Set {
	
	public Block[] blocks; // Blocks in a Set
	
	public Set(int numberOfBlocksInSet, int blockSize) {
		this.blocks = new Block[numberOfBlocksInSet];
		
		for(int i = 0; i < this.blocks.length; i++) 
			this.blocks[i] = new Block(blockSize);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
