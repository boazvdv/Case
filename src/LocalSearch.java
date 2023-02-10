import java.util.ArrayList;
import java.util.Random;

public class LocalSearch {

    public static Solution chuteLocalSearch(PostInstance instance, Solution solution) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        return new Solution(chutes, workers);
    }

    public static Solution workerLocalSearch(PostInstance instance, Solution solution, int searchType) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            Worker[] workersSide = new Worker[workers.length / 2];
            int i = 0;
            for (Worker worker: workers) {
                if (worker.getIsLeft() == leftIndicator) {
                    workersSide[i] = worker;
                    i += 1;
                }
            }

            if (searchType == 1) {
                // Choose random worker, remove one of its chutes and add to a neighbor
                boolean swapFound = false;
                while (!swapFound) {
                    Random generator = new Random();
                    int randomIndex = generator.nextInt(workersSide.length);
                    Worker workerRemove = workersSide[randomIndex];

                    ArrayList < Worker > neighbors = workerRemove.getNeighboringWorkers();
                    randomIndex = generator.nextInt(neighbors.size());
                    Worker workerAdd = neighbors.get(randomIndex);
                    if (workerAdd.getChuteAssignment().size() < instance.getMaxChutesPerWorker()) {
                        swapFound = true;
                        ArrayList < Chute > assignmentRemove = workerRemove.getChuteAssignment();
                        ArrayList < Chute > assignmentAdd = workerAdd.getChuteAssignment();
                        if (workerRemove.getWorkerNumber() < workerAdd.getWorkerNumber()) {
                            Chute chuteToMove = assignmentRemove.remove(assignmentRemove.size() - 1);
                            assignmentAdd.add(0, chuteToMove);
                        } else {
                            Chute chuteToMove = assignmentRemove.remove(0);
                            assignmentAdd.add(chuteToMove);
                        }
                        workerRemove.setChuteAssignment(assignmentRemove);
                        workerAdd.setChuteAssignment(assignmentAdd);
                    }
                }
            } else if (searchType == 2) {
                // Get the busiest worker and the least busy worker with spare capacity
                // Move chute from busiest to least busy
                Worker busiestWorker = workersSide[0];
                Worker leastBusyWorker = workersSide[0];
                for (Worker worker: workersSide) {
                    if (worker.getExpectedContainers() > busiestWorker.getExpectedContainers()) {
                        busiestWorker = worker;
                    }
                    if (worker.getExpectedContainers() < leastBusyWorker.getExpectedContainers()) {
                        if (worker.getChuteAssignment().size() < instance.getMaxChutesPerWorker()) {
                            leastBusyWorker = worker;
                        }
                    }
                }
                boolean destinationReached = false;
                Worker workerRemove = busiestWorker;
                Worker workerAdd = busiestWorker;
                ArrayList < Chute > assignmentRemove;
                ArrayList < Chute > assignmentAdd;
                while (!destinationReached) {
                    ArrayList < Worker > neighbors = workerRemove.getNeighboringWorkers();
                    if (busiestWorker.getWorkerNumber() > leastBusyWorker.getWorkerNumber()) {
                        // Move chute downstream
                        for (Worker neighbor: neighbors) {
                            if (neighbor.getWorkerNumber() < workerRemove.getWorkerNumber()) {
                                workerAdd = neighbor;
                                break;
                            }
                        }
                        assignmentRemove = workerRemove.getChuteAssignment();
                        Chute chuteToMove = assignmentRemove.remove(0);

                        assignmentAdd = workerAdd.getChuteAssignment();
                        assignmentAdd.add(chuteToMove);
                    } else {
                        // Move chute upstream
                        for (Worker neighbor: neighbors) {
                            if (neighbor.getWorkerNumber() > workerRemove.getWorkerNumber()) {
                                workerAdd = neighbor;
                                break;
                            }
                        }
                        assignmentRemove = workerRemove.getChuteAssignment();
                        Chute chuteToMove = assignmentRemove.remove(assignmentRemove.size() - 1);

                        assignmentAdd = workerAdd.getChuteAssignment();
                        assignmentAdd.add(0, chuteToMove);

                    }
                    workerRemove.setChuteAssignment(assignmentRemove);
                    workerAdd.setChuteAssignment(assignmentAdd);
                    if (workerAdd.getWorkerNumber() == leastBusyWorker.getWorkerNumber()) {
                        destinationReached = true;
                    }
                    else {
                        workerRemove = workerAdd;
                    }
                }
            }
        }

        return new Solution(chutes, workers);
    }
}