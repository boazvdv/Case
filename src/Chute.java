import java.util.ArrayList;

public class Chute {
    private int chuteNumber;
    private int isLeft;
    private int distanceFront;
    private int maxContainers;
    private int expectedContainers;
    private ArrayList<Chute> neighboringChutes;
    private ArrayList<DestinationShift> destShiftAssignment;
    public Chute(int chuteNumber, int isLeft, int distanceFront, int maxContainers, ArrayList<Chute> neighboringChutes) {
        this.chuteNumber = chuteNumber;
        this.isLeft = isLeft;
        this.distanceFront = distanceFront;
        this.maxContainers = maxContainers;
        this.neighboringChutes = neighboringChutes;
        this.destShiftAssignment = new ArrayList<DestinationShift>();
    }

    public int getChuteNumber() { return chuteNumber; }
    public int getIsLeft() { return isLeft; }
    public int getDistanceFront() { return distanceFront; }
    public int getMaxContainers() { return maxContainers; }
    public ArrayList<Chute> getNeighboringChutes() { return neighboringChutes; }
    public ArrayList<DestinationShift> getDestShiftAssignment() { return destShiftAssignment; }
    public void setDestShiftAssignment(ArrayList<DestinationShift> newDestShiftAssignment) { this.destShiftAssignment = newDestShiftAssignment; }
    public int getExpectedContainers() {
        this.updateExpectedContainers();
        return expectedContainers;
    }
    public void updateExpectedContainers() {
        int newExpectedContainers = 0;
        for (DestinationShift destShift : this.destShiftAssignment) {
            newExpectedContainers += destShift.getExpectedContainers();
        }
        this.expectedContainers = newExpectedContainers;
    }
    public void addNeighboringChute(Chute newNeighboringChute) { this.neighboringChutes.add(newNeighboringChute); }

}
