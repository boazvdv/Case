import Objects.Chute;
import Objects.DestinationShift;
import Objects.Solution;
import Objects.Worker;
import SimulationPackage.InstancePostNL;

import java.util.ArrayList;

public class HelperFunctions {
    public static void printResults(Solution solution, InstancePostNL instance, boolean printChutes, boolean printWorkers) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        if (printChutes) {
            for (Chute chute : chutes) {
                System.out.print("Chute: " + chute.getChuteNumber() + "  -  D/S: [");
                ArrayList<DestinationShift> assignment = chute.getDestShiftAssignment();
                for (DestinationShift destShift : assignment) {
                    System.out.print("(" + destShift.getDestination() + ", " + destShift.getShift() + ") ");
                }
                System.out.print("]  -  Expected containers: " + chute.getExpectedContainers());
                System.out.println();
            }
            System.out.println();
        }

        if (printWorkers) {
            for (Worker worker : workers) {
                System.out.print("Worker: " + worker.getWorkerNumber() + "  -  Chutes: [");
                ArrayList<Chute> assignment = worker.getChuteAssignment();
                for (Chute chute : assignment) {
                    System.out.print(chute.getChuteNumber() + " ");
                }
                System.out.print("]  -  Expected containers: " + worker.getExpectedContainers());
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("Objective value: " + solution.getObjective(instance));
    }
    public static int[][] createDestinationShiftChuteMatrix(Solution solution, InstancePostNL instance) {
        // Create D/S to chute matrix

        int maxShift = 0;
        for (int[] destShift : instance.getDestShift()) {
            if (destShift[1] > maxShift) {
                maxShift = destShift[1];
            }
        }

        int[][] destinationShiftChuteMatrix = new int[instance.getNumberOfDest()][maxShift+1];

        for (Chute chute : solution.getChutes()) {
            for (DestinationShift destShift : chute.getDestShiftAssignment()) {
                destinationShiftChuteMatrix[destShift.getDestination()][destShift.getShift()] = chute.getChuteNumber();
            }
        }

        return destinationShiftChuteMatrix;
    }

    public static int[][] createWorkerChuteMatrix(Solution solution, InstancePostNL instance) {
        // Create worker to chute matrix
        int[][] workerChuteMatrix = new int[instance.getEmployees()][instance.getNumberOfChutes()];

        for (Worker worker : solution.getWorkers()) {
            for (Chute chute : worker.getChuteAssignment()) {
                workerChuteMatrix[worker.getWorkerNumber()][chute.getChuteNumber()] = 1;
            }
        }

        return workerChuteMatrix;
    }
}
