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
            Solution solution = ConstructionHeuristic.main(instance);

            // Calculate objective
            int objective = HelperFunctions.calculateObjective(solution);

            // Local search
            solution = LocalSearch.workerLocalSearch(instance, solution);

            // Print results
            HelperFunctions.printResults(solution);

        } catch (FileNotFoundException ex) {
            System.out.println("There was an error reading file " + fileToRead);
            ex.printStackTrace();
        }
    }
}