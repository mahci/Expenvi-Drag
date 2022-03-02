package tools;

import java.util.ArrayList;
import java.util.List;

public class Logs {

    private static final List<String> toLogList = new ArrayList<>();

    static {
//        toLogList.add("Server");
        toLogList.add("ExperimentPanel");
        toLogList.add("DemoPanel");
//        toLogList.add("Controller");
//        toLogList.add("MyScrollBarUI");
        toLogList.add("VTScrollPane");
        toLogList.add("TDScrollPane");
        toLogList.add("Experiment");
//        toLogList.add("Round");
//        toLogList.add("TechConfigPanel");
//        toLogList.add("MainFrame");
    }

    /**
     * Show debug log
     * @param tag TAG
     * @param params Things to show
     */
    public static void d(String tag, Object... params) {
        final int pLen = params.length;
        if (pLen > 0 && toShowTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(int oi = 0; oi < pLen - 1; oi++) {
                sb.append(params[oi]).append(" | ");
            }
            sb.append(params[pLen - 1]);

            System.out.println(tag + " >> " + sb);
        }
    }

    /**
     * Show error log
     * @param tag TAG
     * @param params Things to show
     */
    public static void e(String tag, Object... params) {
        final int pLen = params.length;
        if (pLen > 0 && toShowTag(tag)) {
            StringBuilder sb = new StringBuilder();
            for(int oi = 0; oi < pLen - 1; oi++) {
                sb.append(params[oi]).append(" | ");
            }
            sb.append(params[pLen - 1]);

            System.out.println(tag + " !! " + sb);
        }
    }

    private static boolean toShowTag(String tag) {
        return toLogList.contains(tag.split("/")[0]);
    }

}
