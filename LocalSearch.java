import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;

public class LocalSearch {

    public static Solution chuteLocalSearch(PostInstance instance, Solution solution) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        Solution newSolution = new Solution(chutes, workers);
        return newSolution;
    }

    public static Solution workerLocalSearch(PostInstance instance, Solution solution) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            Worker[] workersSide = new Worker[workers.length / 2];
            int i = 0;
            for (Worker worker : workers) {
                if (worker.getIsLeft() == leftIndicator) {
                    workersSide[i] = worker;
                    i += 1;
                }
            }

            boolean swapFound = false;
            while (!swapFound) {
                Random generator = new Random();
                int randomIndex = generator.nextInt(workersSide.length);
                Worker workerRemove = workersSide[randomIndex];

                ArrayList<Worker> neighbors = workerRemove.getNeighboringWorkers();
                randomIndex = generator.nextInt(neighbors.size());
                Worker workerAdd = neighbors.get(randomIndex);
                if (workerAdd.getChuteAssignment().size() < instance.getMaxChutesPerWorker()) {
                    swapFound = true;
                    ArrayList<Chute> assignmentRemove = workerRemove.getChuteAssignment();
                    ArrayList<Chute> assignmentAdd = workerAdd.getChuteAssignment();
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
        }


        Solution newSolution = new Solution(chutes, workers);
        return newSolution;
    }

}