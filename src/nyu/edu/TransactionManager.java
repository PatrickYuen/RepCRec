package nyu.edu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

public class TransactionManager {
	//Purpose: Create new Transactions, execute operations, and synchronize output from each site

	private Site[] sites; //Array of all 10 Sites
	private Integer time; //Current Time Tick
	private HashMap<String, Transaction> transactions; //List of all Current Transactions
	private Integer numVariables;
	
	//Main Queue to run
	private List<Queue<Operation>> waitingOperations; //index = variable => queue
	
	//Blocking
	private HashMap<Integer, BlockNode> lastTr; //variable => Transaction ID of last Operation
	private HashMap<Integer, BlockNode> prevWrite; //variable => Transaction ID of last write
	
	//Buffer of Results for dump: 
	//ie: T1 read 20 from x3, T2 wrote 30 to x6, T3 aborted, T4 committed
	StringBuilder results;
	
	public boolean cmdline = false;
	
	/*
	 * Initial Transaction Manager
	 */
	public TransactionManager(int numSites, int numVar) {
		time = 0;
		sites = new Site[numSites];
		numVariables = numVar;
		
		//Initialize sites
		for(int currSite = 0 ; currSite < sites.length; currSite++) {
			sites[currSite] = new Site(currSite + 1, numVariables);
		}
		
		transactions = new HashMap<>();
		waitingOperations = new ArrayList<>();
		
		lastTr = new HashMap<>();
		prevWrite = new HashMap<>();
		
		//Create Each queue 
		for(int currVar = 0 ; currVar < numVariables; currVar++) {
			//Initialize variables
			for(Site site :getSites(currVar))
					site.getDataManager().init(currVar, 10 * (currVar + 1));
			waitingOperations.add(new LinkedList<>());
		}
		
		results = new StringBuilder();
	}
	
	/*
	 * Input: Command: R(T1,x2); W(T2,x2); 
	 * Output: NA
	 * Effects: Run each step individually	
	 */
	public void runAllSteps(String input) {
		for(String command: input.split(";")) {
			execute(command.trim());
		}
		
		time++;
	}

	/*
	 * Input: Command: R(T1,x2)
	 * Output: NA
	 * Effects: Execute each step
	 */
	public void execute(String input) {
		//Split by parenthesis or comma
		String[] parsedCommand = input.split("\\(|\\)|,");
		
		try {
			//Check for Tests
			if(input.startsWith("assert")) {
				if(parsedCommand[0].endsWith("commit"))
					assertCommitted(parsedCommand[1]);
				else if(parsedCommand[0].endsWith("abort"))
					assertAborted(parsedCommand[1]);
				else if(parsedCommand[0].endsWith("read"))
					assertRead(parsedCommand[1], parsedCommand[2], Integer.parseInt(parsedCommand[3]));
				else if(parsedCommand[0].endsWith("write"))
					assertWrite(parsedCommand[1], parsedCommand[2], Integer.parseInt(parsedCommand[3]));
				
				return;
			}
			
			//Create Transaction
			else if(input.startsWith("beginro")) {
				//Build Read only Operation
				ReadOnlyTransaction roTr = new ReadOnlyTransaction(
						parsedCommand[1],
						time);
				copyVariables(roTr);
				
				transactions.put( parsedCommand[1], roTr);
	
				if(cmdline)
					System.out.println("T: " + time + " Created Read Only Transaction " + parsedCommand[1]);
				else
					results.append("T: ").append(time).append(" Created Read Only Transaction ").append(parsedCommand[1]).append("\n");
				
				return;
			//Out of control Events
			} else if(input.startsWith("begin")) { //Create New Operation
				transactions.put( parsedCommand[1],
						new Transaction(
								parsedCommand[1],
								time));
				
				if(cmdline)
					System.out.println("T: " + time + " Created Transaction " + parsedCommand[1]);
				else
					results.append("T: ").append(time).append(" Created Transaction ").append(parsedCommand[1]).append("\n");
				
				return;
			} else if(input.startsWith("fail")) { //fail
				
				fail(Integer.parseInt(parsedCommand[1]));
				return;
				
			} else if(input.startsWith("recover")) { //recovery
				
				recover(Integer.parseInt(parsedCommand[1]));
				return;
				
			} else if(input.startsWith("dump")) { //dump
				
				if(parsedCommand.length == 1) {
					if(cmdline) {
						System.out.println("Vars:      x1   x2   x3   x4   x5   x6   x7   x8   x9  x10  x11  x12  x13  x14  x15  x16  x17  x18  x19  x20");
						System.out.print(dump());
					} else {
						results.append("Vars:      x1   x2   x3   x4   x5   x6   x7   x8   x9  x10  x11  x12  x13  x14  x15  x16  x17  x18  x19  x20\n").append(dump());
					}
				} else if(parsedCommand[1].startsWith("x")) {//variable
					if(cmdline) {
						System.out.print(dump(parsedCommand[1]));
					} else {
						results.append(dump(parsedCommand[1]));
					}
				} else { //Site
					if(cmdline) {
						System.out.println("Vars:      x1   x2   x3   x4   x5   x6   x7   x8   x9  x10  x11  x12  x13  x14  x15  x16  x17  x18  x19  x20");
						System.out.println(dump(Integer.parseInt(parsedCommand[1])));
					} else {
						results.append("Vars:      x1   x2   x3   x4   x5   x6   x7   x8   x9  x10  x11  x12  x13  x14  x15  x16  x17  x18  x19  x20\n").append(dump(Integer.parseInt(parsedCommand[1]))).append("\n");
					}
				}
				return;
				
			}
			
			//Ignore if transaction already aborted
			if(transactions.get(parsedCommand[1]).aborted)
				return;
			
			Result res = null;
			Operation currOp = null;
			
			//Create Operation
			if(input.startsWith("r")){ //Read Operation
				
				currOp = new ReadOperation(	parsedCommand[1].trim(), //Transaction ID
						parsedCommand[2].trim()  //Variable
					 );
				
				res = read( (ReadOperation) currOp , transactions.get(parsedCommand[1].trim()));
				
			} else if(input.startsWith("w")) { //Write Operations
				
				currOp = new WriteOperation(	parsedCommand[1].trim(), //Transaction ID
						parsedCommand[2].trim(), //Variable
						Integer.parseInt(parsedCommand[3].trim()) //Value to write
						);
				
				res = write( (WriteOperation) currOp , transactions.get(parsedCommand[1]));
				
			} else if(input.startsWith("end")) { //commit
				
				commit(transactions.get(parsedCommand[1].trim()));
				
				return;
			} else { //Invalid Operation
				System.out.println("Invalid Command: " + input);
				return;
			}
			
			//Verify if not success
			if(!res.success) {
				
				//Place in queue if not successful
				transactions.get(currOp.transactionId).operations_remaining++;
				waitingOperations.get(currOp.variable).add(currOp);
				
				//Build WaitsFor with prevWrite
				BlockNode prevBlockingNode = null;
				boolean write = false;
				
				//Last Blocking transaction
				if(currOp instanceof  WriteOperation) {

					prevBlockingNode = lastTr.get(currOp.variable);
					write = true;
					
				} else if(currOp instanceof  ReadOperation) {
					prevBlockingNode = prevWrite.get(currOp.variable);
				}
				
				//If new Waits-For Connections is created
				if(prevBlockingNode != null && 
						//Should not have the same transaction id
						!prevBlockingNode.transactionId.equals(currOp.transactionId)) {
					
					//Waits For
					transactions.get(currOp.transactionId).waitsFor.put( currOp.variable,
							prevBlockingNode
							);
					
					//We Block
					transactions.get(prevBlockingNode.transactionId).weBlock.put( currOp.variable,
							new BlockNode(currOp.transactionId, write)
							);
					
					//Check if Deadlock
					Transaction youngestDeadlockTrans = createsDeadLock(currOp);
					if(youngestDeadlockTrans != null) {
						abort(youngestDeadlockTrans);
					}
				}
				
			}
			
			//Populate last Transaction
			if(currOp != null) {
				if(currOp instanceof WriteOperation) {
					BlockNode currBlock = new BlockNode(currOp.transactionId, true);
					
					//Populate Last Transaction
					lastTr.put(currOp.variable , currBlock );
					
					//Populate Last Write for WaitsFor
					prevWrite.put(currOp.variable , currBlock );
				} else if(currOp instanceof  ReadOperation) {
					//Populate Last Transaction
					lastTr.put(currOp.variable , new BlockNode(currOp.transactionId, false));
				}
			}
			
		} catch (Exception e) {
			//Testing
			e.printStackTrace();
			
			if(cmdline)
				System.out.println("Malformed Command: " + input);
			else
				results.append("Malformed Command: ").append(input).append("\n");
		}
	}
	
	/*
	 * Input: ReadOnlyTransaction
	 * Output: Result of Operation
	 * Effects: Populate Transaction with copies of committed values
	 */
	private void copyVariables(ReadOnlyTransaction roTr) {
		Integer[] readVariables = new Integer[numVariables];
		
		for(int var = 0; var < readVariables.length; var++) {
			for(Site site : getSites(var)) {
				//First Available Site
				if(site.recovery && (var + 1) % 2 == 0)
					continue;
				
				readVariables[var] = site.getDataManager().committedRead(var);
				break;
			}
		}
		
		roTr.populate(readVariables);
	}

	/*
	 * Input: Operation and corresponding Transaction
	 * Output: Result of Operation
	 * Effects: Attempt to execute operation: Read first available
	 */
	private Result read(ReadOperation op, Transaction tr) {		
		Result res = new Result();
		Integer readValue = null;
		String siteInfo = "";
		
		//RO Operation: Fail the site on a read only operation?
		if(tr instanceof ReadOnlyTransaction) {
			
			readValue = ((ReadOnlyTransaction) tr).read(op);
			if(readValue == null)
				return res;
			
		} else { //Acquire Read Lock	
			//Get Lock
			Site firstAvailibleSite = null;
			
			for(Site site : getSites(op.variable)) {
				if(	//For recovery: if replicated and recovering, cannot read: Opposite
					!(site.recovery && op.variable % 2 == 1) &&
					site.getLockManager().readLock(op)) {
					
					firstAvailibleSite = site;
					break;
				}
			}
			
			//DataManager: 
			if(	firstAvailibleSite != null ) {
				readValue = firstAvailibleSite.getDataManager().read(op.variable);
				
			} else {
				return res;
			}
			
			siteInfo = " on site " + firstAvailibleSite.id;
	
		}
		
		tr.readLocks.add(op.variable);
		res.success = true;
		if(cmdline) {
			System.out.println("T: " + time + " Transaction " + tr.id  + " read " + readValue + 
					" from x" + (op.variable + 1) + siteInfo);
		} else {
			results.append("T: ").append(time).append(" Transaction ").append(tr.id).append(" read ")
					.append(readValue).append(" from x").append(op.variable + 1).append(siteInfo).append("\n");
		}
		return res;
	}

	/*
	 * Input: Operation and corresponding Transaction
	 * Output: Result of Operation
	 * Effects: Request locks and attempt to execute operation
	 */
	private Result write(WriteOperation op, Transaction tr) {	
		Result res = new Result();
		
		//Get Lock
		Boolean canWrite = true;
		
		//Check if I can lock all of them
		for(Site site : getSites(op.variable)) {
			//First Unavailable Site
			if(!site.getLockManager().canWriteLock(op)) {
				canWrite = false;
				break;
			}
		}
		
		//DataManager Write to All Sites
		if(canWrite) {
			for(Site site : getSites(op.variable)) {
				site.getLockManager().writeLock(op);
				site.getDataManager().write(op);
			}
			
			tr.writeLocks.add(op.variable);
			res.success = true;
			if(cmdline) {
				System.out.println("T: " + time + " Transaction " + tr.id  + " wrote " + op.value + 
						" to x" + (op.variable + 1));
			} else {
				results.append("T: ").append(time).append(" Transaction ").append(tr.id).append(" wrote ")
					.append(op.value).append(" to x").append(op.variable + 1).append("\n");
			}
		}
		
		return res;
	}
	
	/*
	 * Input: Operation 
	 * Output: List of Corresponding Sites to variable
	 * Effects: NA
	 */
	private List<Site> getSites(Integer variable) {
		List<Site> availibleSites = new ArrayList<>();
		
		//Get 1 Site: Because everything is shifted, everything is the opposite
		if(variable % 2 == 0) { // Even var is really odd var
			if(!sites[(variable + 1) % 10].failed)
				availibleSites.add(sites[(variable + 1) % 10]);
		} else { //Get Replicated Sites
			for(Site site: sites) {
				if(!site.failed)
					availibleSites.add(site);
			}
		}
		
		return availibleSites;
	}

	/*
	 * Input: Site Number
	 * Output: NA
	 * Effects: Abort every transaction that has a write lock in that site and fail site
	 */
	private void fail(Integer siteNum) {
		if(cmdline) {
			System.out.println("T: " + time + " Site " + siteNum + " went down");
		} else {
			results.append("T: ").append(time).append(" Site ").append(siteNum).append(" went down\n");
		}
		
		//Abort Read Locks
		for(HashSet<String> trs : sites[siteNum - 1].getLockManager().readLocks.values()) {
			for(String tr : trs) {
				if(!transactions.get(tr).aborted) {
					abort(transactions.get(tr));
				}
			}
		}
		
		//Abort Write Locks 
		for(String tr : sites[siteNum - 1].getLockManager().writeLocks.values()) {
			abort(transactions.get(tr));
		}
		
		sites[siteNum - 1].fail();
		
	}
	
	/*
	 * Input: Transaction
	 * Output: NA
	 * Effect: Goto each variable you have a write lock on a reset value to last committed value, unlock each lock 
	 */
	private void recover(Integer site) {
		sites[site - 1].recover();

		//Reset Values
		sites[site - 1].getDataManager().resetAll();
		
		if(cmdline) {
			System.out.println("T: " + time + " Site " + site + " came back up");
		} else {
			results.append("T: ").append(time).append(" Site ").append(site).append(" came back up\n");
		}

		//Run waiting operations for all variables
		for(Integer variable: sites[site - 1].getDataManager().variablesStored()) {
			updateWaitingOps(variable);
		}
	}
	
	/*
	 * Input: NA
	 * Output: NA
	 * Effects: If can acquire lock, execute all operation in each queue
	 */
	private void updateWaitingOps(int variable) {
		//Execute waiting operations: ignore if transaction was aborted
		Queue<Operation> varQueue = waitingOperations.get(variable); 
		while(varQueue.size() > 0) {
			Operation op = (Operation) varQueue.peek();
			Transaction tr = transactions.get(op.transactionId);
			
			//if Aborted
			if(tr.aborted) {
				varQueue.poll();
			} else 
				
			//or can execute properly
			if(op instanceof ReadOperation) {
				if(!read((ReadOperation) op, tr).success) {
					break;
				}
			} else if(op instanceof WriteOperation) {
				if(!write((WriteOperation) op, tr).success) {
					break;
				}
			}
			
			tr.operations_remaining--;
			varQueue.poll();
		} 
	}
	
	/*
	 * Input: Transaction
	 * Output: NA
	 * Effect: Goto each variable you have a write lock on a reset value to last committed value, unlock each lock 
	 */
	private void abort(Transaction tr) {
		tr.aborted = true;
		
		//Update LastTR
		 Iterator<Entry<Integer, BlockNode>> it = lastTr.entrySet().iterator();
		 while (it.hasNext()) {
			 Entry<Integer, BlockNode> pair = it.next();
			 if((pair.getValue().transactionId).equals(tr.id)) {
				 BlockNode waitsForTrans = tr.waitsFor.get(pair.getKey());
				 if(waitsForTrans == null) //This means you have the lock
					 it.remove(); 
				 else
					 pair.setValue(waitsForTrans);
		     }
		 }
		
		 //Update PrevWrite
		 it = prevWrite.entrySet().iterator();
		 
		 while (it.hasNext()) {
			 Entry<Integer, BlockNode> pair = it.next();
			 if((pair.getValue().transactionId).equals(tr.id)) {
				 
				 //Tough Implementation
				 BlockNode waitsForTrans = tr.waitsFor.get(pair.getKey());
				 while(waitsForTrans != null && !waitsForTrans.write) {
					 waitsForTrans = transactions.get(waitsForTrans.transactionId).waitsFor.get(pair.getKey());
				 }
				 
				 if(waitsForTrans == null) //This means you have the lock
					 it.remove(); 
				 else
					 pair.setValue(waitsForTrans);
				 
		     }
		 }
		
		//Unblock transactions waiting for me
		for(Integer blockVar : tr.weBlock.keySet()) {
			
			String transId = tr.weBlock.get(blockVar).transactionId;
			transactions.get(transId).waitsFor.remove(blockVar);
			
			//Merge
			if(tr.waitsFor.containsKey(blockVar))
				transactions.get(transId).waitsFor.put(blockVar, 
						tr.waitsFor.get(blockVar));
				
		}
		
		tr.weBlock.clear();
		tr.waitsFor.clear();
		
		if(cmdline) {
			System.out.println("T: " + time + " Abort Transaction " + tr.id);
		} else {
			results.append("T: ").append(time).append(" Abort Transaction ").append(tr.id).append("\n");
		}
		
		//Read Locks
		for(Integer variable: tr.readLocks) {
			for(Site site : getSites(variable)) {
				site.getLockManager().unlock(variable);
			}
			updateWaitingOps(variable);
		}

		//Write Locks
		for(Integer variable: tr.writeLocks) {
			for(Site site : getSites(variable)) {
				site.getDataManager().reset(variable);
				site.getLockManager().unlock(variable);
			}
			updateWaitingOps(variable);
		}
	}
	
	/*
	 * Input: Transaction
	 * Output: NA
	 * Effects: Goto each variable you’ve written to and write value to committed value, unlock each lock
	 */
	private void commit(Transaction tr) {
		//Abort transaction if still waiting operations
		if(tr.operations_remaining != 0){
			if(cmdline)
				System.out.println("Because of waiting operations that haven't executed, Transaction " + tr.id + "will abort.");
			else
				results.append("Because of waiting operations that haven't executed, Transaction " + tr.id + "will abort.\n");
			abort(tr);
			return;
		}
		
		//Update PrevWrite and LastTR
		 Iterator<Entry<Integer, BlockNode>> it = lastTr.entrySet().iterator();
		 while (it.hasNext()) {
			 Entry<Integer, BlockNode> pair = it.next();
			 if((pair.getValue().transactionId).equals(tr.id)) {
		    	 it.remove(); 
		     }
		 }
		
		 it = prevWrite.entrySet().iterator();
		 while (it.hasNext()) {
			 Entry<Integer, BlockNode> pair = it.next();
			 if((pair.getValue().transactionId).equals(tr.id)) {
		    	 it.remove(); 
		     }
		 }

		//Unblock transactions waiting for me
		for(Integer blockVar : tr.weBlock.keySet()) {
			transactions.get(tr.weBlock.get(blockVar).transactionId).waitsFor.remove(blockVar);
		}
		
		tr.weBlock.clear();
		tr.waitsFor.clear();
		
		if(cmdline)
			System.out.println("T: " + time + " Commit Transaction " + tr.id);
		else
			results.append("T: ").append(time).append(" Commit Transaction ").append(tr.id).append("\n");
		
		//Read Locks
		for(Integer variable: tr.readLocks) {
			for(Site site : getSites(variable)) {
				site.getLockManager().unlock(variable);
			}
			updateWaitingOps(variable);
		}
		
		//Write Locks
		for(Integer variable: tr.writeLocks) {
			for(Site site : getSites(variable)) {
				if(site.getLockManager().hasWriteLock(tr.id, variable)) {
					site.recovery = false;
					site.getDataManager().commit(variable);
					site.getLockManager().unlock(variable);
				}
			}
			updateWaitingOps(variable);
		}
	}
	
	/*
	 * Input: Operation 
	 * Output: Null or list of transactions
	 * Effects: DFS on transactions in internal waitsFor data structure to find all transactions involved in cycle
	 */
	private Transaction createsDeadLock(Operation op) { 
		
		LinkedHashSet<String> cycle = new LinkedHashSet<>();
		
		if(cycle( op.transactionId , cycle )) {
			//Find youngest
			Transaction youngestTr = null;
			for(String transId : cycle) {
				if(transactions.get(transId).aborted)
					continue;
				
				if(youngestTr == null)
					youngestTr = transactions.get(transId);
				else {
					if(youngestTr.timeStamp < transactions.get(transId).timeStamp) {
						youngestTr = transactions.get(transId);
					}
				}
			}
			if(cmdline)
				System.out.println("Deadlock found.");
			else
				results.append("Deadlock found.\n");
			return youngestTr;
		}

		return null;
	}
	
	//DFS to find first cycle:
	private boolean cycle( String trId, LinkedHashSet<String> visitedTr) {

		//There is cycle
		if(visitedTr.contains(trId)) {
			//Remove everything not in cycle
			Iterator<String> itr = visitedTr.iterator();
			while(itr.hasNext() && !(itr.next()).equals(trId)) {
				itr.remove();
			}
			
			return true;
		}
		
		visitedTr.add(trId);

		for(BlockNode trWaitsFor : transactions.get(trId).waitsFor.values()) {
			if(cycle(trWaitsFor.transactionId , visitedTr))
				return true;
		}
		
		visitedTr.remove(trId);
		
		return false;
	}
	
	/*
	 * Input: NA
	 * Output: NA
	 * Effects: Output all DB state to STDOUT
	 */
	private String dump() {
		StringBuilder buf = new StringBuilder();
		for(int site = 1; site <= sites.length; site++) {
			buf.append(dump(site)).append("\n");
		}
		return buf.toString();
	}
	
	/*
	 * Input: NA
	 * Output: NA
	 * Effects: Output site state to STDOUT
	 */
	private String dump(int site) {
		if(sites[site - 1].failed)
			return "Site " + (site) + ": Failed";
		return "Site " + (site) + ": " + sites[site - 1].getDataManager().dumpAll();
	}

	/*
	 * Input: NA
	 * Output: NA
	 * Effects: Output variable state to STDOUT
	 */
	private String dump(String variable) {
		int var = Integer.parseInt(variable.substring(1)) - 1;
		
		StringBuilder buf = new StringBuilder(variable);
		buf.append("\n");
		for(Site site: getSites(var)) {
			buf.append("Site ").append(site.id).append(": ")
			.append(site.getDataManager().dump(var)).append("\n");
		}
		return buf.toString();
	}
	
	public String showStatus() {
		StringBuilder status = new StringBuilder(results.toString());
		status.append("\n");
		
		for(Transaction tr : transactions.values()) {
			status.append("Transaction ").append(tr.id).append(": ").append(tr.aborted? "Aborted\n":"Committed\n");
		}
		
		return status.toString();
	}
	
	//Testing Functions: All just print to standard output result of test
	
	/*Print to STDOUT if Transaction <transactionId> committed
	 *input: String transactionId
	 *output: NA
	 */
	public void assertCommitted(String transactionId) {
		System.out.println("Test Transaction " + transactionId + " Committed: " + !transactions.get(transactionId).aborted);
	}
	
	/*Print to STDOUT if Transaction <transactionId> aborted
	 *input: String transactionId
	 *output: NA
	 */
	public void assertAborted(String transactionId) {
		System.out.println("Test Transaction " + transactionId + " Aborted: " + transactions.get(transactionId).aborted);
	}
	
	/*Print to STDOUT if Transaction <transactionId> read <VALUE> from <VARIABLE>
	 *input: String transactionId, String variable, Integer value
	 *output: NA
	 */
	public void assertRead(String transactionId, String variable, Integer value) {
		ReadOperation rOp = new ReadOperation(	transactionId.trim(), //Transaction ID
				variable.trim()  //Variable
			 );
		Transaction tr = transactions.get(transactionId);
		Integer readValue = null;
		
		//RO Operation: Fail the site on a read only operation?
		if(tr instanceof ReadOnlyTransaction) {
			
			readValue = ((ReadOnlyTransaction) tr).read(rOp);
			
		} else { //Acquire Read Lock	
			//Get Lock
			Site firstAvailibleSite = null;
			
			for(Site site : getSites(rOp.variable)) {
				if(	//For recovery: if replicated and recovering, cannot read: Opposite
					!(site.recovery && rOp.variable % 2 == 1)) {
					
					firstAvailibleSite = site;
					break;
				}
			}
			
			//DataManager: 
			if(firstAvailibleSite != null)
				readValue = firstAvailibleSite.getDataManager().read(rOp.variable);
		}
		System.out.println("Test Transaction " + transactionId + " read " + value + 
				" from " + variable + ": " + (readValue == value));
	}
	
	/*Print to STDOUT if Transaction <transactionId> wrote <VALUE> to <VARIABLE>
	 *input: String transactionId, String variable, Integer value
	 *output: NA
	 */
	public void assertWrite(String transactionId, String variable, Integer value) {
		ReadOperation rOp = new ReadOperation(	transactionId.trim(), //Transaction ID
				variable.trim()  //Variable
			 );
		
		boolean written = true;
		for(Site site : getSites(rOp.variable)) {
			if(site.getDataManager().read(rOp.variable) != value)
				written = false;
		}
		System.out.println("Test Transaction " + transactionId + " wrote " + value + 
				" to " + variable + ": " + written);
	}
}
