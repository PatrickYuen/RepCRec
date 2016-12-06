package nyu.edu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class RepCRec_Driver {
	private static TransactionManager tm;
	
	private static void initDB() {
		//Effects: Initializes all Sites and variable values
		tm = new TransactionManager(10, 20); //10 Sites, 20 Variables
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.out.println("Please input outputFile or stdout flag");
			return;
		}
		
		initDB();
		
		BufferedReader br;
		String line;
		boolean cmdline = false;
		
		if(args.length == 2) { //Input File
			br = new BufferedReader(new FileReader(args[1]));
		} else { //Standard Input
			br = new BufferedReader(new InputStreamReader(System.in));
			cmdline = true;
			tm.cmdline = true;
			System.out.println("Command Line Mode: Please Type commands below");
		}
		
		while((line = br.readLine()) != null && !line.equals("quit")) {
			line = line.toLowerCase().replaceAll("\\s+","");
			if(line.startsWith("//") || line.length() <= 0)
				continue;
			//Feeds them to Transaction Manager: R(T1,x2)
			tm.runAllSteps(line.toLowerCase());
		}
		
		br.close();
		
		//Writes output to output file
		if(!cmdline) {
			File outputfile = new File(args[0]);
			if(!outputfile.exists()) {
				System.out.println("Created New Output File: " + args[0]);
				outputfile.createNewFile();
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[0]));
			bw.write(tm.showStatus());
		    System.out.println("Output written Successfully to: " + args[0]);
		    bw.close();
		} else {
			System.out.println(tm.showStatus());
		}
	    
	}
	
/*
 * begin(T1)
	R(T1,x2);R(T2,x1)	// T1 reads x2 from site 1
	R(T1,x1)	// T1 reads x1 from site 2
	W(T1, x2, 10)	// T1 writes x2 to all sites
	R(T1, x2)	// make sure a transaction reads the last value it has written if it did the last write on a data item
	dump()
	begin(T2)
	begin(T3)
	W(T3, x4, 100)	// T3 writes x4 to all sites
	W(T3, x5, 100)	// T3 writes x5 to site 6
	R(T1, x4)	// should wait for T3 to finish
	R(T2, x5)	// should wait for T3 to finish
	end(T3)
	dump()
	R(T2, x4)	// T2 reads x4 from site 1
	R(T2, x2)	// T2 aborts - cannot wait for older transaction T1
	end(T1)
	dump()
*/

}
