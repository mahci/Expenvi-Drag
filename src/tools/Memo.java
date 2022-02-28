package tools;

import static tools.Consts.STRINGS.*;

public class Memo {
    private static final String NAME = "Memo/";

    private String action;
    private String mode;
    private String value1;
    private String value2;

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 String value1
     * @param v2 String value2
     */
    public Memo(String act, String md, String v1, String v2) {
        action = act;
        mode = md;
        value1 = v1;
        value1 = v2;
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Double value Movement along X
     * @param v2 Double value Movement along Y
     */
    public Memo(String act, String md, double v1, double v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     */
    public Memo(String act, String md, Object v1) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = "-";
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     * @param v2 Int value 2
     */
    public Memo(String act, String md, int v1, int v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     * @param v2 Int value 2
     */
    public Memo(String act, String md, Object v1, Object v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Basic consrtuctor
     */
    public Memo() {
        action = "";
        mode = "";
        value1 = "";
        value2 = "";
    }

    /**
     * Return action
     * @return String Action
     */
    public String getAction() {
        return action;
    }

    /**
     * Return mode
     * @return String Mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Get one of the values in String
     * @param valInd Index of the value
     * @return String
     */
    public String getStrValue(int valInd) {
        if (valInd == 1) return value1;
        if (valInd == 2) return value2;
        else return "";
    }

    /**
     * Get one of the values in Double
     * @param valInd Index of the value
     * @return Double
     */
    public Double getDblValue(int valInd) {
        if (valInd == 1) return Double.parseDouble(value1);
        if (valInd == 2) return Double.parseDouble(value2);
        else return 0.0;
    }

    /**
     * Get one of the values in Int
     * @param valInd Index of the value
     * @return Int
     */
    public int getIntValue(int valInd) {
        if (valInd == 1) return Integer.parseInt(value1);
        if (valInd == 2) return Integer.parseInt(value2);
        else return 0;
    }

    /**
     * Get the Memo from String
     * @param mssg String
     * @return Memo
     */
    public static Memo valueOf(String mssg) {
        String TAG = NAME + "valueOf";

        Memo result = new Memo();
        if (mssg != null) {
            String[] parts = mssg.split(SP);

            if (parts.length == 4) {
                result.action = parts[0];
                result.mode = parts[1];
                result.value1 = parts[2];
                result.value2 = parts[3];
            } else {
                Logs.e(TAG, "Memo string not 4 parts!");
            }
        }

        return result;
    }

    /**
     * Get the String equivaluent
     * @return String
     */
    @Override
    public String toString() {
        return action + SP + mode + SP + value1 + SP + value2;
    }
}