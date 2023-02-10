import java.util.Random;

public class SimulatedAnnealing {
    public static Solution main(PostInstance instance) {

        int searchType = 2;

        // Construct initial solution
        Solution newSolution = ConstructionHeuristic.main(instance);
        Solution bestSolution = newSolution;
        int bestObjective = HelperFunctions.calculateObjective(bestSolution);

        int delta;
        double p;
        double P;
        double T = 1;
        double stoppingCriterion = 10E-10;
        double alpha = 0.99;

        while (T > stoppingCriterion) {
            newSolution = LocalSearch.workerLocalSearch(instance, bestSolution, searchType);
            int newObjective = HelperFunctions.calculateObjective(newSolution);
            if (newObjective < bestObjective) {
                bestObjective = newObjective;
                bestSolution = newSolution;
            }
            else {
                delta = newObjective - bestObjective;
                T = alpha * T;
                Random r = new Random();
                p = r.nextDouble();
                P = Math.exp(- delta / T);
                if (p < P) {
                    bestObjective = newObjective;
                    bestSolution = newSolution;
                }
            }
        }

        return bestSolution;
    }
}