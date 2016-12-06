package nyu.edu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Transaction {
	public Integer timeStamp; //Timestamp when transaction was created 
	public String id; //Transaction ID
	public Integer operations_remaining; //List of blocked operations
	
	//List of blocked Transaction indexes
	public HashMap<Integer, BlockNode> waitsFor;
	
	//List of Transactions we block: Transaction
	public HashMap<Integer, BlockNode> weBlock;
	
	//Variables written to
	public Set<Integer> readLocks;
	public Set<Integer> writeLocks;
	
	public boolean aborted;

	public Transaction(String id, Integer timeStamp) {
		this.id = id;
		this.timeStamp = timeStamp;
		
		aborted = false;
		
		operations_remaining = 0;
		
		waitsFor = new HashMap<>();
		weBlock = new HashMap<>();
		
		readLocks = new HashSet<>();
		writeLocks = new HashSet<>();
	}
}
