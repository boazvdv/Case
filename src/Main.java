// Packages used
import SimulationPackage.InstancePostNL;

import java.io.File;
import java.io.FileNotFoundException;
public class Main {
    public static void main(String[] args) {
        boolean generateResultsHeuristic = false;
        boolean generateResultsSimulation = true;

        // Read file
        File fileToRead = new File("DataChanged.txt");
        try {
            double[] arrayPenaltySameDestination = { 0, 0.3, 0.6 };
            boolean[] arrayRandomize = { false, true };

            for (double penaltySameDestination : arrayPenaltySameDestination) {
                for (boolean randomize : arrayRandomize) {
                    InstancePostNL instance = InstancePostNL.read(fileToRead);

                    double penaltyDistanceFront = 0.02;
                    instance.setPenaltyDistanceFront(penaltyDistanceFront);
                    instance.setPenaltySameDestination(penaltySameDestination);

                    // Generate results for heuristic
                    int numRunsHeuristic = 10;
                    if (generateResultsHeuristic) {
                        GenerateResults.Heuristic(numRunsHeuristic, instance);
                    }

                    // Generate results for simulation
                    int numRunsSimulation = 1000;
                    if (generateResultsSimulation) {
                        GenerateResults.Simulation(numRunsSimulation, instance, randomize);
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            System.out.println("There was an error reading file " + fileToRead);
            ex.printStackTrace();
        }
    }
}