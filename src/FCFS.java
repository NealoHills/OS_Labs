import java.util.ArrayList;

class FCFS{

	ArrayList<Process> p;
	ArrayList<Integer> nums;
	boolean verbose;

	public FCFS(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
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
		}update(cycle,isRunning(), rem);
		printVerbose(numprocess, cycle);
		cycle++;
		while (rem > 0){
			rem = update(cycle, isRunning(), rem);
			printVerbose(numprocess,cycle);
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
		for (int f = 0; f < p.size(); f++){
			if (p.get(f).getState().equals("running")){
				p.get(f).incrementCurrCPU();
			}if (p.get(f).getState().equals("ready")){
				p.get(f).setWaitingTime(p.get(f).getWaitingTime() + 1);
			}if (!p.get(f).getState().equals("ready")){
				p.get(f).setRequestTime(0);
			}if (p.get(f).getState().equals("blocked")){
				numblocked++;
			}
		}if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}
		for (int c = 0; c < p.size(); c++){
			Process pc = p.get(c);
			int cpuburst;
			int ioburst;	

			if (!pc.getState().equals("terminated")){

				if (pc.getArrival() < cycle && pc.getState().equals("unstarted")){
					if (isRunning()){
						pc.setState("ready");
						pc.setRequestTime(cycle);
						pc.setReadyTime(1);
					}else{
						pc.setState("running");
						cpuburst = Scheduler.randomOS(p.get(c).getB());
						pc.setRemaining(cpuburst);
					}
				}else if (pc.getCurrCPU() == pc.getCPUTime()){
					pc.setFinTime(cycle-1);
					pc.setState("terminated");
					if (getNextReady() < 2147483646){
						int in = getNextReady();
						p.get(in).setState("running");
						cpuburst = Scheduler.randomOS(p.get(in).getB());
						p.get(in).setRemaining(cpuburst);
						if (c < in){
							p.get(in).setRemaining(cpuburst+1);
						}
						p.get(in).setIOBurst(cpuburst * p.get(in).getIO());
					}
					pc.setRemaining(0);
					counter--;
				}else if (pc.getState().equals("ready") && isRunning()){
					pc.setReadyTime(pc.getReadyTime() + 1);
				}
				else if (pc.getState().equals("ready") && !isRunning()){
					pc.setState("running");
					cpuburst = Scheduler.randomOS(p.get(c).getB());
					pc.setRemaining(cpuburst);
					pc.setIOBurst(cpuburst * pc.getIO());
				}else if (pc.getState().equals("running")){
					
					if (pc.getRemaining() > 1){
						pc.setRemaining(pc.getRemaining() -1);
					}else if (pc.getRemaining() == 1){

						pc.setState("blocked");
						pc.setRemaining(Scheduler.randomOS(pc.getIO()));
						for (int m = 1; m < p.size(); m++){
							int index = (c+m) % p.size();
							if (getNextReady() < 2147483646){
								index = getNextReady();
							}
							
							if (p.get(index).getState().equals("ready")){
								p.get(index).setState("running");
								cpuburst = Scheduler.randomOS(p.get(index).getB());
								p.get(index).setRemaining(cpuburst);
								if (c < index){
									p.get(index).setRemaining(cpuburst+1);
								}
								p.get(index).setIOBurst(cpuburst * p.get(index).getIO());
								m=p.size();
							}	
						}
					}
				}else if (pc.getState().equals("blocked")){
					pc.setIOTime(pc.getIOTime() + 1);
					if(pc.getRemaining() > 1){
						pc.setRemaining(pc.getRemaining() - 1);
					}else if (pc.getRemaining() == 1){
						pc.setState("ready");
						pc.setRemaining(0);
						pc.setRequestTime(cycle);
						if (!isRunning()){
							pc.setState("running");
							cpuburst = Scheduler.randomOS(pc.getB());
							pc.setRemaining(cpuburst);
							pc.setIOBurst(cpuburst * pc.getIO());
						}
					}
				}
			}
		}
				
		return counter;
	}

	public int getNextReady(){
		int lowest = 2147483646;
		int lowestindex = 2147483646;
		for (int i = 0; i < p.size(); i++){
			Process pr = p.get(i);
			if (pr.getState().equals("ready")){
				if (pr.getRequestTime() > 0 && pr.getRequestTime() < lowest){
					lowest = pr.getRequestTime();
					lowestindex = i;
				}
			}
		}return lowestindex;
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