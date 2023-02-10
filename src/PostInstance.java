import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PostInstance {

    // Declare variables about the input into the system
    private final int[][] chutes;
    private final boolean[][] blocked;
    private final int[][] destShift;
    private final int totParcels;
    private final int numWorkers;
    private final int maxChutesPerWorker;

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
    public PostInstance(int[][] chutes, boolean[][] blocked, int[][] destShift, int totParcels) {
        this.chutes = chutes;
        this.blocked = blocked;
        this.destShift = destShift;
        this.totParcels = totParcels;
        this.numWorkers = 10;
        this.maxChutesPerWorker = 5;
    }

    // Get the information about the chutes
    public int[][] getChutes() {
        return chutes;
    }

    // Get the total number of parcels to be handled
    public int getTotParcels() {
        return totParcels;
    }

    // Get the number of worker
    public int getNumWorkers() {
        return numWorkers;
    }

    // Get the max number of chutes per worker
    public int getMaxChutesPerWorker() {
        return maxChutesPerWorker;
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
    public static PostInstance read(File instanceFileName) throws FileNotFoundException {

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

            // Create instance
            PostInstance readInstance = new PostInstance(chutes, blocked, destShift, totParcels);
            return readInstance;
        }

    }

}