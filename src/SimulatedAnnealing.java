import SimulationPackage.InstancePostNL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SimulatedAnnealing {
    public static Solution main(InstancePostNL instance) {
        // Construct initial solution
        Solution currentSolution = ConstructionHeuristic.main(instance);
        double currentObjective = currentSolution.getObjective(instance);

        Solution bestSolution = currentSolution;
        double bestObjective = currentObjective;

        Solution newSolution;

        double delta;
        double p;
        double P;
        double T = 10;
        double stoppingCriterion = 10E-4;
        double alpha = 0.9999;

        boolean searchChutes = true;
        Random r = new Random();

        while (T > stoppingCriterion) {
            HashMap<Integer, ArrayList<Chute>> workerNumberToChuteAssignment = saveChuteAssignment(currentSolution);
            HashMap<Integer, ArrayList<DestinationShift>> chuteNumberToDestShiftAssignment = saveDestShiftAssignment(currentSolution);

            if (searchChutes) {
                int randomIndex = r.nextInt(2); // Randomly select local search type
                randomIndex = randomIndex + 1;
                newSolution = LocalSearch.chuteLocalSearch(instance, currentSolution, randomIndex);
                searchChutes = false;
            }
            else {
                int index = r.nextInt(3); // Randomly select local search type
                index = index + 1;
                newSolution = LocalSearch.workerLocalSearch(instance, currentSolution, index);
                searchChutes = true;
            }
            double newObjective = currentSolution.getObjective(instance);
            if (newObjective < currentObjective) {
                currentObjective = newObjective;
                currentSolution = newSolution;

                if (newObjective < bestObjective) {
                    bestObjective = newObjective;
                    bestSolution = newSolution;
                }
            }
            else {
                delta = newObjective - currentObjective;
                T = alpha * T;
                p = r.nextDouble();
                P = Math.exp(- delta / T);
                if (p < P) {
                    currentObjective = newObjective;
                    currentSolution = newSolution;
                }
                else {
                    revertSolution(workerNumberToChuteAssignment, chuteNumberToDestShiftAssignment, bestSolution);
                }
            }
        }
        return bestSolution;
    }

    public static HashMap<Integer, ArrayList<Chute>> saveChuteAssignment(Solution currentSolution) {
        HashMap<Integer, ArrayList<Chute>> workerNumberToChuteAssignment = new HashMap<>();
        for (Worker worker : currentSolution.getWorkers()) {
            ArrayList<Chute> chuteAssignmentSaved = new ArrayList<>(worker.getChuteAssignment());
            workerNumberToChuteAssignment.put(worker.getWorkerNumber(), chuteAssignmentSaved);
        }
        return workerNumberToChuteAssignment;
    }

    public static HashMap<Integer, ArrayList<DestinationShift>> saveDestShiftAssignment(Solution currentSolution) {
        HashMap<Integer, ArrayList<DestinationShift>> chuteNumberToDestShiftAssignment = new HashMap<>();
        for (Chute chute : currentSolution.getChutes()) {
            ArrayList<DestinationShift> destShiftAssignmentSaved = new ArrayList<>(chute.getDestShiftAssignment());
            chuteNumberToDestShiftAssignment.put(chute.getChuteNumber(), destShiftAssignmentSaved);
        }
        return chuteNumberToDestShiftAssignment;
    }

    public static void revertSolution(HashMap<Integer, ArrayList<Chute>> workerNumberToChuteAssignment, HashMap<Integer, ArrayList<DestinationShift>> chuteNumberToDestShiftAssignment, Solution solution) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        for (Chute chute : chutes) {
            chute.setDestShiftAssignment(chuteNumberToDestShiftAssignment.get(chute.getChuteNumber()));
        }

        for (Worker worker : workers) {
            worker.setChuteAssignment(workerNumberToChuteAssignment.get(worker.getWorkerNumber()));
        }
    }
}