package experiment;

import tools.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experiment {
    private final static String NAME = "Experiment/";

    //-- Constants
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
            else return AXIS.FOR_DIAG;
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
        VERTICAL(0), HORIZONTAL(1), FOR_DIAG(2) /* / */, BACK_DIAG(3) /* \ */;
        private final int n;

        AXIS(int n) {
            this.n = n;
        }

        public static AXIS get(int n) {
            return AXIS.values()[n];
        }

        public DIRECTION randDir() {
            DIRECTION result;
            switch (this) {
                case VERTICAL -> result = DIRECTION.randOne(DIRECTION.N, DIRECTION.S);
                case HORIZONTAL -> result = DIRECTION.randOne(DIRECTION.W, DIRECTION.E);
                case FOR_DIAG -> result = DIRECTION.randOne(DIRECTION.NE, DIRECTION.SW);
                case BACK_DIAG -> result = DIRECTION.randOne(DIRECTION.NW, DIRECTION.SE);
                default -> result = DIRECTION.N;
            }

            return result;
        }
    }

    //--- Participant's things!
    private int mPId;

    // Tasks -------------------------------------------------------------------------------------------------
    public static class BoxTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {13, 20}; // Object widths
        private static final int[] TARGET_WIDTHS = new int[] {40, 60}; // Target widths (mm)
        private static final int[] AXISES = new int[] {0, 1, 2, 3}; // Axises ordinals

        public static final int DIST_mm = 20; // mm

        public static final double NT_DIST_mm = 20;

        public BoxTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            final int dist = Utils.mm2px(DIST_mm);

            for (int vi : OBJECT_WIDTHS) {
                for (int vj : TARGET_WIDTHS) {
                    for (int vk : AXISES) {
                        config.add(Utils.mm2px(vi));
                        config.add(Utils.mm2px(vj));
                        config.add(vk);

                        // Create trials based on the combination
                        result.mTrials.add(new BoxTrial(config, dist));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }

    }

    // -------------------------------------------------------------------------------------
    public static class BarTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {2, 5}; // Object widths (mm)
        private static final int[] TARGET_WIDTHS = new int[] {6, 8}; // Tunnel widths (mm)
        private static final int[] AXISES = new int[] {0, 1}; // Axises ordinals

        public static final int DIST_mm = 50; // mm
        public static final int OBJECT_LEN_mm = 5; // mm
        public static final int TARGET_LEN_mm = 50; // mm
        public static final int TARGET_LINES_THICKNESS_mm = 1; // mm

        public BarTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            final int dist = Utils.mm2px(DIST_mm);
            final int objLen = Utils.mm2px(OBJECT_LEN_mm);
            final int tgtLen = Utils.mm2px(TARGET_LEN_mm);
            final int tgtLinesThickness = Utils.mm2px(TARGET_LINES_THICKNESS_mm);

            for (int vi : OBJECT_WIDTHS) {
                for (int vj : TARGET_WIDTHS) {
                    for (int vk : AXISES) {
                        config.add(Utils.mm2px(vi));
                        config.add(Utils.mm2px(vj));
                        config.add(vk);

                        // Create trials based on the combination
                        result.mTrials.add(new BarTrial(config, objLen, tgtLen, tgtLinesThickness, dist));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }
    }

    // -------------------------------------------------------------------------------------
    public static class PeekTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {1, 3}; // Object widths (mm)
        private static final int[] TARGET_WIDTHS = new int[] {5, 8}; // Target widths (mm)
        private static final int[] AXISES = new int[] {0, 1}; // Axises ordinals

        public static final int LEN_mm = 100; // mm
        public static final int DIST_mm = 50; // mm
        public static final int TEMP_W_mm = 10; // mm

        public PeekTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            final int len = Utils.mm2px(LEN_mm);
            final int dist = Utils.mm2px(DIST_mm);
            final int tempW = Utils.mm2px(TEMP_W_mm);

            for (int vi : OBJECT_WIDTHS) {
                for (int vj : TARGET_WIDTHS) {
                    for (int vk : AXISES) {
                        config.add(Utils.mm2px(vi));
                        config.add(Utils.mm2px(vj));
                        config.add(vk);

                        // Create trials based on the combination
                        result.mTrials.add(new PeekTrial(config, len, dist, tempW));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }
    }

    // -------------------------------------------------------------------------------------
    public static class TunnelTask extends Task {
        //        private final int[] AXISES = new int[] {0, 1}; // Axises ordinals
        private final int[] DIRS = new int[]{0, 1, 2, 3};
//        private final int[] DISTS = new int[]{150}; // Tunnel length (in mm)
        private final int[] DISTS = new int[]{100}; // Tunnel length (in mm)
        private final int[] WIDTHS = new int[]{5, 10}; // Tunnel widths (in mm)

        public final double LINES_W_mm = 1; // Targets width
        public final double TEXT_W_mm = 8; // Width of the start text rectangle

        public static final double DRAG_THRSH_mm = 5; // Movement > threshold => Dragging starts

        public static final double NT_DIST_mm = 100;

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

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
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


