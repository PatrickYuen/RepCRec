package nyu.edu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

public class LockManager {
	//Purpose: Manage/Assign Locks
	public int numVariables;
	public HashMap<Integer,HashSet<String>> readLocks; //variable => Transaction Index
	public HashMap<Integer,String> writeLocks; //variable => Transaction Index
	
	public LockManager(int numVariables) { //why do we need to have numVariables if not using it? have created and assigned one below
		this.numVariables = numVariables;
		readLocks = new HashMap<>();
		writeLocks = new HashMap<>();
	}

	public boolean writeLock(Operation op) {
		//Output: If can access write lock
		//Effects: Create Write lock if can access
		
		//TODO: Fix, can Write if transaction has only read lock
		//Unnecessary Double check
		if(
				(readLocks.containsKey(op.variable) && readLocks.get(op.variable).contains(op.transactionId) && readLocks.get(op.variable).size() == 1) 
				||
				(!readLocks.containsKey(op.variable) && !writeLocks.containsKey(op.variable))
			){
			
			//since there are no read or write locks currently on the variable in operation, the transaction can access a write lock on it
			writeLocks.put(op.variable,op.transactionId);
			return true;
		}
		else{
			//we need to put this transaction in wait queue and restrict any further read locks on this variable until
			//all the wait-listed write-lock requests have been served first
			return false;
		}
	}


	public boolean readLock(Operation op) {
		//Output: If can access read lock
		//Effects: Create read lock if can access
		if(! writeLocks.containsKey(op.variable)){	//one more check should come here which tests if there are zero write requests
			//means there are no write locks on the variable to access and we can assign the read locks

			if(! readLocks.containsKey(op.variable)) { //first read-lock on a variable
				readLocks.put(op.variable, new HashSet<String>(Arrays.asList(op.transactionId)));
			}
			else{ //already a read-lock exists on that variable
				(readLocks.get(op.variable)).add(op.transactionId);
			}
			return true;
		}
		else{
			//TODO: fix when accessing same lock
			if(writeLocks.containsKey(op.variable) && writeLocks.get(op.variable).equals(op.transactionId))
				return true;
			
			// we want to wait before assigning the read lock and hence return false as read lock can not be accessed by
			//the transaction in operation on the variable it wanted
			return false;
		}
	}


	public void unlock(Integer variable) {
		//Effects: Unlock lock at variable
		if(readLocks.containsKey(variable)){
			readLocks.remove(variable);
			//informAll(variable);
		}
		else if(writeLocks.containsKey(variable)){
			writeLocks.remove(variable);
			//informAll(variable);
		}
		else{
			//there is no lock on variable that could be removed
		}
	}

	public void unlockAll() { 
		//Effects: Unlock locks for all variables
		readLocks.clear();
		writeLocks.clear();
	}

	public boolean canWriteLock(WriteOperation op) {
		//TODO: Fix, can Write if transaction has only read lock
		if(
				(readLocks.containsKey(op.variable) && readLocks.get(op.variable).contains(op.transactionId) && readLocks.get(op.variable).size() == 1) 
				||
				(!readLocks.containsKey(op.variable) && !writeLocks.containsKey(op.variable))
			){
			//since there are no read or write locks currently on the variable in operation, the transaction can access a write lock on it
			return true;
		}
		else{
			//we need to put this transaction in wait queue and restrict any further read locks on this variable until
			//all the wait-listed write-lock requests have been served first
			return false;
		}
	}

	public boolean hasWriteLock(String id, Integer variable) {
		if(writeLocks.get(variable) == null)
			return false;
		return id.equals(writeLocks.get(variable));
	}
}
