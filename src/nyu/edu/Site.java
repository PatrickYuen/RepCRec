package nyu.edu;

public class Site {
	//Purpose: Object representation of each Site
	public int id;

	private LockManager lockManager; //Creates Locks on Variables:
	private DataManager dataManager; //Interface for Writes/Reads Data
	
	public Boolean recovery; //If replicated variable, cannot read but can write if true
	public Boolean failed; //deny all operations
	
	public Site(int id, int numVariables) {
		this.id = id;
		
		lockManager = new LockManager(numVariables);
		dataManager = new DataManager(numVariables);
		
		recovery = false;
		failed = false;
	}

	public void fail() {
		//Effects: set fail flag, Clear all locks and values
		failed = true;
		lockManager.unlockAll();
	}
	
	public void recover() {
		failed = false;
		recovery = true;
	}

	public LockManager getLockManager() {
		if(!failed)
			return lockManager;
		return null;
	}
	
	public DataManager getDataManager() {
		if(!failed)
			return dataManager;
		return null;
	}


}
