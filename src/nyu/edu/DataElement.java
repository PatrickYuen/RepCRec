package nyu.edu;

public class DataElement {
	public Integer lastCommitedVal; //Last Commited Value 
	public Integer value; //Current Value
	
	public DataElement(Integer value) {
		this.value = value;
		this.lastCommitedVal = value;
	}
	
	public boolean set(Integer value) {
		this.value = value;
		return true;
	}
}
