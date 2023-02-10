import java.util.ArrayList;

public class HelperFunctions {
    public static void printResults(Solution solution) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();
        for (Chute chute : chutes) {
            System.out.print("Chute: " + chute.getChuteNumber() + "  -  D/S: [");
            ArrayList< DestinationShift > assignment = chute.getDestShiftAssignment();
            for (DestinationShift destShift : assignment) {
                System.out.print("(" + destShift.getDestination() + ", " + destShift.getShift() + ") ");
            }
            System.out.print("]  -  Expected containers: " + chute.getExpectedContainers());
            System.out.println();
        }
        System.out.println();

        for (Worker worker : workers) {
            System.out.print("Worker: " + worker.getWorkerNumber() + "  -  Chutes: [");
            ArrayList < Chute > assignment = worker.getChuteAssignment();
            for (Chute chute : assignment) {
                System.out.print(chute.getChuteNumber() + " ");
            }
            System.out.print("]  -  Expected containers: " + worker.getExpectedContainers());
            System.out.println();
        }
        System.out.println();
    }

    public static int calculateObjective(Solution solution) {
        Worker[] workers = solution.getWorkers();

        int maxWorkloadLeft = 0;
        int maxWorkloadRight = 0;

        for (Worker worker : workers) {
            int sumExpectedContainers = 0;
            ArrayList<Chute> assignment = worker.getChuteAssignment();
            for (Chute chute : assignment) {
                sumExpectedContainers += chute.getExpectedContainers();
            }
            if (worker.getIsLeft() == 0) {
                if (sumExpectedContainers > maxWorkloadLeft) {
                    maxWorkloadLeft = sumExpectedContainers;
                }
            } else {
                if (sumExpectedContainers > maxWorkloadRight) {
                    maxWorkloadRight = sumExpectedContainers;
                }
            }
        }
        int sumWorkload = maxWorkloadLeft + maxWorkloadRight;
        return sumWorkload;
    }
}
