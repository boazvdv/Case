import SimulationPackage.InstancePostNL;

import static SimulationPackage.Main.runSimulation;

public class GenerateResults {
    public static void Heuristic(int numRuns, InstancePostNL instance) {
        double[] obj = new double[numRuns];
        double[] objMaxWorkload = new double[numRuns];
        double[] objDistanceFront = new double[numRuns];
        double[] objSameDestination = new double[numRuns];
        double[] runningTimes = new double[numRuns];

        System.out.println("Generating results using heuristic...");
        System.out.println("[ a = " + instance.getPenaltyDistanceFront() + " | B = " + instance.getPenaltySameDestination() + " ]");
        for (int i = 0; i < numRuns; i++) {
            System.out.print("\r" + "Run " + (i+1) + "/" + (numRuns));
            long begin = System.nanoTime();
            Solution solution = SimulatedAnnealing.main(instance);
            long end = System.nanoTime();
            long elapsedTime = end - begin;
            double seconds = (double) elapsedTime / 1_000_000_000.0;

            obj[i] = solution.getObjective(instance);
            objMaxWorkload[i] = solution.getMaxWorkload();
            objDistanceFront[i] = solution.getDistanceFront();
            objSameDestination[i] = solution.getSameDestination();

            runningTimes[i] = seconds;
        }
        System.out.println("\nAverage objective: " + arrayAverage(obj));
        System.out.println("Average 'workload' objective: " + arrayAverage(objMaxWorkload));
        System.out.println("Average 'distance front' objective: " + arrayAverage(objDistanceFront));
        System.out.println("Average 'same destination' objective: " + arrayAverage(objSameDestination));
        System.out.println("Average running time: " + arrayAverage(runningTimes));

    }
    public static void Simulation(int numRuns, InstancePostNL instance) {
        System.out.println("Generating solution using heuristic...\n");
        long begin = System.nanoTime();
        Solution solution = SimulatedAnnealing.main(instance);
        long end = System.nanoTime();
        long elapsedTime = end - begin;
        double seconds = (double) elapsedTime / 1_000_000_000.0;

        boolean printChutes = true;
        boolean printWorkers = true;
        HelperFunctions.printResults(solution, instance, printChutes, printWorkers);

        int[][] destinationShiftChuteMatrix = HelperFunctions.createDestinationShiftChuteMatrix(solution, instance);
        int[][] workerChuteMatrix = HelperFunctions.createWorkerChuteMatrix(solution, instance);

        System.out.println("\nSolution generated using heuristic in " + seconds + " seconds");
        System.out.println("Running simulation...");
        instance.createInitialSequence();
        runSimulation(instance, workerChuteMatrix, destinationShiftChuteMatrix, numRuns);
    }

    public static double arrayAverage(double[] inputArray) {
        double length = inputArray.length;
        double sum = 0;
        for (double v : inputArray) {
            sum += v;
        }
        return sum / length;
    }
}