import java.util.Random;

public class SimulatedAnnealing {
    public static Solution main(PostInstance instance) {

        int chuteSearchType = 2;
        int workerSearchType = 2;

        // Construct initial solution
        Solution newSolution = ConstructionHeuristic.main(instance);
        Solution bestSolution = newSolution.clone();
        Solution minSolution = newSolution.clone();
        int bestObjective = HelperFunctions.calculateObjective(bestSolution);
        int minObjective = bestObjective;

        int delta;
        double p;
        double P;
        double T = 1;
        double stoppingCriterion = 10E-2;
        double alpha = 0.99;

        boolean searchChutes = true;
        Random r = new Random();

        while (T > stoppingCriterion) {
            System.out.println(searchChutes);
            System.out.println(T);
            if (searchChutes) {
                newSolution = LocalSearch.chuteLocalSearch(instance, bestSolution, chuteSearchType);
                searchChutes = false;
            }
            else {
                newSolution = LocalSearch.workerLocalSearch(instance, bestSolution, workerSearchType);
                searchChutes = true;
            }
            int newObjective = HelperFunctions.calculateObjective(newSolution);
            if (newObjective < bestObjective) {
                bestObjective = newObjective;
                bestSolution = newSolution.clone();

                minObjective = newObjective;
                minSolution = newSolution.clone();
            }
            else {
                delta = newObjective - bestObjective;
                T = alpha * T;
                p = r.nextDouble();
                P = Math.exp(- delta / T);
                if (p < P) {
                    bestObjective = newObjective;
                    bestSolution = newSolution.clone();
                }
            }
        }
        System.out.println("Min. objective: " + minObjective);
        return minSolution;
    }
}