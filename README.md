Replicated Concurrency Control and Recovery
(RepCRec for short)

===========================================================================
In groups of 1 or 2, you will implement a distributed database, complete
with multiversion concurrency control, deadlock detection, replication, and
failure recovery. If you do this project, you will have a deep insight into the
design of a distributed system. Normally, this is a multi-year, multi-person
effort, but it’s doable because the database is tiny.

- Data
The data consists of 20 distinct variables x1, ..., x20 (the numbers between
1 and 20 will be referred to as indexes below). There are 10 sites
numbered 1 to 10. A copy is indicated by a dot. Thus, x6.2 is the copy of
variable x6 at site 2. The odd indexed variables are at one site each (i.e.
1 + index number mod 10 ). For example, x3 and x13 are both at site 4.
Even indexed variables are at all sites. Each variable xi is initialized to the
value 10i. Each site has an independent lock table. If that site fails, the
lock table is erased.

- Algorithms to use
Please implement the available copies approach to replication using two
phase locking (using read and write locks) at each site and validation at
commit time. A transaction may read a variable and later write that same
variable as well as others. Please use the version of the algorithm specified
in my notes rather than in the textbook.
Detect deadlocks using cycle detection and abort the youngest transaction
in the cycle. This implies that your system must keep track of the
oldest transaction time of any transaction holding a lock. (We will ensure
that no two transactions will have the same age.)
Read-only transactions should use multiversion read consistency.

- Test Specification
When we test your software, input instructions come from a file or the
standard input, output goes to standard out. 
