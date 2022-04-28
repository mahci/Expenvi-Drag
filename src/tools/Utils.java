package tools;

import experiment.Experiment;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static tools.Consts.DISP.*;

public class Utils {

    private final static String NAME = "Utils/";
    /*-------------------------------------------------------------------------------------*/

    private static final ArrayList<Integer> lineCharCountList = new ArrayList<>();
    /*-------------------------------------------------------------------------------------*/

    /**
     * Returns a random int between the min (inclusive) and the bound (exclusive)
     * @param min Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     * @throws IllegalArgumentException if bound < min
     */
    public static int randInt(int min, int bound) throws IllegalArgumentException {
        return ThreadLocalRandom.current().nextInt(min, bound);
    }

    /**
     * Returns a random int int between the min (inclusive) max (exclusive)
     * @param minMax Thresholds
     * @return Random int
     * @throws IllegalArgumentException if bound < min
     */
    public static int randIntBetween(MinMax minMax) throws IllegalArgumentException {
        return ThreadLocalRandom.current().nextInt(minMax.min, minMax.max);
    }

    /**
     * Get a random element from any int array
     * @param inArray input int[] array
     * @return int element
     */
    public static int randElement(int[] inArray) {
        return inArray[randInt(0, inArray.length)];
    }

    /**
     * NOT on 0/1 (o => 1, 1 => 0)
     * @return Int
     */
    public static int intNOT(int i) {
        return (i == 1) ? 0 : 1;
    }

    /**
     * mm to pixel
     * @param mm - millimeters
     * @return equivalant in pixels
     */
    public static int mm2px(double mm) {
        String TAG = NAME + "mm2px";

        return (int) ((mm / MM_in_INCH) * DPI);
    }

    /**
     * mm to pixel
     * @param px - pixels
     * @return equivalant in mm
     */
    public static double px2mm(double px) {
        String TAG = NAME + "px2mm";

        return (px / DPI) * MM_in_INCH;
    }

    /**
     * Generate a random permutation of {0, 1, ..., len - 1}
     * @param len - length of the permutation
     * @return Random permutation
     */
    public static List<Integer> randPerm(int len) {
        String TAG = NAME + "randPerm";

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        return indexes;
    }

    /**
     * True -> 1, False -> 0
     * @param b Boolean
     * @return Int
     */
    public static int bool2Int(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Get the current time up to the seconds
     * @return LocalTime
     */
    public static LocalTime nowTimeSec() {
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Get the current time up to the milliseconds
     * @return LocalTime
     */
    public static LocalTime nowTimeMilli() {
        return LocalTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * Get the time in millis
     * @return Long timestamp
     */
    public static long nowInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Get the current date+time up to minutes
     * @return LocalDateTime
     */
    public static String nowDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_hh-mm");
        return format.format(Calendar.getInstance().getTime());
    }

    /**
     * Print Path2D.Double coords
     * @param path Path2D.Double
     */
    private void printPath(Path2D.Double path) {
        final String TAG = NAME + "printPath";
        Out.d(TAG, "Printing Path...");
        double[] coords = new double[4];
        PathIterator pi = path.getPathIterator(null);
        Out.d(TAG, pi.isDone());
        while(!pi.isDone()) {
            pi.currentSegment(coords);
            Out.d(TAG, Arrays.toString(coords));
            pi.next();
        }
    }

    public static boolean intersects(Path2D.Double path, Line2D line) {
        double x1 = -1 ,y1 = -1 , x2= -1, y2 = -1;
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next())
        {
            double[] coordinates = new double[6];
            switch (pi.currentSegment(coordinates))
            {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                {
                    if(x1 == -1 && y1 == -1 )
                    {
                        x1 = coordinates[0];
                        y1 = coordinates[1];
                        break;
                    }
                    if(x2 == -1 && y2 == -1)
                    {
                        x2 = coordinates[0];
                        y2 = coordinates[1];
                        break;
                    }
                    break;
                }
            }
            if(x1 != -1 && y1 != -1 && x2 != -1 && y2 != -1)
            {
                Line2D segment = new Line2D.Double(x1, y1, x2, y2);
                if (segment.intersectsLine(line))
                {
                    return true;
                }
                x1 = -1;
                y1 = -1;
                x2 = -1;
                y2 = -1;
            }
        }

        return false;
    }
}
