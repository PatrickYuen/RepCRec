package nyu.edu;

import java.io.File;

public class RunTests {
	public static void main(String[] args) throws Exception {
		//Output Directory
		if(args.length < 2) {
			System.out.println("Please input input Directory and output directory");
			return;
		}
		
		File outputDirectory = new File(args[0]);
		File inputDirectory = new File(args[1]);
		
		if(!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		
		//Check if directory was directory
		if(!inputDirectory.isDirectory() || !outputDirectory.isDirectory()) {
			System.out.println("Please input input Directory and output directory");
			return;
		}
		
		for(File testFile : inputDirectory.listFiles()) {
			System.out.println("Running test: " + testFile);
			String outputFile = outputDirectory.getName() + "\\result-" + testFile.getName();
			RepCRec_Driver.main(new String[]{ outputFile , testFile.getPath() });
		}
		
		System.out.println("Finished Tests");
	}
}
