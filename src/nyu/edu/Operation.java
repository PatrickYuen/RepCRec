package nyu.edu;

public class Operation {
	
	public String transactionId; //Transaction ID
	public Integer variable; //Variable to Access
	
	public Operation(
			String transactionId,
			String variable) {
		
		this.transactionId = transactionId;
		this.variable = Integer.parseInt(variable.substring(1)) - 1; //format(x16)
	}

}
