import java.util.ArrayList;
import java.util.Stack;

class LCFS{

	ArrayList<Process> p;
	ArrayList<Integer> nums;
	boolean verbose;
	Stack<Integer> s = new Stack<Integer>();

	public LCFS(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.p = p;
		this.nums = nums;
		this.verbose = v; 
		int size = p.size();
	}
	public void run(){
		int numprocess = p.size();
		int cycle = 0;
		boolean terminated = false;
		int cpuburst;
		int ioburst;
		int cpuTime = 0;
		int ioTime = 0;
		int rem = numprocess;

		while (p.get(0).getArrival() > cycle){
			printVerbose(numprocess,cycle);
			cycle++;
		}
        printVerbose(numprocess, cycle);
        update(cycle,isRunning(), rem);
		cycle++;
		while (rem > 0){
            printVerbose(numprocess,cycle);
			rem = update(cycle, isRunning(), rem);
			cycle++;

		}	
	}

	public boolean isRunning(){
		for (Process pr:p){
			if (pr.getState().equals("running")){
				return true;
			}
		}return false;
	}

	public int update(int cycle, boolean running, int rem){	
		int counter = rem;
		int numblocked = 0;
		for (int f = p.size()-1; f > -1; f--){
			// do we want all of these to be ifs or else ifs?
            if (p.get(f).getArrival() <= cycle && p.get(f).getState().equals("unstarted")) {
                //set state of ready process to ready
                p.get(f).setState("ready");

                //else {
                    s.push(f);
                //}
                //set request time
                p.get(f).setRequestTime(cycle);
                p.get(f).setReadyTime(0);
            }
			if (p.get(f).getState().equals("running")){
				p.get(f).incrementCurrCPU();
			}if (!p.get(f).getState().equals("ready")){
				p.get(f).setRequestTime(0);
			}
			/*if (!isRunning() && p.get(f).getState().equals("ready")) {
                p.get(f).setState("running");
                p.get(f).setRemaining(Scheduler.randomOS(p.get(f).getB()));
                p.get(f).setReadyTime(1);
            }*/
			if (p.get(f).getState().equals("blocked")){
				numblocked++;
                if(p.get(f).getRemaining() > 0){
                    //decrement remaining by 1
                    p.get(f).setRemaining(p.get(f).getRemaining() - 1);
                    p.get(f).setIOTime(p.get(f).getIOTime()+1);
                    //if only 1 remains
                }
                if (p.get(f).getRemaining() == 0) {
                    //put process back to ready
                    p.get(f).setState("ready");
					s.push(f);
                    p.get(f).setReadyTime(1);
                    //remaining time to ready
                    p.get(f).setRemaining(0);
                    p.get(f).setRequestTime(cycle);
                }
			}
		}if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}
		//iterating over all processes in one cycle
		for (int c = 0; c < p.size(); c++){
			Process pc = p.get(c);
			int cpuburst;
			int ioburst;	
			//if process is not terminated
			if (!pc.getState().equals("terminated")){
				//checking if process is ready and unstarted
				if (pc.getArrival() <= cycle && pc.getState().equals("unstarted")) {
                    //if a process is running already
                    //set state of ready process to ready
                    pc.setState("ready");
                    s.push(c);
                    //set request time
                    pc.setRequestTime(cycle);
                    pc.setReadyTime(0);
				//checking if currentCPUtime is finished
				}else if (pc.getCurrCPU() == pc.getCPUTime()){
					//set the finishing time
					pc.setFinTime(cycle);
					//set state to terminated
					pc.setState("terminated");
					int in = getNextReady();
					if (in < Integer.MAX_VALUE){
						//get index of next process thats supposed to run
						//set that process to running
						p.get(in).setState("running");
						//set Ready time 1
						p.get(in).setReadyTime(1);
						//set the CPU burst of the now running process
						cpuburst = Scheduler.randomOS(p.get(in).getB());
						//set remaining CPU burst time of that process
						p.get(in).setRemaining(cpuburst);
						//condition as required in the instructions 
						if (c < in){
							p.get(in).setRemaining(cpuburst+1);
						}
						//p.get(in).setIOBurst(cpuburst * p.get(in).getIO());
					}
					pc.setRemaining(0);
					counter--;
				//if a process is ready and a process is already running 
				}else if (pc.getState().equals("ready") && isRunning()){
					//increment the ReadyTime
					pc.setReadyTime(pc.getReadyTime() + 1);

				}
				//if a process is ready and a process is not running 
				else if (pc.getState().equals("ready") && !isRunning()){
					//set state of that process to running
					pc.setState("running");
                    s.pop();
					//set ready time back to 0
					pc.setReadyTime(pc.getReadyTime() + 1);
					//calculate cpu burst
					cpuburst = Scheduler.randomOS(p.get(c).getB());
					pc.setRemaining(cpuburst);
					//calculate IO burst
					//pc.setIOBurst(cpuburst * pc.getIO());
				//if a process is running
				}else if (pc.getState().equals("running")){
					//if a process has more that 1 remaining
					if (pc.getRemaining() > 0){
						//decrement the remaining Time by 1
						pc.setRemaining(pc.getRemaining() -1);
					//if there is only one remaining
					}
					if (pc.getRemaining() == 0){
						//set the state to blocked
						pc.setState("blocked");
						//calculate the IO time
						pc.setRemaining(Scheduler.randomOS(pc.getIO()));
						//iterate through the processes and get the index of the next ready process to be set to running
						/*for (int m = 1; m < p.size(); m++){
							int index = (c+1) % p.size();
                            int next = getNextReady();
							if (next < Integer.MAX_VALUE && next<index){
								index = next;
							}*/
						int index = getNextReady();

							if (index<Integer.MAX_VALUE && p.get(index).getState().equals("ready")){
								//the next index thats supposed to be running set state to running
								p.get(index).setState("running");
								//set Ready time back to 0
								p.get(index).setReadyTime(0);
								//calculate CPU burst
								cpuburst = Scheduler.randomOS(p.get(index).getB());
								p.get(index).setRemaining(cpuburst);
                                //what is this next line for?
								if (c < index){
									p.get(index).setRemaining(cpuburst+1);
								}
								//p.get(index).setIOBurst(cpuburst * p.get(index).getIO());
								//m=p.size();
							}	
						//}
					}
				//if a process is blocked
				}
				/*else if (pc.getState().equals("blocked")){
					pc.setIOTime(pc.getIOTime() + 1);
					if(pc.getRemaining() > 0){
						//decrement remaining by 1
						pc.setRemaining(pc.getRemaining() - 1);
					//if only 1 remains
					}else if (pc.getRemaining() == 0){
						//put process back to ready
						pc.setState("ready");
						pc.setReadyTime(1);
						//remaining time to ready
						pc.setRemaining(0);
						pc.setRequestTime(cycle);
						//if process is not running
						if (!isRunning()){
                            /** why are you running this? Just because nothing was running at that moment doesn't mean it's the most recent one to be ready
							//set that process to running
							pc.setState("running");
							//set ready Time back to 1
							pc.setReadyTime(1);
							cpuburst = Scheduler.randomOS(pc.getB());
							pc.setRemaining(cpuburst);
							pc.setIOBurst(cpuburst * pc.getIO());
						}
					}
				}*/
			}
		}
		for(int i=0; i<p.size(); i++){
            if(p.get(i).getState().equals("ready")){
                p.get(i).setWaitingTime(p.get(i).getWaitingTime() + 1);
            }
        }
		return counter;
	}

	public int getNextReady(){
		int lowest = Integer.MAX_VALUE;
		int lowestindex = Integer.MAX_VALUE;
		for (int i = 0; i < p.size(); i++){
			Process pr = p.get(i);
			if (pr.getState().equals("ready")){
				if (pr.getReadyTime() <= lowest){
					lowest = pr.getReadyTime();
					lowestindex = i;
				}
			}
		}
		if (!s.empty())
		    lowestindex = s.pop();
		return lowestindex;
	}
	
	public void printVerbose(int numprocess, int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < numprocess; m++){
				System.out.printf("%13s %2d", p.get(m).getState(), p.get(m).getRemaining());
			}System.out.println();
		}
	}

}
