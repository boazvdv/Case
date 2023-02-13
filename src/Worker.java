import java.util.ArrayList;

public class Worker {
    private final int workerNumber;
    private final int isLeft;
    private ArrayList<Chute> chuteAssignment;
    private final ArrayList<Worker> neighboringWorkers;
    private int expectedContainers;
    public Worker(int workerNumber, int isLeft, ArrayList<Worker> neighboringWorkers) {
        this.workerNumber = workerNumber;
        this.isLeft = isLeft;
        this.neighboringWorkers = neighboringWorkers;
        this.chuteAssignment = new ArrayList<>();
    }

    public int getWorkerNumber() {
        return workerNumber;
    }
    public int getIsLeft() { return isLeft; }
    public ArrayList<Worker> getNeighboringWorkers() { return neighboringWorkers; }
    public void addNeighboringWorker(Worker newNeighboringWorker) { this.neighboringWorkers.add(newNeighboringWorker); }
    public ArrayList<Chute> getChuteAssignment() { return chuteAssignment; }
    public void setChuteAssignment(ArrayList<Chute> newChuteAssignment) { this.chuteAssignment = newChuteAssignment; }
    public int getExpectedContainers() {
        this.updateExpectedContainers();
        return expectedContainers;
    }
    public void updateExpectedContainers() {
        int newExpectedContainers = 0;
        for (Chute chute : this.chuteAssignment) {
            newExpectedContainers += chute.getExpectedContainers();
        }
        this.expectedContainers = newExpectedContainers;
    }

    public Worker clone() {
        try {
            return (Worker) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Worker(this.workerNumber, this.isLeft, this.neighboringWorkers);
        }
    }
}
