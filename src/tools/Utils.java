package tools;

import experiment.Experiment;

import java.awt.*;
import java.awt.geom.Area;
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
import java.util.List;
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
    public static int randInt(int min, int bound) {
        if (bound <= min) return min;
        else return ThreadLocalRandom.current().nextInt(min, bound);
    }

    /**
     * Returns a random int int between the min (inclusive) max (exclusive)
     * @param minMax Thresholds
     * @return Random int
     * @throws IllegalArgumentException if bound < min
     */
    public static int randInt(MinMax minMax) throws IllegalArgumentException {
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
     * Get a random element from a List
     * @param inList List
     * @return Object element
     */
    public static Object randElement(List inList) {
        return inList.get(randInt(0, inList.size()));
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
     * Get the time in millis
     * @return Long timestamp
     */
    public static long nowMillis() {
//        return Calendar.getInstance().getTimeInMillis();
        return System.currentTimeMillis();
    }

    /**
     * Get the current date+time up to minutes
     * @return LocalDateTime
     */
    public static String nowDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_hh-mm");
        return format.format(Calendar.getInstance().getTime());
    }

    public static String str(Line2D.Double line) {
        return line.x1 + "," + line.y1 + "--" + line.x2 + "," + line.y2;
    }

//    public static String str(Rectangle rect) {
//        return "Rect" +
//                "[x=" + rect.x +
//                ",y=" + rect.y +
//                ",y=" + rect.y +
//                ",y=" + rect.y +
//                "]";
//    }

    public static String str(Area area) {
        return "Area" + area.getBounds().toString();
    }



    public static <T> T last(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public static MinMax xMinMax(List<Point> pList) {
        MinMax result = new MinMax(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (Point p : pList) {
            if (p.x < result.min) result.min = p.x;
            if (p.x > result.max) result.max = p.x;
        }

        return result;
    }

    public static MinMax yMinMax(List<Point> pList) {
        MinMax result = new MinMax(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (Point p : pList) {
            if (p.y < result.min) result.min = p.y;
            if (p.y > result.max) result.max = p.y;
        }

        return result;
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

    public static Point subPoints(Point p1, Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    public static Point intPoint(double x, double y) {
        return new Point((int) x, (int) y);
    }
}
