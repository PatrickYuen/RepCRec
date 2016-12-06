RepCRec - Replicated Concurrency Control and Recovery

Authors: Patrick Yuen, Sanaya Bhathena  

===========================================================================
- Run Instructions:  
Make sure java version is 1.8 (on energon1, module load java-1.8)  
Running the main Driver:  

*In src, compile with javac -cp . nyu.edu.RepCRec_Driver.java  
java -cp . nyu.edu.RepCRec_Driver [InputFileName] [Output FileName]  

or use the provided jar 

  java -cp repcrecADB.jar nyu.edu.RepCRec_Driver [InputFileName] [Output FileName]  

*2 arguments assume arguements as: [inputFile] [outputFile]  
*1 argument assumes: [inputFile]   
*0 arguments will be complete command line interface.    

To bulk run tests:  
  java -cp repcrecADB.jar nyu.edu.RunTests [test Output Directory] [test Directory]  

Example Input:  
begin(T1)  
begin(T2)  
W(T1,x1,101)   
W(T2,x2,202)  
W(T1,x2,102);W(T2,x1,201)  
end(T1)  
end(T2)  
dump()  
  
*Reprozip packages run on energon1 as google drive links since they are quite large (~50 MB):  
Normal run:
java -cp repcrecADB.jar nyu.edu.RepCRec_Driver Input.txt  
https://drive.google.com/file/d/0B_dgWatPZKsha1JPakNYLVdQS0E/view?usp=sharing  

Bulk Testing:
java -cp repcrecADB.jar nyu.edu.RunTests testOutput tests  
https://drive.google.com/file/d/0B_dgWatPZKshZHdzdEd3SWg3eE0/view?usp=sharing

===========================================================================

- Description:
In groups of 1 or 2, you will implement a distributed database, complete
with multiversion concurrency control, deadlock detection, replication, and
failure recovery. If you do this project, you will have a deep insight into the
design of a distributed system. Normally, this is a multi-year, multi-person
effort, but it’s doable because the database is tiny.

- Data:
The data consists of 20 distinct variables x1, ..., x20 (the numbers between
1 and 20 will be referred to as indexes below). There are 10 sites
numbered 1 to 10. A copy is indicated by a dot. Thus, x6.2 is the copy of
variable x6 at site 2. The odd indexed variables are at one site each (i.e.
1 + index number mod 10 ). For example, x3 and x13 are both at site 4.
Even indexed variables are at all sites. Each variable xi is initialized to the
value 10i. Each site has an independent lock table. If that site fails, the
lock table is erased.

- Algorithms to use:
  - Available copies approach to replication
  - Two phase locking (using read and write locks) at each site and validation at commit time. 
  - Detect deadlocks using cycle detection and abort the youngest transaction in the cycle. 
  - Read-only transactions should use multiversion read consistency.

- Test Specification:
Input instructions come from a file or the
standard input, output goes to standard out (with flag -stdout) or targeted file. Bulk testing can be done with the runtests.java file. 

