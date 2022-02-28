package experiment;

public class Experiment {
    private final static String NAME = "Experiment/";

    //-- Constants
//    public static final DimensionD VT_PANE_DIM_mm = new DimensionD(130.0, 145.0);

//    public enum DIRECTION {
//        N(0), S(1), E(2), W(3), NE(4), NW(5), SE(6), SW(7);
//        private final int n;
//        DIRECTION(int i) { n = i; }
//        // Get a NE/NW/SE/SW randomly
//        public static DIRECTION randTd() {
//            return DIRECTION.values()[Utils.randInt(4, 8)];
//        }
//        // Get a NE/SE randomly
//        public static DIRECTION randOne(DIRECTION d0, DIRECTION d1) {
//            if (Utils.randInt(0, 2) == 0) return d0;
//            else return d1;
//        }
//        // Get the opposite direction (Horizontal)
//        public static DIRECTION oppHz(DIRECTION dr) {
//            return switch (dr) {
//                case N -> N;
//                case S -> S;
//                case E -> W;
//                case W -> E;
//                case NE -> NW;
//                case NW -> NE;
//                case SE -> SW;
//                case SW -> SE;
//            };
//        }
//        // Get the opposite direction (Vertical)
//        public static DIRECTION oppVt(DIRECTION dr) {
//            return switch (dr) {
//                case N -> S;
//                case S -> N;
//                case E -> E;
//                case W -> W;
//                case NE -> SE;
//                case NW -> SW;
//                case SE -> NE;
//                case SW -> NW;
//            };
//        }
//    }

//    public enum TASK {
//        VERTICAL, TWO_DIM;
//        private static final TASK[] values = values();
//        public static TASK get(int ord) {
//            if (ord < values.length) return values[ord];
//            else return values[0];
//        }
//    }

    //-- Variables
//    private int[] VT_DISTANCES = new int[]{50, 200, 600}; // in lines/cells
//    private Map<TASK, Integer> N_BLOCKS = Map.of(VERTICAL, 8, TWO_DIM, 4);

    //--- Participant's things!
    private int mPId;

    // -------------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param pid Participant's Id (from 1)
     */
    public Experiment(int pid) {
        final String TAG = NAME;

        mPId = pid;
    }


    /**
     * Get the participant's id
     * @return Participant's id
     */
    public int getPId() {
        return mPId;
    }

}
