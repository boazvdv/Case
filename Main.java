// Packages used
import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        File fileToRead = new File("DataChanged.txt");

        try {
            // Read file
            PostInstance instance = PostInstance.read(fileToRead);

            // Construct initial solution
            Solution initialSolution = ConstructionHeuristic.main(instance);

            // Calculate objective
            int objective = HelperFunctions.calculateObjective(initialSolution);

            // Print results
            HelperFunctions.printResults(initialSolution);

            // Local search
            Solution newSolution = LocalSearch.workerLocalSearch(instance, initialSolution);

            // Print results
            HelperFunctions.printResults(newSolution);

        } catch (FileNotFoundException ex) {
            System.out.println("There was an error reading file " + fileToRead);
            ex.printStackTrace();
        }
    }
}