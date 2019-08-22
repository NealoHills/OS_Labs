import java.util.ArrayList;

class SRTN {

	ArrayList<Process> processes;
	ArrayList<Integer> nums;
	int[] remaining;

	boolean verbose;

	public SRTN(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.processes = p;
		this.nums = nums;
		this.verbose = v; 
		remaining = new int[processes.size()];
		for (int n = 0; n < processes.size(); n++){
			remaining[n] = processes.get(n).getCPUTime();
		}
	}

	public void run(){
		int numprocess = processes.size();
		int cycle = 0;
		int rem = numprocess;

        /*
        while (processes.get(0).getArrival() > cycle){
			printVerbose(cycle);
			cycle++;
		}

		update(cycle, rem);
		printVerbose(cycle);
		*/

		while (rem > 0){
			printVerbose(cycle);
			rem = update(cycle, rem);
			cycle++;
		}
	}

    public int my_update(int cycle, int rem){
        int running = 0;
        int i;
        // figure out which process is running
        for (i = 0; i < Scheduler.num_processes; i++) {
            if (processes.get(i).getState().equals("running")) {
                running = i;
            }
        }
        // figure out which processes just arrived
        for (i=0; i<Scheduler.num_processes; i++) {
            Process p = processes.get(i);
            if (p.getState().equals("unstarted") && p.getArrival() <= cycle) {
                p.setState("ready");
            }
        }
        if (!isRunning() && lowestRem() >= 0){
            int cpuburst;
            int index = lowestRem();
            processes.get(index).setState("running");
            if(processes.get(index).getRemaining()!=0) {
                cpuburst = Scheduler.randomOS(processes.get(index).getB());
                if (cpuburst > remaining[index]) {
                    cpuburst = remaining[index];
                }
                processes.get(index).setRemaining(cpuburst);
            }
            running = index;
        }
        // figure out if we need to preempt the running process
        for(i=0; i<Scheduler.num_processes; i++){
            if (i == lowestRem() & i!=running) {
                processes.get(running).setState("ready");
                processes.get(i).setState("running");
                processes.get(i).setRemaining(Scheduler.randomOS(processes.get(i).getB()));
                break;
            }
        }
        for (i=0; i<Scheduler.num_processes; i++) {
            Process p = processes.get(i);
            // if a process is waiting in the ready list increase waiting time
            if (p.getState().equals("ready")){
                p.setWaitingTime(p.getWaitingTime() + 1);
            }
            // if a process is running update it's time remaining, then check if it terminated, then check if it got blocked, I may have screwed up and didn't put preemption last
            else if (p.getState().equals("running")){
                p.setRemaining(p.getRemaining() - 1);
                p.incrementCurrCPU();
                remaining[i]--;
                if(p.getCurrCPU() == p.getCPUTime()){
                    p.setState("terminated");
                    p.setFinTime(cycle-1);
                }
                else if(p.getRemaining() == 0){
                    p.setState("blocked");
                    p.setRemaining(Scheduler.randomOS(p.getIO()));
                }
            }
            // update blocked time and check to see if unblocked
            else if (p.getState().equals("blocked")){
                Scheduler.setOverlap(Scheduler.getOverlap()+1);
                p.setRemaining(p.getRemaining() - 1);
                p.setIOTime(p.getIOTime() + 1);
                if (p.getRemaining()==0){
                    p.setState("ready");
                }
            }
        }
        return lowestRem();
    }

	public int update(int cycle, int rem){
		int numblocked = 0;
		int counter = rem;
		for (int c = 0; c < processes.size(); c++) {
			Process p = processes.get(c);
			if (p.getState().equals("terminated")) {
			} else if (p.getArrival() <= cycle && p.getState().equals("unstarted")) {
				p.setState("ready");
			} else if (p.getState().equals("blocked")) {
				if (p.getRemaining() == 0) {
					p.setState("ready");
				}
			} else if (p.getState().equals("running")) {
				if (p.getRemaining() == 0) {
					p.setState("blocked");
					p.setRemaining(Scheduler.randomOS(p.getIO()));
				}
			} else if (p.getState().equals("ready")) {
				if (c == lowestRem()) {
					for (int i = 0; i < Scheduler.num_processes; i++) {
						if (processes.get(i).getState().equals("running")) {
							processes.get(i).setState("ready");
						}
					}
					p.setState("running");
					if(p.getRemaining()==0){
						p.setRemaining(Scheduler.randomOS(p.getB()));
					}
				}
			}
		}
		for (int n = 0; n < processes.size(); n++){
			Process p = processes.get(n);
			if (p.getState().equals("ready")){
				p.setWaitingTime(p.getWaitingTime() + 1);
			}else if (p.getState().equals("running")){
				p.setRemaining(p.getRemaining() - 1);
				p.incrementCurrCPU();
				remaining[n]--;
				if(p.getCurrCPU() == p.getCPUTime()){
					p.setState("terminated");
                    p.setRemaining(0);
					p.setFinTime(cycle);
                    p.setWaitingTime(p.getWaitingTime()-1);
					counter = rem - 1;
				}
                else if (p.getRemaining() == 0) {
                    p.setState("blocked");
                    p.setRemaining(Scheduler.randomOS(p.getIO()));
                }
                else if (n != lowestRem()){
                    p.setState("ready");
                }
			}else if (p.getState().equals("blocked")){
				numblocked++;
				p.setRemaining(p.getRemaining() - 1);
				p.setIOTime(p.getIOTime() + 1);
                if (p.getRemaining() == 0) {
                    p.setState("ready");
                }
			}
		}
        if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}


		if (!isRunning() && lowestRem() >= 0){
			int cpuburst;
			int index = lowestRem();
			processes.get(index).setState("running");
            if(processes.get(index).getRemaining()==0) {
                cpuburst = Scheduler.randomOS(processes.get(index).getB());
                processes.get(index).setRemaining(cpuburst);
            }
		}

		return counter;
	}


    public int lowestRem(){
		int rem = Integer.MAX_VALUE;
		int lowestindex = -1;
		for (int n = 0; n < processes.size(); n++){
			String state = processes.get(n).getState();
			if (state.equals("ready") || state.equals("running")){
				if (remaining[n] < rem){
					rem = remaining[n];
					lowestindex = n;
				}
			}
		}return lowestindex;
	}

	public boolean isRunning(){
		for (Process pr:processes){
			if (pr.getState().equals("running")){
				return true;
			}
		}return false;
	}


	public void printTime(){
		for (int n = 0; n < processes.size(); n++){
			System.out.printf("Process %d: %d remaining\t", n, processes.get(n).getCPUTime() - processes.get(n).getCurrCPU());
		}System.out.println();
	}

	public void printVerbose(int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
			}System.out.println();
		}
	}

}