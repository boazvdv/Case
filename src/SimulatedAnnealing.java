import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;

public class SimulatedAnnealing {
    public static Solution main(PostInstance instance) {

        int searchType = 2;

        // Construct initial solution
        Solution bestSolution = ConstructionHeuristic.main(instance);
        int bestObjective = HelperFunctions.calculateObjective(bestSolution);

        int numIterationsWithoutImprovement = 0;

        Solution newSolution = LocalSearch.workerLocalSearch(instance, bestSolution, searchType);
        int newObjective = HelperFunctions.calculateObjective(newSolution);

        return bestSolution;
    }
}