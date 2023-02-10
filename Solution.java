public class Solution {
    private Chute[] chutes;
    private Worker[] workers;
    public Solution(Chute[] chutes, Worker[] workers) {
        this.chutes = chutes;
        this.workers = workers;
    }

    public Chute[] getChutes() { return chutes; }
    public Worker[] getWorkers() { return workers; }
    public void setChutes(Chute[] newChutes) { this.chutes = newChutes; }
    public void setWorkers(Worker[] newWorkers) { this.workers = newWorkers; }
}
