class Process implements Comparable<Process>{

	public int A;
	public int B;
	public int C;
	public int IO;
	public String state = "unstarted";
	public int remaining = 0;
	public int finTime;
	public int ioTime = 0;
	public int waitingTime = 0;
	public int currCPU = 0;
	public int ioburst;
	public int readyTime = 0;
	public int requestTime;

	public Process(int A, int B, int C, int IO){
		this.A = A;
		this.B = B;
		this.C = C;
		this.IO = IO;
	}

	public int getArrival(){
		return A;
	}

	public int getB(){
		return B;
	}

	public int getCPUTime(){
		return C;
	}

	public int getIO(){
		return IO;
	}

	public String getState(){
		return state;
	}

	public int getRemaining(){
		return remaining;
	}

	public int getFinTime(){
		return finTime;
	}
	public int getIOTime(){
		return ioTime;
	}
	public int getWaitingTime(){
		return waitingTime;
	}

	public int getCurrCPU(){
		return currCPU;
	}

	public int getioburst(){
		return ioburst;
	}

	public int getReadyTime(){
		return readyTime;
	}

	public int getRequestTime(){
		return requestTime;
	}

	public void incrementCurrCPU(){
		currCPU++;
	}

	public void setCurrCPU(int n){
		currCPU = n;
	}

	public void setState(String s){
		state = s;
		if (s != "ready"){
			readyTime = 0;
		}
	}

	public void setRemaining(int r){
		remaining = r;
	}

	public void setFinTime(int finTime){
		this.finTime = finTime ;
	}
	public void setIOTime(int ioTime){
		this.ioTime = ioTime ;
	}
	public void setWaitingTime(int waitingTime){
		this.waitingTime = waitingTime ;
	}

	public void setIOBurst(int b){
		ioburst = b;
	}

	public void setReadyTime(int r) {
        readyTime = r;
    }

	public void setRequestTime(int r){
		requestTime = r;
	}

	public int compareTo(Process p){
		return Integer.compare(getArrival(), p.getArrival());
	}

	public String toString(){
		return String.format("(%d, %d, %d, %d)", getArrival(), getB(), getCPUTime(), getIO());
	}

	public void printProcessInfo(int p){
		System.out.printf("Process %d:\n", p);
		System.out.printf("\t(A,B,C,IO) = %s\n", toString());
		System.out.printf("\tFinishing time: %d\n", getFinTime());
		System.out.printf("\tTurnaround time: %d\n", getFinTime() - getArrival());
		System.out.printf("\tI/O time: %d\n", getIOTime());
		System.out.printf("\tWaiting time: %d\n\n", getWaitingTime());

	}

	public void reset(){
		state = "unstarted";
		remaining = 0;
		finTime = 0;
		ioTime = 0;
		waitingTime = 0;
		currCPU = 0;
		ioburst = 0;
 		readyTime = 0;
		requestTime = 0;
	}

}
