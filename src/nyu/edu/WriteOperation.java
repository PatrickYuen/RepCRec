package nyu.edu;

public class WriteOperation extends Operation {
	public Integer value;
	
	public WriteOperation(
			String transactionId,
			String variable,
			Integer value) {
		super(transactionId, variable);
		this.value = value;
	}
}
