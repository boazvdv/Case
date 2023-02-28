package SimulationPackage;

public class Action implements Comparable<Action> {
	
	private final int time;
	private final String toDo;
	private final int chute;
	private final int dest;
	private final int shift;
	
	public Action (int time, String toDo, int chute, int dest, int shift) {
		this.time = time;
		this.toDo = toDo;
		this.chute = chute;
		this.dest = dest;
		this.shift = shift;
	}

	public int getTime() {
		return time;
	}

	public String getToDo() {
		return toDo;
	}

	public int getChute() {
		return chute;
	}

	public int getDest() {
		return dest;
	}

	public int getShift() {
		return shift;
	}


	@Override
	public String toString() {
		return "Action [time=" + time + ", toDo=" + toDo + ", chute=" + chute + ", dest=" + dest + ", shift=" + shift
				+ "]";
	}

	@Override
	public int compareTo(Action action2) {
		return (this.time - action2.time);
	}

}
