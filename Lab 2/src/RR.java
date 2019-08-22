import java.util.ArrayList;
import java.util.Collections;

class RR{

	public static int QUANTUM = 2;

	ArrayList<Process> processes;
	ArrayList<Integer> nums;
	ArrayList<Integer> q = new ArrayList<Integer>();
	boolean verbose;
	boolean notEnough = false;

	public RR(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.processes = p;
		this.nums = nums;
		this.verbose = v; 
	}

	public void start(){
		int cycle = 0;
		
		printVerbose(cycle);
		cycle++;
		while(!isDone()){
			cycle = run(cycle);
		}
	}

	public void update(int cycle){
		int numblocked = 0;
		for (int i = 0; i < processes.size(); i++){
			Process p = processes.get(i);
			if (p.getState().equals("terminated")){
			}else if (p.getState().equals("ready")){
				p.setWaitingTime(p.getWaitingTime() + 1);
			}else if (p.getState().equals("running")){
				p.setRemaining(p.getRemaining() - 1);
				p.incrementCurrCPU();
			}else if (p.getState().equals("blocked")){
				p.setRemaining(p.getRemaining() - 1);
				p.setIOTime(p.getIOTime() + 1);
				numblocked++;
			}
		}
        // no clue what this is doing
        if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}
	}

	public void set(int cycle){

		for (int c = 0; c < processes.size(); c++){
			Process p = processes.get(c);	
			if (p.getState().equals("terminated")){
			}else if (p.getArrival() < cycle && p.getState().equals("unstarted")){
				p.setState("ready");
			}else if (p.getState().equals("blocked")){
				if (p.getRemaining() == 0){
					p.setState("ready");
				}
			}else if (p.getState().equals("running")){
				if (notEnough){
					p.setState("ready");
				}
				if (p.getRemaining() == 0){
					p.setState("blocked");
					p.setRemaining(Scheduler.randomOS(p.getIO()));
				}
			}
		}
	}

	public void checkUnstarted(int cycle){
		for (int c = 0; c < processes.size(); c++){
			Process p = processes.get(c);
			if (p.getArrival() < cycle && p.getState().equals("unstarted")){
				p.setState("ready");
				q.add(c);
			}
		}
	}

	public int getRunning(){
        int running = -1;
        for (int c = 0; c < processes.size(); c++) {
            if (processes.get(c).getState().equals("running")) {
                running = c;
            }
        }
        return running;
    }

	public void checkQ() {

        for (int c = 0; c < processes.size(); c++) {
            Process p = processes.get(c);
            if (p.getState().equals("ready") && !q.contains(c)) {
            } else if (p.getState().equals("blocked") && p.getRemaining() == 0 && !q.contains(c)) {
                q.add(c);
            } else if (p.getState().equals("running") && notEnough && !q.contains(c) && p.getCurrCPU() < p.getCPUTime()) {
                q.add(c);
            }
        }
	}

	public int run(int cycle){
		checkUnstarted(cycle);
		checkTerm(cycle);
		while(isRunning()){
            int running = getRunning();
            Collections.sort(q);
            for (int i=0; i<q.size(); i++){
                if(q.get(i)<running){
                    q.add(q.get(0));
                    q.remove(0);
                }
            }
			update(cycle);
			set(cycle);
			checkQ();
			cycle++;
		}
        if (!isRunning() && q.size() == 0){
			printVerbose(cycle);
			update(cycle);
			set(cycle);
			checkQ();
			cycle++;
		}else if (!isRunning() && q.size() > 0){
            int index = q.get(0);
            q.remove(0);
			int r;
			int cpuburst;
			processes.get(index).setState("running");
			if (processes.get(index).getRemaining() == 0){
				cpuburst = Scheduler.randomOS(processes.get(index).getB());
                if(cpuburst > (processes.get(index).getCPUTime()-processes.get(index).getCurrCPU())){
                    cpuburst = processes.get(index).getCPUTime()-processes.get(index).getCurrCPU();
                }
				processes.get(index).setRemaining(cpuburst);
			}
			
			r = (processes.get(index).getRemaining());
			
			boolean add = false;

			if (r > QUANTUM){
				r = QUANTUM;
				add = true;
			}
			boolean term;
			for (int n = r; n > 0; n--){
				term = checkTerm(cycle);
				if (!term){
					checkUnstarted(cycle);
					if(add && n == 1){
						notEnough = true;	
					}
					printVerbose(cycle, n);
					update(cycle);
					checkQ();
                    checkTerm(cycle);
					set(cycle);
					cycle++;
				}
				else if (q.size() > 0){
					index = q.get(0);
					processes.get(index).setState("running");
					if (processes.get(index).getRemaining() == 0){
						cpuburst = Scheduler.randomOS(processes.get(index).getB());
                        if(cpuburst > processes.get(index).getCPUTime()-processes.get(index).getCurrCPU()){
                            cpuburst = processes.get(index).getCPUTime()-processes.get(index).getCurrCPU();
                        }
						processes.get(index).setRemaining(cpuburst);
						//processes.get(index).setIOBurst(cpuburst * processes.get(index).getIO());
					}
				}
				
			}notEnough =false;
			if (processes.get(index).getRemaining() > 0 && processes.get(index).getState().equals("running")){
				processes.get(index).setState("ready");
			}else if (processes.get(index).getRemaining() == 0 && processes.get(index).getState().equals("running")){
				processes.get(index).setState("blocked");
                processes.get(index).setRemaining(Scheduler.randomOS(processes.get(index).getIO()));
			}
		}
        return cycle;
	}

	public boolean checkTerm(int cycle){
		for (Process p:processes){
			if (!p.getState().equals("terminated") && p.getCPUTime() <= p.getCurrCPU()){
				p.setState("terminated");
				p.setRemaining(0);
				p.setFinTime(cycle);
				return true;
			}
		}return false;
	}

	public boolean isRunning(){
		for (Process pr:processes){
			if (pr.getState().equals("running")){
				return true;
			}
		}return false;
	}

	public boolean isDone(){
		for (Process pr:processes){
			if (!pr.getState().equals("terminated")){
				return false;
			}
		}return true;
	}

	public void printVerbose(int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
			}System.out.println(".");
		}
	}

	public void printVerbose(int cycle, int rem){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				if (processes.get(m).getState().equals("running")){
					System.out.printf("%13s %2d", processes.get(m).getState(), rem);
				}else if (processes.get(m).getState().equals("ready")){
					System.out.printf("%13s %2d", processes.get(m).getState(), 0);
				}else{
					System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
				}

			}System.out.println(".");
		}
	}

}
