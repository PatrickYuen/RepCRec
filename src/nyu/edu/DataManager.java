package nyu.edu;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
	private DataElement[] elements; //where all data is stored

	public DataManager(int numVariables) {
		elements = new DataElement[numVariables];
	}
	
	public void init(int currVar, int initVal) {
		elements[currVar] = new DataElement(initVal);
	}
	
	/*
	 * Input: NA
	 * Output: Integer output of variable
	 * Effects: read actual value
	 */
	public Integer read(int variable) {
		return elements[variable].value;
	}
	
	/*
	 * Input: NA
	 * Output: Integer output of variable
	 * Effects: read Last Commited value
	 */
	public Integer committedRead(int variable) {
		return elements[variable].lastCommitedVal;
	}

	/*
	 * Input: NA
	 * Output: NA
	 * Effects: write value
	 */
	public void write(WriteOperation op) {
		elements[op.variable].value = op.value;
	}

	/*
	 * Input: NA
	 * Output: NA
	 * Effects: write value t0 lastCommitedVal
	 */
	public void commit(Integer variable) {
		elements[variable].lastCommitedVal = elements[variable].value;
	}
	
	/*
	 * Input: NA
	 * Output: NA
	 * Effects: Restore value to lastCommitedVal
	 */
	public void reset(Integer variable) {
		elements[variable].value = elements[variable].lastCommitedVal;
	}
	
	/*
	 * Input: NA
	 * Output: NA
	 * Effects: allRestore value to lastCommitedVal
	 */
	public void resetAll() {
		for(int variable = 0; variable < elements.length; variable++) {
			if(elements[variable] != null)
				elements[variable].value = elements[variable].lastCommitedVal;
		}
	}
	
	public String dump(int variable) {
		return String.format("%1$" + 5 + "s", elements[variable].value.toString());
	}
	
	public String dumpAll() {
		StringBuilder buf = new StringBuilder();
		for(DataElement ele : elements) {
			buf.append(String.format("%1$" + 5 + "s", ele == null? "*":ele.value));
		}
		return buf.toString();
	}
	
	public List<Integer> variablesStored() {
		List<Integer> vars = new ArrayList<Integer>();
		for(int ind = 0; ind < elements.length; ind++) {
			if(elements[ind] != null)
				vars.add(ind);
				
		}
		return vars;
	}
}
