import SimulationPackage.InstancePostNL;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
public class ConstructionHeuristic {
    public static Solution main(InstancePostNL instance) {
        Chute[] chutes = initializeChutes(instance);

        DestinationShift[] destShifts = initializeDestinationShifts(instance);

        // Get matrix with blocked postal codes
        boolean[][] blocked = instance.getBlocked();

        // Separate into L / R side
        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            // Create array with chutes on corresponding side
            ArrayList< Chute > chutesSide = new ArrayList <> ();
            for (Chute chute : chutes) {
                if (chute.getIsLeft() == leftIndicator) {
                    chutesSide.add(chute);
                }
            }

            // Create queue with unassigned destination/shift combinations
            Queue<DestinationShift> unassignedDestShifts = new LinkedList<>();
            for (DestinationShift destShift : destShifts) {
                if (destShift.getIsLeft() == leftIndicator) {
                    unassignedDestShifts.add(destShift);
                }
            }

            // Assign destination/shift combinations to chutes
            while (!unassignedDestShifts.isEmpty()) {
                DestinationShift currentDestShift = unassignedDestShifts.remove();
                int currentDest = currentDestShift.getDestination();
                boolean assigned = false;
                int i = 0;

                // Check all possible chutes as long as D/S combination is not assigned
                while (i < chutesSide.size() & !assigned) {
                    Chute currentChute = chutesSide.get(i);
                    ArrayList < DestinationShift > currentAssignment = currentChute.getDestShiftAssignment();
                    if (currentAssignment.size() < currentChute.getMaxContainers()) {
                        boolean postalCodeBlock = false;
                        for (DestinationShift tempDestShift : currentAssignment) {
                            if (blocked[tempDestShift.getDestination()][currentDest]) {
                                postalCodeBlock = true;
                                break;
                            }
                        }
                        if (!postalCodeBlock) {
                            currentAssignment = currentChute.getDestShiftAssignment();
                            currentAssignment.add(currentDestShift);
                            currentChute.setDestShiftAssignment(currentAssignment);
                            assigned = true;
                        }
                    }
                    i += 1;
                }

                // Randomly swap out conflicting D/S combination if no chute could be assigned due to postal code
                if (!assigned) {
                    ArrayList < DestinationShift > tempAssignment;
                    Chute chuteTemp;
                    while (true) {
                        Random generator = new Random();
                        int randomIndex = generator.nextInt(chutesSide.size());
                        chuteTemp = chutesSide.get(randomIndex);
                        tempAssignment = chuteTemp.getDestShiftAssignment();
                        int numViolations = 0;
                        for (DestinationShift tempDestShift : tempAssignment) {
                            if (blocked[currentDest][tempDestShift.getDestination()]) {
                                numViolations += 1;
                            }
                        }
                        if (numViolations > 0) {
                            break;
                        }
                    }
                    ArrayList <DestinationShift> newAssignment = new ArrayList<>();
                    for (DestinationShift tempDestShift : tempAssignment) {
                        int tempDest = tempDestShift.getDestination();
                        if (!blocked[currentDest][tempDest]) {
                            newAssignment.add(tempDestShift);
                        } else {
                            unassignedDestShifts.add(tempDestShift);
                        }
                    }
                    newAssignment.add(currentDestShift);
                    chuteTemp.setDestShiftAssignment(newAssignment);
                }
            }
        }

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
        return new Solution(chutes, workers);
    }

    public static Chute[] initializeChutes(InstancePostNL instance) {
        // Get chutes data
        // Initialize array of chutes
        // Add neighboring chutes
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

        Chute[] chutes = new Chute[chutesData.length];
        for (int i = 0; i < chutes.length; i++) {
            int[] chute = chutesData[i];
            ArrayList<Chute> neighboringChutes = new ArrayList<>();
            Chute newChute = new Chute(i, chute[0], chute[1], chute[2], neighboringChutes);
            chutes[i] = newChute;
        }

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
        // Initialize array of destination/shift combinations
        int[][] sortedDestShiftsData = instance.getDestShift();
        sortByColumn(sortedDestShiftsData, 3);

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
            int workerIsLeft = 0;
            ArrayList<Worker> neighboringWorkers = new ArrayList<>();
            if (i >= numWorkersPerSide) {
                workerIsLeft = 1;
            }
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
    public static void sortByColumn(int[][] arr, int col) {
        // Using built-in sort function Arrays.sort
        // Compare values according to columns
        Arrays.sort(arr, (entry1, entry2) -> {
            // To sort in descending order
            return Integer.compare(entry2[col], entry1[col]);
        });
    }
}