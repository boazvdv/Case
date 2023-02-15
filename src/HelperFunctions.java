import java.util.ArrayList;

public class HelperFunctions {
    public static void printResults(Solution solution, PostInstance instance, boolean printChutes, boolean printWorkers) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        if (printChutes) {
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
        System.out.println("Objective value: " + calculateObjective(solution, instance));
    }

    public static double calculateObjective(Solution solution, PostInstance instance) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        double penaltyDistanceFront = instance.getPenaltyDistanceFront();
        double penaltySameDestination = instance.getPenaltySameDestination();

        int maxWorkload;
        int distanceFront;
        int sameDestination;

        // Calculate maximum workload
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

        maxWorkload = maxWorkloadLeft + maxWorkloadRight;

        // Calculate distance from front
        distanceFront = 0;
        for (Chute chute : chutes) {
            for (DestinationShift destinationShift : chute.getDestShiftAssignment()) {
                distanceFront += destinationShift.getExpectedContainers() * chute.getDistanceFront();
            }
        }

        // Calculate distance between the same destination
        sameDestination = 0;
        for (Chute firstChute : chutes) {
            for (Chute secondChute : chutes) {
                if (firstChute.getChuteNumber() < secondChute.getChuteNumber() && firstChute.getIsLeft() == secondChute.getIsLeft()) {
                    for (DestinationShift firstDestShift : firstChute.getDestShiftAssignment()) {
                        for (DestinationShift secondDestShift : secondChute.getDestShiftAssignment()) {
                            if (firstDestShift.getDestination() == secondDestShift.getDestination()) {
                                sameDestination += (secondChute.getDistanceFront() - firstChute.getDistanceFront());
                            }
                        }
                    }
                }
            }
        }

        return maxWorkload + penaltyDistanceFront * distanceFront + penaltySameDestination * sameDestination;
    }
}
