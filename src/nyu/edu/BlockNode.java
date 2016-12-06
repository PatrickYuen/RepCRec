package nyu.edu;

public class BlockNode {
	public String transactionId;
	public boolean write;
	
	public BlockNode(String transactionId, boolean write) {
		this.transactionId = transactionId;
		this.write = write;
	}
}
