package nyu.edu;

public class Result {
	public Boolean success; //If result was successful
	public Boolean deadlock; //If operation created a deadlock
	public String output; //TODO: Reason for abortion
	
	public Result() {
		this.success = false;
		this.deadlock = false;
	}
	
	public Result(Boolean success, Boolean deadlock) {
		this.success = success;
		this.deadlock = deadlock;
	}
	
	public Result(Boolean success, Boolean deadlock, String output) {
		this.success = success;
		this.deadlock = deadlock;
		this.output = output;
	}
}
