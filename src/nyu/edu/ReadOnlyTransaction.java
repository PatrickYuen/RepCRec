package nyu.edu;

public class ReadOnlyTransaction extends Transaction{
	private Integer[] commitedValues; //Copy of all commited values
	
	public ReadOnlyTransaction(String id, Integer timeStamp) {
		super(id, timeStamp);
	}
	
	public Integer read(ReadOperation op) {
		return commitedValues[op.variable];
	}
	
	public void populate(Integer[] readValues) {
		commitedValues = readValues;
	}
}
