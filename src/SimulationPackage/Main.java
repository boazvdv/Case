package SimulationPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		File instance = new File("Data.txt");
		InstancePostNL postnl = InstancePostNL.read(instance);

		// The simulation can be run like this:

		// postnl.createInitialSequence();
		// runSimulation(POST NL INSTANCE, WORKER ALLOCATION, DEST/SHIFT ALLOCATIONS, #RUNS (for example 1000));
	}

	public static void runSimulation(InstancePostNL instance, int[][] empToChute, int[][] dsToChute, int runs) {

		System.out.println();
		Random rand = new Random(50);

		long tic = System.currentTimeMillis();

		double[] avQueues = new double[runs];
		int[] distancesWalked = new int[runs];
		int[] parcelsRemoved = new int[runs];

		for (int i = 0; i < runs; i++) {
			// Randomize the parcel sequence
			instance.randomizeSequence(rand);

			// Initialize the simulation
			Simulation sim = new Simulation(instance);
			sim.setEmployees(empToChute);
			sim.setDestShiftToChute(dsToChute);

			// Run the simulation
			sim.initialise();
			sim.run();

			avQueues[i] = sim.getAverageQueue();
			distancesWalked[i] = sim.getDistanceWalked();
			parcelsRemoved[i] = sim.getParcelsRemoved();
		}

		// Print out the simulation results
//		System.out.println(Arrays.toString(avQueues));
//		System.out.println(Arrays.toString(distancesWalked));
//		System.out.println(Arrays.toString(parcelsRemoved));
//		System.out.println();


		// Get the averages of the simulation results
		double avQueue = 0;
		double avDistWalked = 0;
		double avParcelsRem = 0;
		for (int i = 0; i < runs; i++) {
			avQueue += avQueues[i]/runs;
			avDistWalked += 1.0 * distancesWalked[i]/runs;
			avParcelsRem += 1.0 * parcelsRemoved[i]/runs;
		}

		// Get the variances of the simulation results
		double sdQueue = 0;
		double sdDistWalked = 0;
		double sdParcelsRem = 0;
		for (int i = 0; i < runs; i++) {
			sdQueue += Math.pow((avQueues[i] - avQueue),2)/runs;
			sdDistWalked += Math.pow((distancesWalked[i] - avDistWalked),2)/runs;
			sdParcelsRem += Math.pow((parcelsRemoved[i] - avParcelsRem),2)/runs;
		}
		sdQueue = Math.round(1000.0 * Math.pow(sdQueue, .5))/1000.0;
		sdDistWalked = Math.round(Math.pow(sdDistWalked, .5));
		sdParcelsRem = Math.round(10.0 * Math.pow(sdParcelsRem, .5))/10.0;

		avQueue = Math.round(100.0 * avQueue)/100.0;
		avDistWalked = Math.round(avDistWalked);
		avParcelsRem = Math.round(avParcelsRem);

		// Print the results
		System.out.println("Queue");
		System.out.println("Average: " + avQueue);
		System.out.println("Std: " + sdQueue);
		System.out.println();

		System.out.println("Distance walked");
		System.out.println("Average: " + avDistWalked);
		System.out.println("Std: " + sdDistWalked);
		System.out.println();

		System.out.println("Parcels removed");
		System.out.println("Average: " + avParcelsRem);
		System.out.println("Std: " + sdParcelsRem);
		System.out.println();

		System.out.println("Running time: " + (System.currentTimeMillis() - tic) /1000.0 + " seconds");

	}

}
