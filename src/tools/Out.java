package tools;

import java.util.ArrayList;
import java.util.List;

public class Out {

    private static final List<String> toLogList = new ArrayList<>();

    static {
        toLogList.add("BoxTaskPanel");
        toLogList.add("BarTaskPanel");
//        toLogList.add("TaskPanel");
        toLogList.add("TunnelTaskPanel");
        toLogList.add("Enter");
        toLogList.add("Circle");
        toLogList.add("Server");
        toLogList.add("Block");
//        toLogList.add("BoxTrial");
        toLogList.add("TunnelTrial");
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
            for (int oi = 0; oi < pLen - 1; oi++) {
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
            for (int oi = 0; oi < pLen - 1; oi++) {
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
