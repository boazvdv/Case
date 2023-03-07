package Heuristics;

import Objects.Chute;
import Objects.DestinationShift;
import Objects.Solution;
import Objects.Worker;
import SimulationPackage.InstancePostNL;

import java.util.ArrayList;
import java.util.Random;

public class LocalSearch {

    public static Solution chuteLocalSearch(InstancePostNL instance, Solution solution, int chuteSearchType) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        if (chuteSearchType == 1) {
            Random generator = new Random();
            while (true) {
                int randomIndex = generator.nextInt(chutes.length);
                int otherRandomIndex = generator.nextInt(chutes.length);
                Chute chute = chutes[randomIndex];
                Chute otherChute = chutes[otherRandomIndex];
                if (chute.getChuteNumber() != otherChute.getChuteNumber() && chute.getIsLeft() == otherChute.getIsLeft() && chute.getDestShiftAssignment().size()!=0 && otherChute.getDestShiftAssignment().size()!=0) {
                    int randomDsIndex = generator.nextInt(chute.getDestShiftAssignment().size());
                    int otherRandomDsIndex = generator.nextInt(otherChute.getDestShiftAssignment().size());
                    DestinationShift ds = chute.getDestShiftAssignment().get(randomDsIndex);
                    DestinationShift otherDs = otherChute.getDestShiftAssignment().get(otherRandomDsIndex);
                    // check if postal code block isn't violated in first chute
                    boolean blocked = false;
                    for (int k = 0; k < chute.getDestShiftAssignment().size() - 1; k++) {
                        if (instance.getBlocked()[chute.getDestShiftAssignment().get(k).getDestination()][otherDs.getDestination()] && chute.getDestShiftAssignment().get(k) != ds) {
                            blocked = true;
                        }
                    }
                    // check if postal code block isn't violated in second chute
                    for (int l = 0; l < otherChute.getDestShiftAssignment().size() - 1; l++) {
                        if (instance.getBlocked()[otherChute.getDestShiftAssignment().get(l).getDestination()][ds.getDestination()] && otherChute.getDestShiftAssignment().get(l) != otherDs) {
                            blocked = true;
                        }
                    }

                    boolean neighbouringChutesBlock = false;

                    int i = otherChute.getChuteNumber();
                    for (int j = 0; j < 40; j++) {
                        if(Math.abs(i-j) > 2) {
                            for(DestinationShift desShift : chutes[j].getDestShiftAssignment()  ) {
                                if (ds.getDestination() == desShift.getDestination()) {
                                    neighbouringChutesBlock = true;
                                    break;
                                }
                            }
                        }
                    }

                    int k = chute.getChuteNumber();
                    for (int j = 0; j < 40; j++) {
                        if(Math.abs(k - j) > 2) {
                            for(DestinationShift desShift : chutes[j].getDestShiftAssignment()  ) {
                                if (otherDs.getDestination() == desShift.getDestination()) {
                                    neighbouringChutesBlock = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!blocked && !neighbouringChutesBlock) {
                        ArrayList<DestinationShift> newAssignment = chute.getDestShiftAssignment();
                        ArrayList<DestinationShift> otherNewAssignment = otherChute.getDestShiftAssignment();

                        newAssignment.remove(ds);
                        newAssignment.add(otherDs);

                        otherNewAssignment.remove(otherDs);
                        otherNewAssignment.add(ds);

                        chute.setDestShiftAssignment(newAssignment);
                        otherChute.setDestShiftAssignment(otherNewAssignment);

                        return new Solution(chutes, workers);

                    }
                }
            }
        }
        else if (chuteSearchType == 2) {
            Random generator = new Random();
            while(true) {
                int randomIndex = generator.nextInt(chutes.length);
                int otherRandomIndex = generator.nextInt(chutes.length);
                Chute chute = chutes[randomIndex];
                Chute otherChute = chutes[otherRandomIndex];
                if(chute.getChuteNumber() != otherChute.getChuteNumber() && chute.getIsLeft() == otherChute.getIsLeft() && chute.getDestShiftAssignment().size()!=0) {
                    int randomDs = generator.nextInt(chute.getDestShiftAssignment().size());
                    DestinationShift ds = chute.getDestShiftAssignment().get(randomDs);
                    boolean blocked = false;
                    for(int l = 0; l < otherChute.getDestShiftAssignment().size()-1; l++) {
                        if(instance.getBlocked()[otherChute.getDestShiftAssignment().get(l).getDestination()][ds.getDestination()]) {
                            blocked = true;
                        }
                    }

                    boolean neighbouringChutesBlock = false;

                    int i = otherChute.getChuteNumber();
                    for (int j = 0; j < 40; j++) {
                        if(Math.abs(i-j) > 2) {
                            for(DestinationShift desShift : chutes[j].getDestShiftAssignment()  ) {
                                if (ds.getDestination() == desShift.getDestination()) {
                                    neighbouringChutesBlock = true;
                                    break;
                                }
                            }
                        }
                    }

                    if(!blocked && !neighbouringChutesBlock && otherChute.getDestShiftAssignment().size() < otherChute.getMaxContainers()) {
                        ArrayList<DestinationShift> newAssignment = chute.getDestShiftAssignment();
                        ArrayList<DestinationShift> otherNewAssignment = otherChute.getDestShiftAssignment();

                        newAssignment.remove(ds);
                        otherNewAssignment.add(ds);

                        chute.setDestShiftAssignment(newAssignment);
                        otherChute.setDestShiftAssignment(otherNewAssignment);


                        return new Solution(chutes, workers);
                    }

                }
            }
        }
        return new Solution(chutes, workers);
    }

    public static Solution workerLocalSearch(InstancePostNL instance, Solution solution, int searchType) {
        Chute[] chutes = solution.getChutes();
        Worker[] workers = solution.getWorkers();

        for (int leftIndicator = 0; leftIndicator <= 1; leftIndicator++) {
            Worker[] workersSide = new Worker[workers.length / 2];
            int i = 0;
            for (Worker worker: workers) {
                if (worker.getIsLeft() == leftIndicator) {
                    workersSide[i] = worker;
                    i += 1;
                }
            }

            if (searchType == 1) {
                // Choose random worker, remove one of its chutes and add to a neighbor
                boolean swapFound = false;
                while (!swapFound) {
                    Random generator = new Random();
                    int randomIndex = generator.nextInt(workersSide.length);
                    Worker workerRemove = workersSide[randomIndex];

                    ArrayList < Worker > neighbors = workerRemove.getNeighboringWorkers();
                    randomIndex = generator.nextInt(neighbors.size());
                    Worker workerAdd = neighbors.get(randomIndex);
                    if (workerAdd.getChuteAssignment().size() < instance.getMaxChutesPerWorker()) {
                        swapFound = true;
                        ArrayList < Chute > assignmentRemove = workerRemove.getChuteAssignment();
                        ArrayList < Chute > assignmentAdd = workerAdd.getChuteAssignment();
                        if (workerRemove.getWorkerNumber() < workerAdd.getWorkerNumber()) {
                            Chute chuteToMove = assignmentRemove.remove(assignmentRemove.size() - 1);
                            assignmentAdd.add(0, chuteToMove);
                        } else {
                            Chute chuteToMove = assignmentRemove.remove(0);
                            assignmentAdd.add(chuteToMove);
                        }
                        workerRemove.setChuteAssignment(assignmentRemove);
                        workerAdd.setChuteAssignment(assignmentAdd);
                    }
                }
            } else if (searchType == 2) {
                // Get the busiest worker and the least busy worker with spare capacity
                // Move chute from busiest to the least busy
                Worker busiestWorker = workersSide[0];
                Worker leastBusyWorker = workersSide[0];
                for (Worker worker: workersSide) {
                    if (worker.getExpectedContainers() > busiestWorker.getExpectedContainers()) {
                        busiestWorker = worker;
                    }
                    if (worker.getExpectedContainers() < leastBusyWorker.getExpectedContainers()) {
                        if (worker.getChuteAssignment().size() < instance.getMaxChutesPerWorker()) {
                            leastBusyWorker = worker;
                        }
                    }
                }
                boolean destinationReached = busiestWorker.getWorkerNumber() == leastBusyWorker.getWorkerNumber();
                Worker workerRemove = busiestWorker;
                Worker workerAdd = busiestWorker;
                ArrayList < Chute > assignmentRemove;
                ArrayList < Chute > assignmentAdd;
                while (!destinationReached) {
                    ArrayList < Worker > neighbors = workerRemove.getNeighboringWorkers();
                    if (busiestWorker.getWorkerNumber() > leastBusyWorker.getWorkerNumber()) {
                        // Move chute downstream
                        for (Worker neighbor: neighbors) {
                            if (neighbor.getWorkerNumber() < workerRemove.getWorkerNumber()) {
                                workerAdd = neighbor;
                                break;
                            }
                        }
                        assignmentRemove = workerRemove.getChuteAssignment();
                        Chute chuteToMove = assignmentRemove.remove(0);

                        assignmentAdd = workerAdd.getChuteAssignment();
                        assignmentAdd.add(chuteToMove);
                    } else {
                        // Move chute upstream
                        for (Worker neighbor: neighbors) {
                            if (neighbor.getWorkerNumber() > workerRemove.getWorkerNumber()) {
                                workerAdd = neighbor;
                                break;
                            }
                        }
                        assignmentRemove = workerRemove.getChuteAssignment();
                        Chute chuteToMove = assignmentRemove.remove(assignmentRemove.size() - 1);

                        assignmentAdd = workerAdd.getChuteAssignment();
                        assignmentAdd.add(0, chuteToMove);

                    }
                    workerRemove.setChuteAssignment(assignmentRemove);
                    workerAdd.setChuteAssignment(assignmentAdd);
                    if (workerAdd.getWorkerNumber() == leastBusyWorker.getWorkerNumber()) {
                        destinationReached = true;
                    }
                    else {
                        workerRemove = workerAdd;
                    }
                }
            }
        }

        return new Solution(chutes, workers);
    }
}