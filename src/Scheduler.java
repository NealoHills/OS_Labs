import java.util.Scanner;
import java.io.*;
import java.util.Collections;
import java.util.ArrayList;


class Scheduler{
	
	public static boolean verbose = false; 
	public static int num_processes;
	public static ArrayList<Process> Processes = new ArrayList<Process>();
	public static ArrayList<Integer> randList = new ArrayList<Integer>();
	public static int randc;
	public static int overlap;

	public static void main(String[] args) throws FileNotFoundException{
		if (args.length == 0){
			System.out.println("Please enter a valid filename");
		}else if (args.length > 2){
			System.out.println("Do not enter more than two arguments");
		}else if (args.length > 1 && !args[0].equals("--verbose")){
			System.out.println("Incorrect flag argument. Did you mean --verbose"); 
		}else{
			if (args[0].equals("--verbose")){
				verbose = true;
			}try{				
				File f = new File(args[args.length-1]);
				File rn = new File("random-numbers.txt");		
				Scanner s = new Scanner(f);
				Scanner r = new Scanner(rn);

				Scheduler sched = new Scheduler();

                sched.getNumList(r);
				sched.setProcesses(s);

                s.close();
                r.close();

				sched.printHeading(num_processes);

				sched.printSummary("First Come First Serve");
				sched.reset();
                sched.printSummary("Round Robin");
                sched.reset();
				sched.printSummary("Last Come First Serve");
                sched.reset();
				sched.printSummary("Shortest Run Time Next");

			}catch (FileNotFoundException ex){
				System.out.println("File not found");
			}
		}
	}

	public void setProcesses(Scanner s){

        num_processes = s.nextInt();

		for (int i = 0; i < num_processes; i++){
            Processes.add(new Process(s.nextInt(), s.nextInt(), s.nextInt(), s.nextInt()));
        }
	}

	public static void setOverlap(int n){
		overlap = n;
	}

	public static int getOverlap(){
		return overlap;
	}

	public void getNumList(Scanner s){
		while (s.hasNextInt()){
			randList.add(s.nextInt());
		}
	}

	public void printHeading(int n){
		System.out.printf("The original input was: %d %s\n", n, printProcesses());
		Collections.sort(Processes);
		System.out.printf("The (sorted) input is:  %d %s\n", n, printProcesses());		
	}

	public String printProcesses(){
		String result = "";
		for (Process m: Processes){
			result += String.format("%s ", m.toString());
		}return result;
	}

	public void printSummary(String algorithm){
		int fin = 0;
		float cpu = 0f;
		float io = 0f;
		float throughput = 0f;
		float aTurnaround = 0f;
		float aWaiting = 0f;

		System.out.printf("\n\n%s:\n\n", algorithm);
		if (algorithm.equals("First Come First Serve")){
			FCFS fcfs = new FCFS(Processes, randList, verbose);
			fcfs.run();			
		}if (algorithm.equals("Round Robin")){
            RR rr = new RR(Processes, randList, verbose);
            rr.start();
        }if (algorithm.equals("Last Come First Serve")){
            LCFS lcfs = new LCFS(Processes, randList, verbose);
            lcfs.run();
        }if (algorithm.equals("Shortest Run Time Next")){
			SRTN SRTN = new SRTN(Processes, randList, verbose);
			SRTN.run();
		}



		for(int i = 0; i < Processes.size(); i++){
			Process pl = Processes.get(i);
			pl.printProcessInfo(i);
			if (pl.getFinTime() > fin){
				fin = pl.getFinTime();
			}
			cpu += pl.getCPUTime();
			io += pl.getIOTime();	
			aTurnaround += pl.getFinTime() - pl.getArrival();
			aWaiting += pl.getWaitingTime();				
		}
		System.out.printf("Summary Data:\n");
		System.out.printf("\tFinishing time: %d\n", fin);
		System.out.printf("\tCPU Utilization: %f\n", cpu / fin);
		System.out.printf("\tI/O Utilization: %f\n", (io-getOverlap()) / fin);
		System.out.printf("\tThroughput: %f processes per hundred cycles\n", (float) num_processes / fin * 100);
		System.out.printf("\tAverage turnaround time: %f\n", aTurnaround/num_processes);
		System.out.printf("\tAverage Waiting time: %f\n\n", aWaiting/num_processes);
	
	}

	public static int randomOS(int u){
		int x = randList.get(randc);
		randc++;
		return 1+(x % u);
	}

	public static void reset(){
		randc = 0;
		overlap = 0;
		for (Process pr: Processes){
			pr.reset();
		}
	}



}

	