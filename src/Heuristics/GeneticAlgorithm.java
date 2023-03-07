package Heuristics;

import SimulationPackage.InstancePostNL;
import Objects.*;

import java.util.*;

public class GeneticAlgorithm {
    public static Solution main(InstancePostNL instance) {
        System.out.println();
        int pairsParents = 50;
        int numParents = 2 * pairsParents;
        int numInitialSolutions = 20 * numParents;
        int numLocalSearches = 50;
        int numMutations = 250;

        double bestObj = Double.MAX_VALUE;
        double newObj;

        Solution bestSolution = ConstructionHeuristic.main(instance);

        // Construct initial solutions
        ArrayList<Solution> population = new ArrayList<>();
        for (int i = 0; i < numInitialSolutions; i++) {
            Solution newInitialSolution = ConstructionHeuristic.main(instance);
            for (int j = 0; j < numLocalSearches; j++) {
                newInitialSolution = LocalSearch.chuteLocalSearch(instance, newInitialSolution, 1);
                newInitialSolution = LocalSearch.workerLocalSearch(instance, newInitialSolution, 1);
                newInitialSolution = LocalSearch.chuteLocalSearch(instance, newInitialSolution, 2);
                newInitialSolution = LocalSearch.workerLocalSearch(instance, newInitialSolution, 2);
            }
            newObj = newInitialSolution.getObjective(instance);
            if (newObj < bestObj) {
                bestObj = newObj;
                bestSolution = newInitialSolution;
                System.out.println("New best objective (initialization): " + bestObj);
            }
            population.add(newInitialSolution);
        }

        int iterationsWithoutImprovement = 0;
        int i = 0;
        while (iterationsWithoutImprovement < 20) {
            i += 1;
            Integer[] indicesParents = indicesBestParents(population, instance, numParents);
            List<Integer> intList = Arrays.asList(indicesParents);
            Collections.shuffle(intList);
            intList.toArray(indicesParents);
            int count = 0;
            Solution[] parents = new Solution[2];
            for (int index: indicesParents) {
                parents[count] = population.get(index);
                count += 1;
                if (count == 2) {
                    count = 0;
                    Solution firstParent = parents[0];
                    Solution secondParent = parents[1];
                    HashMap<Integer, ArrayList<Chute>> chuteAssignment = SimulatedAnnealing.saveChuteAssignment(firstParent);
                    HashMap<Integer, ArrayList<DestinationShift>> destShiftAssignment = SimulatedAnnealing.saveDestShiftAssignment(secondParent);
                    Solution newChild = ConstructionHeuristic.main(instance);
                    SimulatedAnnealing.revertSolution(chuteAssignment, destShiftAssignment, newChild);
                    for (int j = 0; j < numLocalSearches; j++) {
                        newChild = LocalSearch.chuteLocalSearch(instance, newChild, 1);
                        newChild = LocalSearch.workerLocalSearch(instance, newChild, 1);
                        newChild = LocalSearch.chuteLocalSearch(instance, newChild, 2);
                        newChild = LocalSearch.workerLocalSearch(instance, newChild, 2);
                    }
                    population.add(newChild);
                    newObj = newChild.getObjective(instance);
                    if (newObj < bestObj) {
                        bestObj = newObj;
                        bestSolution = newChild;
                        iterationsWithoutImprovement = 0;
                    }
                    else {
                        iterationsWithoutImprovement += 1;
                    }
                }
            }
            System.out.println("New best objective (mutation " + i +"): " + bestObj);
            System.out.println("Max workload obj: " + bestSolution.getMaxWorkload());
            System.out.println("Distance front obj: " + bestSolution.getDistanceFront());
            System.out.println("Same destination obj: " + bestSolution.getSameDestination());
        }

        System.out.println("\n" + bestObj);
        return bestSolution;
    }
    public static int[] uniqueRandomNumbers(int populationSize, int numParents) {
        int[] indicesParents = new int[numParents];
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < populationSize; i++) list.add(i);
        Collections.shuffle(list);
        for (int i = 0; i < numParents; i++) indicesParents[i] = list.get(i);
        return indicesParents;
    }

    public static Integer[] indicesBestParents(ArrayList<Solution> population, InstancePostNL instance, int numParents) {
        double[][] objectives = new double[population.size()][2];
        for (int i = 0; i < population.size(); i++) {
            objectives[i][0] = population.get(i).getObjective(instance);
            objectives[i][1] = i;
        }
        sortByColumn(objectives, 0);
        Integer[] indices = new Integer[numParents];
        for (int i = 0; i < numParents; i++) {
            indices[i] = (int)objectives[i][1];
        }
        return indices;
    }

    public static void sortByColumn(double arr[][], int col)
    {
        Arrays.sort(arr, new Comparator<double[]>() {
            @Override
            public int compare(final double[] entry1,
                               final double[] entry2) {
                if (entry1[col] >= entry2[col])
                    return 1;
                else
                    return -1;
            }
        });
    }
}
