package SimulationPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class InstancePostNL {

	// Declare variables about the input into the system
	private final int[][] chutes;
	private final boolean[][] blocked;
	private final int[][] destShift;
	private int totParcels;
	private final int maxChutesPerWorker;
	private double penaltyDistanceFront;
	private double penaltySameDestination;

	private final int capacityChute;
	private final int employees;
	private final int walkingSpeed;
	private final double parcelsPerSecond;
	private final double workRate;

	private int[][] parcelSequence;

	/**
	 * Constructor of a PostNL instance
	 * @param chutes: Information about the chutes. The first column is 1 if the chute is on the left side. The second column states
	 * the distance from the front. The third column states the maximum number of rollcontainers at the chute.
	 * @param blocked: Matrix showing which destination combinations are blocked. If entry (i,j) is true, then destinations i and j
	 * cannot be on the same chute
	 * @param destShift: Information about the destination/shift combinations. The first column contains the destination. The second
	 * column contains the shift number. The third column is 1 if the destination/shift needs to be on the left side. The fourth
	 * column contains the expected number of rollcontainers for the destination/shift combination.
	 * @param totParcels The total number of rollcontainers expected to arrive.
	 */
	public InstancePostNL(int[][] chutes, boolean[][] blocked, int[][] destShift, int totParcels) {
		this.chutes = chutes;
		this.blocked = blocked;
		this.destShift = destShift;
		this.totParcels = totParcels;
		this.penaltyDistanceFront = 0;
		this.penaltySameDestination = 0;

		this.capacityChute = 25;
		this.employees = 10;
		this.maxChutesPerWorker = 5;
		this.walkingSpeed = 1;
		this.parcelsPerSecond = 2;
		this.workRate = 0.25;
	}

	// Get the information about the chutes
	public int[][] getChutes() {
		return chutes;
	}

	// Return the randomized arrival sequence of parcels
	public int[][] getParcelSequence() {
		return parcelSequence;
	}

	// Return how many parcels arrive persecond
	public double getParcelsPerSecond() {
		return parcelsPerSecond;
	}

	// Get the total number of parcels to be handled
	public int getTotParcels() {
		return totParcels;
	}
	// Get the max number of chutes per worker
	public int getMaxChutesPerWorker() {
		return maxChutesPerWorker;
	}

	// Get the capacity of a chute (how many parcels can wait in the line)
	public int getCapacityChute() {
		return capacityChute;
	}

	// Get the number of employees
	public int getEmployees() {
		return employees;
	}

	// Get the walking speed of employees (in m/s)
	public int getWalkingSpeed() {
		return walkingSpeed;
	}

	// Get the working rate of employees (in parcels/s)
	public double getWorkRate() {
		return workRate;
	}

	// Get the matrix of blocked destination combinations
	public boolean[][] getBlocked() {
		return blocked;
	}

	// Get the information about the destination/shift combinations
	public int[][] getDestShift() {
		return destShift;
	}

	// Get the number of chutes
	public int getNumberOfChutes() {
		return chutes.length;
	}

	// Get the number of destinations
	public int getNumberOfDest() {
		return blocked.length;
	}
	public double getPenaltyDistanceFront() {
		return this.penaltyDistanceFront;
	}

	public double getPenaltySameDestination() {
		return this.penaltySameDestination;
	}
	public void setPenaltyDistanceFront(double penalty) {
		this.penaltyDistanceFront = penalty;
	}

	public void setPenaltySameDestination(double penalty) {
		this.penaltySameDestination = penalty;
	}

	/**
	 * This method initializes a sequence of parcel arrivals
	 */
	public void createInitialSequence(boolean randomize, Random rnd, double varDs) {
		if (randomize) {
			double variation = Methods.getNormal(0.10, 1, rnd);

			int[][] avPar = new int[27][10];
			int avParTot = 0;
			for (int i = 0; i < this.destShift.length; i++) {
				double multiplier = Methods.getNormal(varDs, variation, rnd);
				int parcels = (int) Math.round(this.destShift[i][3] * 50 * multiplier);
				avPar[this.destShift[i][0]][this.destShift[i][1]] = parcels;
				avParTot += parcels;
			}
			this.parcelSequence = new int[avParTot][2];
			//System.out.println("CHECK:");
			//System.out.println("Estimated Parcels: " + 45100 * variation);
			//System.out.println("Parcels in reality: " + avParTot);
			int counter2 = 0;
			for (int d = 0; d < 27; d++)
				for (int s = 0; s < 10; s++) {
					int p = 0;
					while (p < avPar[d][s]) {
						this.parcelSequence[counter2][0] = d;
						this.parcelSequence[counter2][1] = s;
						counter2++;
						p++;
					}
				}
			this.totParcels = avParTot;

			//System.out.println(Arrays.deepToString(avPar).replace("], ", "]\n"));
		} else {
			this.parcelSequence = new int[totParcels][2];
			int counter = 0;
			for (int i = 0; i < this.destShift.length; i++) {
				int j = 0;
				while (j < this.destShift[i][3]) {
					for (int k = 0; k < 50; k++) {
						this.parcelSequence[counter][0] = this.destShift[i][0];
						this.parcelSequence[counter][1] = this.destShift[i][1];
						counter++;
					}
					j++;
				}
			}
		}

	}

	/**
	 * This method randomizes the sequence of the parcel arrivals
	 */
	public void randomizeSequence(Random rnd) {

		shuffleArray(this.parcelSequence, rnd);
		//System.out.println(Arrays.deepToString(this.parcelSequence).replace("], ", "]\n"));

	}


	/**
	 * Method to shake an array in a random order
	 * @param arr The array to place in a random order
	 */
	public void shuffleArray(int[][] arr, Random rnd) {
		for (int i = arr.length - 1; i > 0; i--) {

			int index = rnd.nextInt(i+1);

			int a0 = arr[index][0];
			int a1 = arr[index][1];

			arr[index][0] = arr[i][0];
			arr[index][1] = arr[i][1];

			arr[i][0] = a0;
			arr[i][1] = a1;
		}
	}


	/**
	 * Method to get all the d/s corresponding to one of the two sides
	 * @param left: 1 if you want to get the left side, 0 for the right side
	 * @return Array containing all the information about the d/s from the corresponding side
	 */
	public int[][] getSide(int left) {

		int lefts = 0;

		for (int i = 0; i < this.destShift.length; i++) {
			if (this.destShift[i][2] == left)
				lefts++;
		}

		int[][] destShiftLeft = new int[lefts][3];
		int counter = 0;

		for (int i = 0; i < this.destShift.length; i++) {
			if (this.destShift[i][2] == left) {
				destShiftLeft[counter][0] = this.destShift[i][0];
				destShiftLeft[counter][1] = this.destShift[i][1];
				destShiftLeft[counter][2] = this.destShift[i][3];
				counter++;
			}
		}

		return destShiftLeft;
	}


	/**
	 * Read a text file containing information about the PostNL case
	 * @param instanceFileName The filename of the text file
	 * @return A new PostNL instance
	 * @throws FileNotFoundException if file not found
	 */
	public static InstancePostNL read(File instanceFileName) throws FileNotFoundException {

		// Try to open the file and initialize the use of a so called scanner.
		try (Scanner s = new Scanner(instanceFileName)) {

			/*
			 * First read the information about the chutes
			 */

			// Obtain the number of chutes
			s.next();
			int numberOfChutes = s.nextInt();
			s.next();

			s.next();
			s.next();
			s.next();
			s.next();

			// Obtain information about the chutes
			int[][] chutes = new int[numberOfChutes][3];
			for (int i = 0; i < numberOfChutes; i++) {
				s.next();
				chutes[i][0] = s.nextInt();
				chutes[i][1] = s.nextInt();
				chutes[i][2] = s.nextInt();
			}

			//System.out.println(numberOfChutes);
			//System.out.println(Arrays.deepToString(chutes).replace("], ", "]\n"));

			/*
			 * Next read the information about the blocked combinations
			 */

			// Obtain number of destinations
			s.next();
			int numberOfDest = s.nextInt();
			s.next();
			for (int i = 0; i < numberOfDest + 1; i++)
				s.next();

			// Obtain information about the blocked destination combinations
			boolean[][] blocked = new boolean[numberOfDest][numberOfDest];
			for (int i = 0; i < numberOfDest; i++) {
				s.next();
				for (int j = 0; j < numberOfDest; j++) {
					int temp = s.nextInt();
					if (temp == 1)
						blocked[i][j] = true;
				}
			}

			//System.out.println(numberOfDest);
			//System.out.println(Arrays.deepToString(blocked).replace("], ", "]\n"));

			/*
			 * Read the information about the destination/shifts
			 */

			// Obtain the number of destinations/shift combinations
			s.next();
			int numberOfDestShift = s.nextInt();
			s.next();

			s.next();
			s.next();
			s.next();

			// Obtain information about the destination/shift combinations
			int[][] destShift = new int[numberOfDestShift][4];

			String previous = "";
			String current = "";
			int j = -1;
			for (int i = 0; i < numberOfDestShift; i++) {
				current = s.next();
				if (!current.equals(previous))
					j++;
				previous = current;

				destShift[i][0] = j;
				destShift[i][1] = s.nextInt() - 1;
				destShift[i][2] = s.nextInt();
			}

			s.next();
			s.next();
			s.next();
			s.next();
			s.next();
			s.next();

			int totParcels = 0;
			previous = "";
			current = "";
			j = -1;
			for (int i = 0; i < numberOfDestShift; i++) {
				current = s.next();
				if (!current.equals(previous))
					j++;
				previous = current;

				s.next();
				destShift[i][3] = s.nextInt();
				totParcels += destShift[i][3];
			}
			totParcels *= 50;

			//System.out.println(Arrays.deepToString(destShift).replace("], ", "]\n"));
			//System.out.println(totParcels);

			// Create instance
			InstancePostNL readInstance = new InstancePostNL(chutes, blocked, destShift, totParcels);
			return readInstance;
		}

	}

}
