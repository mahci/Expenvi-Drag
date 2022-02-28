package tools;

import experiment.Experiment;

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

import static tools.Consts.DISP.APPLE_DISP_PPI;
import static tools.Consts.DISP.MM_in_INCH;

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
     * mm to pixel
     * @param mm - millimeters
     * @return equivalant in pixels
     */
    public static int mm2px(double mm) {
        String TAG = NAME + "mm2px";

        return (int) ((mm / MM_in_INCH) * APPLE_DISP_PPI);
    }

    /**
     * mm to pixel
     * @param px - pixels
     * @return equivalant in mm
     */
    public static double px2mm(double px) {
        String TAG = NAME + "px2mm";

        return (px / APPLE_DISP_PPI) * MM_in_INCH;
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
     * Manually wrap the text (add \n) and write to outFileName
     * @param resTxtFileName - name of the text file (in /res)
     * @param outFileName - name of the output file (in /res)
     * @param wrapWidth - Width to wrap (in characters)
     * @return ArrayList<Integer></> - number of chraacters in each line
     */
    public static ArrayList<Integer> wrapFile(String resTxtFileName, String outFileName, int wrapWidth)
            throws IOException {
        String TAG = "wrapFile";

        // Read and get the paragraphs
        String filePath = System.getProperty("user.dir") + "/res/" + resTxtFileName;
        String content = Files.readString(Path.of(filePath));
        String[] paragraphs = content.split("\n");

        // Wrap each paragraph and write to file
        PrintWriter outFilePW = new PrintWriter(new FileWriter(outFileName));
        int nParagraphs = paragraphs.length;
        for (int pi = 0; pi < nParagraphs - 1; pi++) {
            outFilePW.println(wrapParagraph(paragraphs[pi], wrapWidth));
        }
        outFilePW.print(wrapParagraph(paragraphs[nParagraphs - 1], wrapWidth)); // write the last paragraph
        outFilePW.close();

        return lineCharCountList;
    }

    /**
     * Wrap a paragraph
     * @param paragraph - input paragraph
     * @param wrapWidth - width of wrap
     * @return wrapped paraagraph
     */
    public static String wrapParagraph(String paragraph, int wrapWidth) {
        String TAG = "wrapText";

        // Special cases
        if (paragraph == null) return "";
        if (paragraph.length() <= wrapWidth) {
            lineCharCountList.add(paragraph.length()); // add the count of chars to the list
            return paragraph;
        }

        // Normal cases
        StringBuilder outStr = new StringBuilder();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(paragraph.split(" ")));

        StringBuilder line = new StringBuilder();
        while (words.size() > 0) {
            if ((line + words.get(0)).length() + 1 < wrapWidth) { // +1 for the last space
                // add if doesn't exceed the limit
                line.append(words.remove(0)).append(" ");
            } else {
                outStr.append(line).append("\n");
                // add the count of chars to the list
                lineCharCountList.add(line.length());
                // reset the line
                line = new StringBuilder();
            }
        }
        outStr.append(line); // append the last line (bc the words is now empty)
        lineCharCountList.add(line.length()); // add the count of chars to the list

        return outStr.toString();
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

}
