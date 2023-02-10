import java.util.ArrayList;

public class Worker {
    private int workerNumber;
    private int isLeft;
    private ArrayList<Chute> chuteAssignment;
    private ArrayList<Worker> neighboringWorkers;
    public Worker(int workerNumber, int isLeft, ArrayList<Worker> neighboringWorkers) {
        this.workerNumber = workerNumber;
        this.isLeft = isLeft;
        this.neighboringWorkers = neighboringWorkers;
        this.chuteAssignment = new ArrayList<Chute>();
    }

    public int getWorkerNumber() {
        return workerNumber;
    }
    public int getIsLeft() { return isLeft; }
    public ArrayList<Worker> getNeighboringWorkers() { return neighboringWorkers; }
    public ArrayList<Chute> getChuteAssignment() { return chuteAssignment; }
    public void setChuteAssignment(ArrayList<Chute> newChuteAssignment) { this.chuteAssignment = newChuteAssignment; }
    public void addNeighboringWorker(Worker newNeighboringWorker) { this.neighboringWorkers.add(newNeighboringWorker); }
}
