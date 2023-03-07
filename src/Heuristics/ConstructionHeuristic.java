package Heuristics;

import Objects.*;
import SimulationPackage.InstancePostNL;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ConstructionHeuristic {
    public static Solution main(InstancePostNL instance) {
        Chute[] chutes = assignChutes(instance);
        Worker[] workers = assignWorkers(instance, chutes);
        return new Solution(chutes, workers);
    }

    private static Chute[] assignChutes(InstancePostNL instance) {
        Chute[] chutes = initializeChutes(instance);
        DestinationShift[] destShifts = initializeDestinationShifts(instance);

        int numDest = instance.getNumberOfDest();
        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            ArrayList<Chute> chutesSide = new ArrayList<>();
            for (Chute chute : chutes) {
                if (chute.getIsLeft() == leftIndicator) {
                    chutesSide.add(chute);
                }
            }
            double[][] avgParcelsPerShift = new double[numDest][2];
            for (int d = 0; d < numDest; d++) {
                int parcels = 0;
                int shifts = 0;
                avgParcelsPerShift[d][0] = d;
                for (DestinationShift destShift : destShifts) {
                    if (destShift.getDestination() == d && destShift.getIsLeft() == leftIndicator) {
                        parcels += destShift.getExpectedContainers();
                        shifts++;
                    }
                }
                if (shifts == 0)
                    avgParcelsPerShift[d][1] = -1;
                else
                    avgParcelsPerShift[d][1] = 1.0 * parcels / shifts;
            }

            sortByColumnDouble(avgParcelsPerShift, 1);

            boolean[] allocated = new boolean[numDest];

            for (int d = 0; d < numDest; d++)
                if (avgParcelsPerShift[d][1] < -0.5)
                    allocated[(int) Math.round(avgParcelsPerShift[d][0])] = true;

            int curChute = 0;
            Chute currentChute = chutesSide.get(curChute);

            int curDest = (int) Math.round(avgParcelsPerShift[0][0]);
            boolean[][] blocked = instance.getBlocked();
            while (!areAllTrue(allocated)) {

                int row = 0;
                while (!canAddDest(curDest, currentChute, blocked) || allocated[curDest]) {
                    row++;
                    curDest = (int) Math.round(avgParcelsPerShift[row][0]);
                }
                allocated[curDest] = true;

                for (DestinationShift destShift : destShifts) {
                    if (destShift.getDestination() == curDest && destShift.getIsLeft() == leftIndicator) {
                        ArrayList<DestinationShift> destShiftAssignment = currentChute.getDestShiftAssignment();
                        destShiftAssignment.add(destShift);
                        chutesSide.get(curChute).setDestShiftAssignment(destShiftAssignment);
                        if (currentChute.getDestShiftAssignment().size() >= currentChute.getMaxContainers()) {
                            curChute++;
                            currentChute = chutesSide.get(curChute);
                        }
                    }
                }
            }
        }
        return chutes;
    }

    private static Worker[] assignWorkers(InstancePostNL instance, Chute[] chutes) {

        Worker[] workers = initializeWorkers(instance);
        int maxChutesPerWorker = instance.getMaxChutesPerWorker();
        int numWorkers = instance.getEmployees();
        int numWorkersPerSide = numWorkers / 2;

        // Set expected number of containers per chute
        for (Chute chute : chutes) {
            chute.updateExpectedContainers();
        }

        // Separate into L / R side
        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            // Create array with chutes on corresponding side
            // Create queue with chutes on corresponding side
            ArrayList < Chute > chutesSide = new ArrayList<>();
            Queue < Chute > chutesSideQueue = new LinkedList<>();
            for (Chute chute : chutes) {
                if (chute.getIsLeft() == leftIndicator) {
                    chutesSide.add(chute);
                    chutesSideQueue.add(chute);
                }
            }

            // Create queue for workers
            Queue < Worker > workerQueue = new LinkedList < > ();
            for (Worker worker : workers) {
                if (worker.getIsLeft() == leftIndicator) {
                    workerQueue.add(worker);
                }
            }

            // Find average containers to handle per worker
            float totalExpectedContainers = 0;
            for (Chute chute : chutesSide) {
                totalExpectedContainers += chute.getExpectedContainers();
            }
            float avgContainersPerWorker = totalExpectedContainers / numWorkersPerSide;

            // Get first worker in line
            Worker currentWorker = workerQueue.remove();
            // Keep adding chutes to workers according to [conditions]
            while (true) {
                Chute currentChute = chutesSideQueue.remove();
                ArrayList < Chute > assignment = currentWorker.getChuteAssignment();
                assignment.add(currentChute);
                currentWorker.setChuteAssignment(assignment);

                if (chutesSideQueue.isEmpty()) {
                    break;
                }

                int totalExpectedContainersCurrentWorker = 0;
                for (Chute chute: currentWorker.getChuteAssignment()) {
                    totalExpectedContainersCurrentWorker += chute.getExpectedContainers();
                }
                if ((currentWorker.getChuteAssignment().size() >= maxChutesPerWorker ||
                        totalExpectedContainersCurrentWorker >= avgContainersPerWorker) &&
                        chutesSideQueue.size() <= maxChutesPerWorker * workerQueue.size()) {
                    currentWorker = workerQueue.remove();
                }
            }
        }
        return workers;
    }

    public static Chute[] initializeChutes(InstancePostNL instance) {
        // Get chutes data
        int[][] chutesData = instance.getChutes();
        int minDistanceFront = Integer.MAX_VALUE;
        int maxDistanceFront = Integer.MIN_VALUE;
        for (int[] chute : chutesData) {
            int distanceFront = chute[1];
            if (distanceFront < minDistanceFront) {
                minDistanceFront = distanceFront;
            }
            if (distanceFront > maxDistanceFront) {
                maxDistanceFront = distanceFront;
            }
        }

        // Initialize array of chutes
        Chute[] chutes = new Chute[chutesData.length];
        for (int i = 0; i < chutes.length; i++) {
            int[] chute = chutesData[i];
            ArrayList<Chute> neighboringChutes = new ArrayList<>();
            Chute newChute = new Chute(i, chute[0], chute[1], chute[2], neighboringChutes);
            chutes[i] = newChute;
        }

        // Add neighboring chutes
        for (int i = 0; i < chutes.length; i++) {
            if (chutes[i].getDistanceFront() > minDistanceFront) {
                chutes[i].addNeighboringChute(chutes[i-1]);
            }
            if (chutes[i].getDistanceFront() < maxDistanceFront) {
                chutes[i].addNeighboringChute(chutes[i+1]);
            }
        }
        return chutes;
    }

    public static DestinationShift[] initializeDestinationShifts(InstancePostNL instance) {
        // Get destination/shift data
        // Sort destination/shift combinations by expected number of containers
        int[][] sortedDestShiftsData = instance.getDestShift();
        sortByColumnInt(sortedDestShiftsData, 3);

        // Initialize array of destination/shift combinations
        DestinationShift[] destShifts = new DestinationShift[sortedDestShiftsData.length];
        for (int i = 0; i < sortedDestShiftsData.length; i++) {
            int[] destShift = sortedDestShiftsData[i];
            DestinationShift newDestShift = new DestinationShift(destShift[0], destShift[1], destShift[2],
                    destShift[3]);
            destShifts[i] = newDestShift;
        }
        return destShifts;
    }

    public static Worker[] initializeWorkers(InstancePostNL instance) {
        // Initialize settings for workers
        int numWorkers = instance.getEmployees();
        int numWorkersPerSide = numWorkers / 2;

        // Initialize array of workers
        Worker[] workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            int workerIsLeft = (i >= numWorkersPerSide) ? 1 : 0;
            ArrayList<Worker> neighboringWorkers = new ArrayList<>();
            workers[i] = new Worker(i, workerIsLeft, neighboringWorkers);
        }

        for (int i = 0; i < workers.length; i++) {
            if ((i > 0 && i <= numWorkersPerSide - 1) || (i > numWorkersPerSide && i <= numWorkers - 1)) {
                workers[i-1].addNeighboringWorker(workers[i]);
            }
            if ((i < numWorkersPerSide - 1) || (i >= numWorkersPerSide && i < numWorkers - 1)) {
                workers[i+1].addNeighboringWorker(workers[i]);
            }
        }
        return workers;
    }

    public static boolean canAdd(DestinationShift ds, Chute chute, boolean[][] blocked) {
        for (DestinationShift inChute : chute.getDestShiftAssignment()) {
            if (blocked[inChute.getDestination()][ds.getDestination()])
                return false;
        }
        return true;
    }
    public static boolean canAddDest(int destination, Chute chute, boolean[][] blocked) {
        for (DestinationShift inChute : chute.getDestShiftAssignment()) {
            if (blocked[inChute.getDestination()][destination])
                return false;
        }
        return true;
    }

    public static void sortByColumnInt(int[][] arr, int col) {
        // Using built-in sort function Arrays.sort
        // Compare values according to columns
        Arrays.sort(arr, (entry1, entry2) -> {
            // To sort in descending order
            return Integer.compare(entry2[col], entry1[col]);
        });
    }

    public static void sortByColumnDouble(double[][] arr, int col) {
        // Using built-in sort function Arrays.sort
        // Compare values according to columns
        Arrays.sort(arr, (entry1, entry2) -> {
            // To sort in descending order
            return Double.compare(entry2[col], entry1[col]);
        });
    }

    static boolean areAllTrue(boolean[] array) {
        for (boolean b : array)
            if (!b)
                return false;
        return true;
    }
}
