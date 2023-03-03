package SimulationPackage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Simulation {

	// Time variable and creation of the event list with events to do
	private double time;
	PriorityQueue<Action> events = new PriorityQueue<Action>();

	// Decision variables given into the system
	private int[][] chutesEmpl; // Matrix that stores which employee is allocated to which chutes
	private int[] currentChuteEmpl; // Vector that says what the current chute is of each employee
	private int[][] destShiftToChute; // Matrix that tells what chute is allocated to which dest/shifts
	private int[][] destShiftToRoll; // Matrix that tells what roll container is allocated to which dest/shift
	private int[] emplOfChute; // Vector that states, for every chute, which employee is working on it

	// Variables for the current state of the system
	private int[] queueSizes; // Array containing the queue at each chute
	private boolean[] chuteStopped; // Array that says whether the work at a chute is stopped due to a parcel with capacity at the roll container
	List<Queue<DestinationShift>> queues;
	private int currentTotQueue;
	private int parcelNumber;

	private int[] containersAvailable; // Array to store the number of available (unused) roll containers per chute
	private int[][] storageLeft; // Array to store the storage left per roll container (rows stand for chutes and columns for which roll container)
	private boolean[][] positionUsed;

	// System parameters
	private final int chuteCapacity;
	private final int numberChutes;
	private final int numberEmployees;
	private final int[][] chutes; // Array containing information about the chutes
	private final int[][] destShift;
	private final int capContainer;

	private final double arrivalTime;
	private final double handlingTime;
	private final double emptyTime;
	private final double walkingSpeed;
	private final double beltSpeed;

	// Performance metrics
	private double totalQueue;
	private int distanceWalked;
	private int parcelsRemoved;


	// Parcel information
	private int[][] parcelSequence;

	public Simulation(InstancePostNL postnlInstance) {
		this.parcelSequence = postnlInstance.getParcelSequence();

		this.currentChuteEmpl = new int[postnlInstance.getEmployees()];
		this.queueSizes = new int[postnlInstance.getNumberOfChutes()];

		this.chuteCapacity = postnlInstance.getCapacityChute();
		this.numberChutes = postnlInstance.getNumberOfChutes();
		this.numberEmployees = postnlInstance.getEmployees();
		this.chutes = postnlInstance.getChutes();
		this.destShift = postnlInstance.getDestShift();
		this.capContainer = 50;

		this.arrivalTime =  0.42; // seconds
		this.handlingTime = 3; // seconds
		this.emptyTime = 15; // seconds
		this.walkingSpeed = 1.2; // meter/seconds
		this.beltSpeed = 2.0; // meter/seconds

		this.emplOfChute = new int[this.numberChutes];

		this.chuteStopped = new boolean[this.numberChutes];
		queues = new ArrayList<Queue<DestinationShift>>(32);
		for (int i = 0; i < numberChutes; ++i)
			queues.add(new LinkedList<DestinationShift>());
	}

	/**
	 * Get the allocation of the employees to the chutes
	 * @return
	 */
	public int[][] getEmployees() {
		return chutesEmpl;
	}

	/**
	 * Add which employee is allocated to what chutes. The rows should stand for the employees and the
	 * columns to the chutes. Entry (i,j) is 1 if chute j is allocated to employee i.
	 * @param employees: Allocations of employees to chutes
	 */
	public void setEmployees(int[][] employees) {
		this.chutesEmpl = employees;
		for (int i = 0; i < this.numberChutes; i++)
			for (int j = 0; j < this.numberEmployees; j++)
				if (this.chutesEmpl[j][i] == 1) {
					this.emplOfChute[i] = j;
					break;
				}

		for (int i = 0; i < this.numberEmployees; i++)
			for (int j = 0; j < this.numberChutes; j++)
				if (this.chutesEmpl[i][j] == 1) {
					this.currentChuteEmpl[i] = j;
					break;
				}

	}

	/**
	 * Get the allocations of the destination/shifts to chutes
	 * @return
	 */
	public int[][] getDestShiftToChute() {
		return destShiftToChute;
	}

	/**
	 * Add what destination shift combinations are allocated to what chutes. The row should stand for the
	 * destination and the column for the shift. The entry then stand for the chute (ranging from 0 to 39)
	 * @param destShiftToChute
	 */
	public void setDestShiftToChute(int[][] destShiftToChute) {
		this.destShiftToChute = destShiftToChute;
		this.allocateRollContainers();
	}

	/**
	 * This method allocates the roll containers
	 */
	public void allocateRollContainers() {
		int height = destShiftToChute.length;
		int width = destShiftToChute[0].length;
		this.destShiftToRoll = new int[height][width];
		int[][] expectedRollContainers = new int[height][width];
		int[] rollContainersUsed = new int[this.numberChutes];

		// Retrieve the expected number of roll containers per destination/shift
		for (int i = 0; i < this.destShift.length; i++) {
			int dest = this.destShift[i][0];
			int shift = this.destShift[i][1];
			expectedRollContainers[dest][shift] = this.destShift[i][3];
		}

		// Allocate per chute which dest/shift get which roll container and count the number of
		// roll containers used per chute
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (expectedRollContainers[i][j] > 0) {
					int chute = destShiftToChute[i][j];
					destShiftToRoll[i][j] = rollContainersUsed[chute];
					rollContainersUsed[chute]++;
				}


		// Initialize the number of free roll containers per chute
		this.containersAvailable = new int[this.numberChutes];
		for (int i = 0; i < this.numberChutes; i++)
			containersAvailable[i] = this.chutes[i][2] - rollContainersUsed[i];

		// Initialize the available capacity in all the roll containers (set it at 50)
		this.storageLeft = new int[this.numberChutes][7];
		this.positionUsed = new boolean[this.numberChutes][7];
		for (int i = 0; i < this.numberChutes; i++)
			for (int j = 0; j < rollContainersUsed[i]; j++) {
				this.storageLeft[i][j] = capContainer;
				this.positionUsed[i][j] = true;
			}


		//System.out.println(Arrays.deepToString(this.storageLeft).replace("], ", "]\n"));
		//System.out.println(Arrays.deepToString(this.destShiftToRoll).replace("], ", "]\n"));
	}


	public void initialise() {

		// Initialise the time
		this.time = 0;
		this.parcelNumber = 0;

		int dest = this.parcelSequence[this.parcelNumber][0];
		int shift = this.parcelSequence[this.parcelNumber][1];
		int chute = this.destShiftToChute[dest][shift];

		// Create first parcel arrival
		this.events.add(new Action(this.time, "toConveyor", chute, dest, shift));
	}

	public void run() {
		//for (int i = 0; i < 10; i++) {
		while (!this.events.isEmpty()) {
			//System.out.println("Time: " + this.time);
			//System.out.println(Arrays.toString(queueSizes));
			//System.out.println(Arrays.toString(chuteStopped));
			String toDo = this.events.peek().getToDo();

			if (toDo == "toConveyor")
				this.toConveyor();
			else if (toDo == "newParcel")
				this.newParcel();
			else if (toDo == "handled")
				this.handled();
			else if (toDo == "walked")
				this.walked();
			else if (toDo == "emptied")
				this.emptied();
		}

		//System.out.println("End-time: " + this.time/3600);
		//System.out.println(Arrays.deepToString(this.storageLeft).replace("], ", "]\n"));
	}

	public void toConveyor() {

		Action toDo = this.events.poll();

		// Update the performance metrics
		double timeElapsed = toDo.getTime() - this.time;
		this.totalQueue = totalQueue + timeElapsed * this.currentTotQueue;

		// Get information about the action to do
		this.time = toDo.getTime();
		int newDest = toDo.getDest();
		int newShift = toDo.getShift();

		double timeAtChute = 0;
		if (toDo.getChute() < 20)
			timeAtChute = this.time + (30 + 4 * toDo.getChute()) / this.beltSpeed;
		else
			timeAtChute = this.time + (170 + 4 * (39 - toDo.getChute())) / this.beltSpeed;

		this.events.add(new Action(timeAtChute, "newParcel", toDo.getChute(), newDest, newShift));

		// Create a new parcel arrival and add it to the event list
		if (this.parcelNumber + 1 < this.parcelSequence.length) {
			this.parcelNumber++;
			int dest = this.parcelSequence[this.parcelNumber][0];
			int shift = this.parcelSequence[this.parcelNumber][1];
			int chute = this.destShiftToChute[dest][shift];
			this.events.add(new Action(this.time+arrivalTime, "toConveyor", chute, dest, shift));
		}

	}

	/**
	 * This method handles the incoming of a new parcel
	 */
	public void newParcel() {

		Action toDo = this.events.poll();

		// Update the performance metrics
		double timeElapsed = toDo.getTime() - this.time;
		this.totalQueue = totalQueue + timeElapsed * this.currentTotQueue;

		// Get information about the action to do
		this.time = toDo.getTime();
		int newDest = toDo.getDest();
		int newShift = toDo.getShift();

		// Save the chute that the parcel needs to go to and the employee that is allocated to that chute
		int toChute = toDo.getChute();
		int toEmployee = this.emplOfChute[toChute];


		// If the queue get bigger than the capacity, the parcel gets removed
//		if (queueSizes[toChute] == chuteCapacity) {
//			parcelsRemoved++;
//			if (queueSizes[toChute] - queueSizes[currentChuteEmpl[toEmployee]] > 2) { // PARAMETER TUNING HERE
//				this.events.add(new Action(this.time + 8 * Math.abs(toChute - currentChuteEmpl[toEmployee]), "walked", toChute, -1, -1));
//				distanceWalked += (4 * Math.abs(toChute - currentChuteEmpl[toEmployee]));
//				this.currentChuteEmpl[toEmployee] = toChute;
//			}
//			return;
//		}


		// If the queue get bigger than the capacity, the parcel gets removed
		if (queueSizes[toChute] == chuteCapacity) {
			parcelsRemoved++;
			this.events.add(new Action(this.time + 280/this.beltSpeed, "newParcel", toChute, newDest, newShift));
			if (queueSizes[toChute] - queueSizes[currentChuteEmpl[toEmployee]] > 10) { // PARAMETER TUNING HERE
				this.events.add(new Action(this.time + (4 * Math.abs(toChute - currentChuteEmpl[toEmployee]))/this.walkingSpeed, "walked", toChute, -1, -1));
				distanceWalked += (4 * Math.abs(toChute - currentChuteEmpl[toEmployee]));
				this.currentChuteEmpl[toEmployee] = toChute;
			}
			return;
		}


		// Add the parcel to the queue of the corresponding chute
		this.queues.get(toChute).add(new DestinationShift(newDest, newShift));
		this.currentTotQueue++;

		// If there is no queue at the chute and there is an employee working on the chute and the chute is not stopped, handle the parcel
		if (queueSizes[toChute] == 0 && currentChuteEmpl[toEmployee] == toChute && !this.chuteStopped[toChute]) {
			this.queueSizes[toChute]++;
			this.events.add(new Action(this.time + this.handlingTime, "handled", toChute, newDest, newShift));
			return;
		}

		// If there is a queue at the chute and the queue is ten bigger than the chute the employee is
		// currently working on. Move the employee to the new chute // PARAMETER TUNING HERE
		if ((queueSizes[toChute] - queueSizes[currentChuteEmpl[toEmployee]] > 18 || queueSizes[currentChuteEmpl[toEmployee]] == 0) && !this.chuteStopped[toChute]) {
			this.queueSizes[toChute]++;
			this.events.add(new Action(this.time + (4 * Math.abs(toChute - currentChuteEmpl[toEmployee]))/this.walkingSpeed, "walked", toChute, -1, -1));
			distanceWalked += (4 * Math.abs(toChute - currentChuteEmpl[toEmployee]));
			this.currentChuteEmpl[toEmployee] = toChute;
			return;
		}

		// If there is a queue and no need to move the employee to the chute. Add 1 to the queue of the chute
		this.queueSizes[toChute]++;

	}

	/**
	 * This method handles the handling of a package at a chute
	 */
	public void handled() {
		// Get information about the action to do
		Action toDo = this.events.poll();

		// Update the performance metrics
		double timeElapsed = toDo.getTime() - this.time;
		this.totalQueue = totalQueue + timeElapsed * this.currentTotQueue;

		// Get information about the action to do (2)
		this.time = toDo.getTime();
		int toChute = toDo.getChute();
		int toEmployee = this.emplOfChute[toChute];

		// Handle the parcel
		if (currentChuteEmpl[toEmployee] == toChute && !this.chuteStopped[toChute] && this.queueSizes[toChute] > 0) {

			// Update the queue and roll containers
			this.queueSizes[toChute]--;
			this.currentTotQueue--;
			int toContainer = this.destShiftToRoll[toDo.getDest()][toDo.getShift()];
			this.storageLeft[toChute][toContainer]--;
			this.queues.get(toChute).remove();

			//Check if the space on the just used roll container is 0
			if (this.storageLeft[toChute][toContainer] == 0) {

				// Calculate the distance to walk
				double toWalk = 0;
				if (toChute >= 21)
					toWalk = 8 * (toChute - 20);
				else
					toWalk = 8 * toChute;

				// Create the new event to empty the roll container
				this.events.add(new Action(this.time + this.emptyTime, "emptied", toChute, toDo.getDest(), toDo.getShift()));
				//distanceWalked += toWalk; // COMMENTED OUT!!!

				// If there are roll containers available, use one roll container for the to empty d/s roll container
				if (this.containersAvailable[toChute] > 0) {
					this.containersAvailable[toChute]--;
					this.storageLeft[toChute][toContainer] = capContainer;
				} else {
					this.chuteStopped[toChute] = true;
				}
			}

			//Schedule the handling of the next parcel (if any)
			if (this.chuteStopped[toChute])
				return;

			if (this.queueSizes[toChute] == 0 || this.chuteStopped[toChute]) {

				int maxQueue = 0;
				int newChute = 0;

				// Look for the chute of the employee with the highest queue
				for (int i = 0; i < this.numberChutes; i++)
					if (this.chutesEmpl[toEmployee][i] == 1 && this.queueSizes[i] > maxQueue && i != toChute && !this.chuteStopped[i]) {
						maxQueue = this.queueSizes[i];
						newChute = i;
					}
				// Move the employee to the chute with the highest queue (if any)
				if (maxQueue > 0) {
					this.currentChuteEmpl[toEmployee] = newChute;
					this.events.add(new Action(this.time + (4 * Math.abs(newChute - toChute))/this.walkingSpeed, "walked", newChute, -1, -1));
					distanceWalked += (4 * Math.abs(toChute - newChute));
				}
				return;

			}

			int newDest = this.queues.get(toChute).peek().getDestination();
			int newShift = this.queues.get(toChute).peek().getShift();
			this.events.add(new Action(this.time + this.handlingTime, "handled", toChute, newDest, newShift));

		}


	}

	public void emptied() {

		// Update the performance metrics
		Action toDo = this.events.poll();
		double timeElapsed = toDo.getTime() - this.time;
		this.totalQueue = totalQueue + timeElapsed * this.currentTotQueue;

		this.time = toDo.getTime();

		// Get the information about the action to do
		int chute = toDo.getChute();
		int dest = toDo.getDest();
		int shift = toDo.getShift();
		int container = destShiftToRoll[dest][shift];
		int employee = this.emplOfChute[chute];

		// To add: if there is any other full RC on the chute, the arrived RC is for that d/s
		if (this.storageLeft[chute][container] == 0)
			this.storageLeft[chute][container] = capContainer;
		else
			this.containersAvailable[chute]++;

		// Update the status of the chute (whether it is stopped)
		this.chuteStopped[chute] = false;
		for (int r = 0; r < 7; r++) {
			if (this.positionUsed[chute][r] && this.storageLeft[chute][r] == 0) {
				//System.out.println("WHY?");
				//System.out.println(Arrays.deepToString(positionUsed).replace("], ", "]\n"));
				//System.out.println(Arrays.deepToString(storageLeft).replace("], ", "]\n"));
				this.chuteStopped[chute] = true;
			}
		}

		// If an employee is on the chute, working can be continued, and there is a queue
		// at the chute. Then schedule the next handling
		if (!chuteStopped[chute] && this.currentChuteEmpl[employee] == chute && this.queueSizes[chute] > 0) {
			int d = this.queues.get(chute).peek().getDestination();
			int s = this.queues.get(chute).peek().getShift();
			this.events.add(new Action(this.time + this.handlingTime, "handled", chute, d, s));
		}


		// TO ADD: MOVE TO THE CHUTE IF IT HAS A HIGHER QUEUE
		if ((queueSizes[chute] - queueSizes[currentChuteEmpl[employee]] > 0 || queueSizes[currentChuteEmpl[employee]] == 0) && !this.chuteStopped[chute] && this.queueSizes[chute] > 0) {
			this.events.add(new Action(this.time + (4 * Math.abs(chute - currentChuteEmpl[employee]))/this.walkingSpeed, "walked", chute, -1, -1));
			distanceWalked += (4 * Math.abs(chute - currentChuteEmpl[employee]));
			this.currentChuteEmpl[employee] = chute;
		}

	}

	/**
	 * This methods handles the walking of an employee
	 */
	public void walked() {

		// Update the performance metrics
		Action toDo = this.events.poll();
		double timeElapsed = toDo.getTime() - this.time;
		this.totalQueue = totalQueue + timeElapsed * this.currentTotQueue;

		this.time = toDo.getTime();

		// Save which employee walked to which chute
		int toChute = toDo.getChute();

		// Create a new event for the handling of a package from the new chute
		if (!this.chuteStopped[toChute] && this.queueSizes[toChute] > 0) {
			int newDest = this.queues.get(toChute).peek().getDestination();
			int newShift = this.queues.get(toChute).peek().getShift();
			this.events.add(new Action(this.time + this.handlingTime, "handled", toChute, newDest, newShift));
		}


	}


	public double getAverageQueue() {
		return Math.round((1.00 * this.totalQueue) / ((this.parcelSequence.length * this.arrivalTime) * this.numberChutes)*1000.0)/1000.0;
	}

	public int getDistanceWalked() {
		return this.distanceWalked/10;
	}

	public int getParcelsRemoved() {
		return this.parcelsRemoved;
	}

	public double getTime() {
		return this.time / 3600;
	}


}
