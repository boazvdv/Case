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
            // Generate results for heuristic
            InstancePostNL instance = InstancePostNL.read(fileToRead);
            double penaltyDistanceFront = 0.02;
            double penaltySameDestination = 0.3;
            instance.setPenaltyDistanceFront(penaltyDistanceFront);
            instance.setPenaltySameDestination(penaltySameDestination);

            int numRunsHeuristic = 10;
            if (generateResultsHeuristic) {
                GenerateResults.Heuristic(numRunsHeuristic, instance);
            }

            int numRunsSimulation = 1000;
            if (generateResultsSimulation) {
                GenerateResults.Simulation(numRunsSimulation, instance);
            }

        } catch (FileNotFoundException ex) {
            System.out.println("There was an error reading file " + fileToRead);
            ex.printStackTrace();
        }
    }
}