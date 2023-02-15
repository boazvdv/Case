// Packages used
import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        File fileToRead = new File("DataChanged.txt");

        try {
            // Read file
            PostInstance instance = PostInstance.read(fileToRead);
            double penaltyDistanceFront = 0.1;
            double penaltySameDestination = 0.1;
            instance.setPenaltyDistanceFront(penaltyDistanceFront);
            instance.setPenaltySameDestination(penaltySameDestination);

            Solution solution = SimulatedAnnealing.main(instance);

            boolean printChutes = true;
            boolean printWorkers = true;


            HelperFunctions.printResults(solution, instance, printChutes, printWorkers);

        } catch (FileNotFoundException ex) {
            System.out.println("There was an error reading file " + fileToRead);
            ex.printStackTrace();
        }
    }
}