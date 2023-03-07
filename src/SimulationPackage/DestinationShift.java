package SimulationPackage;

public class DestinationShift {
	
	private final int destination;
	private final int shift;
	
	public DestinationShift(int destination, int shift) {
		this.destination = destination;
		this.shift = shift;
	}

	public int getDestination() {
		return destination;
	}

	public int getShift() {
		return shift;
	}

	@Override
	public String toString() {
		return "Objects.DestinationShift [destination=" + destination + ", shift=" + shift + "]";
	}
	
	

}
