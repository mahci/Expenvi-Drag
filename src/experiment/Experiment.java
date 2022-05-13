package experiment;

import tools.Utils;

import java.util.ArrayList;
import java.util.List;

public class Experiment {
    private final static String NAME = "Experiment/";

    //-- Constants
//    public static final DimensionD VT_PANE_DIM_mm = new DimensionD(130.0, 145.0);

    public enum DIRECTION {
        N(0), S(1), E(2), W(3), NE(4), NW(5), SE(6), SW(7);
        private final int n;

        DIRECTION(int i) { n = i; }
        // Get a NE/NW/SE/SW randomly
        public static DIRECTION randDiag() {
            return DIRECTION.values()[Utils.randInt(4, 8)];
        }
        public static DIRECTION randVertical() {
            return DIRECTION.values()[Utils.randInt(0, 2)];
        }
        public static DIRECTION randHorizontal() {
            return DIRECTION.values()[Utils.randInt(2, 4)];
        }
        // Get a random direction from two
        public static DIRECTION randOne(DIRECTION d0, DIRECTION d1) {
            if (Utils.randInt(0, 2) == 0) return d0;
            else return d1;
        }
        // Get a random direction
        public static DIRECTION random() {
            return DIRECTION.values()[Utils.randInt(0, 8)];
        }
        // Get direction from ordinal
        public static DIRECTION get(int i) {return DIRECTION.values()[i];}
        // Get Axis
        public AXIS getAxis() {
            if (this.equals(N) || this.equals(S)) return AXIS.VERTICAL;
            if (this.equals(W) || this.equals(E)) return AXIS.HORIZONTAL;
            else return AXIS.DIAGONAL;
        }

        // Get the opposite direction (Horizontal)
        public static DIRECTION oppHz(DIRECTION dr) {
            return switch (dr) {
                case N -> N;
                case S -> S;
                case E -> W;
                case W -> E;
                case NE -> NW;
                case NW -> NE;
                case SE -> SW;
                case SW -> SE;
            };
        }

        // Get the opposite direction (Vertical)
        public static DIRECTION oppVt(DIRECTION dr) {
            return switch (dr) {
                case N -> S;
                case S -> N;
                case E -> E;
                case W -> W;
                case NE -> SE;
                case NW -> SW;
                case SE -> NE;
                case SW -> NW;
            };
        }
    }

    public enum AXIS {
        VERTICAL(0), HORIZONTAL(1), DIAGONAL(2);
        private final int n;

        AXIS(int n) {
            this.n = n;
        }

//        public static int[] list() {
//            return new int[]{VERTICAL.ordinal(), HORIZONTAL.ordinal()};
//        }
    }

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

    // Tasks -------------------------------------------------------------------------------------------------
    private class BoxTask extends Task {
        private static final int[] AXISES = new int[] {0, 1, 2}; // Axises ordinals
        private static final int[] OBJ_WIDTHS = new int[] {0, 0}; // Object widths (TODO)
        private static final int[] TARGET_WIDTHS = new int[] {0, 0}; // Tunnel widths (mm)

        public BoxTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(new Block(AXISES, OBJ_WIDTHS, TARGET_WIDTHS));
            }
        }
    }

    private class BarTask extends Task {
        private static final int[] AXISES = new int[] {0, 1}; // Axises ordinals
        private static final int[] OBJ_WIDTHS = new int[] {4, 8}; // Object (Bar) widths (mm)
        private static final int[] TARGET_WIDTHS = new int[] {16, 32}; // Target widths (mm)

        public BarTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(new Block(AXISES, OBJ_WIDTHS, TARGET_WIDTHS));
            }
        }
    }

    public static class TunnelTask extends Task {
//        private final int[] AXISES = new int[] {0, 1}; // Axises ordinals
        private final int[] DIRS = new int[] {0, 1, 2, 3};
        private final int[] DISTS = new int[] {150}; // Tunnel length (in mm)
        private final int[] WIDTHS = new int[] {5, 10}; // Tunnel widths (in mm)

        public final double LINES_W_mm = 1; // Targets width
        public final double TEXT_W_mm = 8; // Width of the start text rectangle

        public final double DRAG_THRSH_mm = 5; // Movement > threshold => Dragging starts

        public final double NT_DIST_mm = 100;
        public final long NT_DELAY_ms = 700; // Delay before showing the next trial

        public TunnelTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            final int linesW = Utils.mm2px(LINES_W_mm);
            final int textW = Utils.mm2px(TEXT_W_mm);
            for (int vi : DIRS) {
                for (int vj : DISTS) {
                    for (int vk : WIDTHS) {
                        config.add(vi);
                        config.add(vj);
                        config.add(vk);

                        // Create trials based on the combination
                        result.mTrials.add(new TunnelTrial(config, linesW, textW));

                        config.clear();
                    }
                }
            }

            return result;
        }

        public Block getBlock(int ind) {
            return mBlocks.get(ind);
        }
    }


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
