import SimulationPackage.InstancePostNL;

import java.util.ArrayList;

public class Solution {
    private final Chute[] chutes;
    private final Worker[] workers;
    private double maxWorkload;
    private double distanceFront;
    private double sameDestination;
    private double objective;

    public Solution(Chute[] chutes, Worker[] workers) {
        this.chutes = chutes;
        this.workers = workers;
    }

    public Chute[] getChutes() {
        return chutes;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public void setMaxWorkload() {
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

        this.maxWorkload = maxWorkloadLeft + maxWorkloadRight;
    }

    public double getMaxWorkload() {
        this.setMaxWorkload();
        return this.maxWorkload;
    }

    public void setDistanceFront() {
        // Calculate distance from front
        int distanceFront = 0;
        for (Chute chute : chutes) {
            for (DestinationShift destinationShift : chute.getDestShiftAssignment()) {
                distanceFront += destinationShift.getExpectedContainers() * chute.getDistanceFront();
            }
        }

        this.distanceFront = distanceFront;
    }

    public double getDistanceFront() {
        this.setDistanceFront();
        return this.distanceFront;
    }

    public void setSameDestination() {
        int sameDestination = 0;
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
        this.sameDestination = sameDestination;
    }

    public double getSameDestination() {
        this.setSameDestination();
        return sameDestination;
    }

    public void setObjective(InstancePostNL instance) {
        double penaltyDistanceFront = instance.getPenaltyDistanceFront();
        double penaltySameDestination = instance.getPenaltySameDestination();

        this.objective = this.getMaxWorkload() + penaltyDistanceFront * this.getDistanceFront() + penaltySameDestination * this.getSameDestination();
    }

    public double getObjective(InstancePostNL instance) {
        this.setObjective(instance);
        return this.objective;
    }
}