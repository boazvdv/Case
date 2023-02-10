public class DestinationShift {
	private final int destination;
	private final int shift;
	private final int isLeft;
	private final int expectedContainers;
	public DestinationShift(int destination, int shift, int isLeft, int expectedContainers) {
		this.destination = destination;
		this.shift = shift;
		this.isLeft = isLeft;
		this.expectedContainers = expectedContainers;
	}

	public int getDestination() {
		return destination;
	}
	public int getShift() {
		return shift;
	}
	public int getIsLeft() { return isLeft; }
	public int getExpectedContainers() {
		return expectedContainers;
	}
}
