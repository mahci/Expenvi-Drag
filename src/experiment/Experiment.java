package experiment;

import tools.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experiment {
    private final static String NAME = "Experiment/";

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


