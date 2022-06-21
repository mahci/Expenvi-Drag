package experiment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static tools.Consts.*;

public class Experiment {
    private final static String NAME = "Experiment/";

    //--- Participant's things!
    private int mPId;

    // Technique ---------------------------------------------------------------------------------------------
    public enum TECHNIQUE {
        TAP_PRESS_HOLD, TWO_FINGER_SWIPE, MOUSE; // TAP-PRESS-HOLD, TWO-FINGER-SWIPE-DOWN
        private static final TECHNIQUE[] values = values();
        public String getTitle() {
            switch (this) {
                case MOUSE -> {
                    return "Mouse";
                }
                case TAP_PRESS_HOLD -> {
                    return "Tap-Press-Hold";
                }
                case TWO_FINGER_SWIPE -> {
                    return "Two-Finger-Swipe";
                }
            }

            return "";
        }
    }

    public enum TASK {
        BOX, BAR, PEEK, TUNNEL;

        public String getTitle() {
            String result = "";
            switch (this) {
                case BOX -> result = "Box Task";
                case BAR -> result = "Bar Task";
                case PEEK -> result = "Peek Task";
                case TUNNEL -> result = "Tunnel Tsak";
            }

            return result;
        }

        public Color getBgColor() {
            Color result = Color.WHITE;
            switch (this) {
                case BOX -> result = COLORS.GREEN_100;
                case BAR -> result = COLORS.BLUE_100;
                case PEEK -> result = COLORS.YELLOW_100;
                case TUNNEL -> result = COLORS.PURPLE_100;
            }

            return result;
        }

        public Color getFgColor() {
            Color result = COLORS.GRAY_900;
            switch (this) {
                case BOX -> result = COLORS.GREEN_900;
                case BAR -> result = COLORS.BLUE_900;
                case PEEK -> result = COLORS.YELLOW_900;
                case TUNNEL -> result = COLORS.PURPLE_900;
            }

            return result;
        }
    }

    public enum ACTION {
        MOVE, GRAB, DRAG, RELEASE, REVERT;
    }

    public enum MODE {
        DEMO, PRACTICE, TEST;
    }


    // Tasks -------------------------------------------------------------------------------------------------
    public static class BoxTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {28, 40}; // Object widths
        private static final int[] TARGET_WIDTHS = new int[] {82, 134}; // Target widths (mm)
        private static final int[] STRAIGHT_VALS = new int[] {0, 1}; // Straightness values

        public static final int DIST_mm = 50;
        public static final double NT_DIST_mm = 30;

        public BoxTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

//            final int dist = Utils.mm2px(DIST_mm);

            List<Integer> config = new ArrayList<>();
            for (int objW : OBJECT_WIDTHS) {
                for (int tgtW : TARGET_WIDTHS) {
                    for (int stV : STRAIGHT_VALS) {
                        config.add(objW);
                        config.add(tgtW);
                        config.add(stV);

                        // Create trials based on the combination
                        result.mTrials.add(new BoxTrial(config, DIST_mm));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }

        @Override
        public String toString() {
            return "BoxTask";
        }
    }

    // -------------------------------------------------------------------------------------
    public static class BarTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {2, 4}; // Object widths (mm)
        private static final int[] TARGET_WIDTHS = new int[] {5, 8}; // Target widths (mm)
        private static final int[] AXISES = new int[] {0, 1}; // Axises ordinals

        public static final int DIST_mm = 50; // mm
        public static final int OBJECT_LEN_mm = 5; // mm
        public static final int TARGET_LEN_mm = 30; // mm

        public BarTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>(); // Best to have as int so px values + enums
            for (int objW : OBJECT_WIDTHS) {
                for (int tgtW : TARGET_WIDTHS) {
                    for (int axis : AXISES) {
                        config.add(objW);
                        config.add(tgtW);
                        config.add(axis);

                        // Create trials based on the combination
                        result.mTrials.add(new BarTrial(config,
                                OBJECT_LEN_mm, TARGET_LEN_mm,
                                DIST_mm));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }

        @Override
        public String toString() {
            return "BarTask";
        }
    }

    // -------------------------------------------------------------------------------------
    public static class PeekTask extends Task {
        private static final int[] OBJECT_WIDTHS = new int[] {6}; // Object widths (mm)
        private static final int[] TARGET_WIDTHS = new int[] {18, 36}; // Target widths (mm)
        private static final int[] AXISES = new int[] {0, 1}; // Axises ordinals

        public static final int LEN_mm = 100; // mm
        public static final int DIST_mm = 100; // mm
        public static final int TEMP_W_mm = 18; // mm

        public PeekTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            for (int objW : OBJECT_WIDTHS) {
                for (int tgtW : TARGET_WIDTHS) {
                    for (int axis : AXISES) {
                        config.add(objW);
                        config.add(tgtW);
                        config.add(axis);

                        // Create trials based on the combination
                        result.mTrials.add(new PeekTrial(config, LEN_mm, DIST_mm, TEMP_W_mm));

                        config.clear();
                    }
                }
            }

            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }

        @Override
        public String toString() {
            return "PeekTask";
        }
    }

    // -------------------------------------------------------------------------------------
    public static class TunnelTask extends Task {
        private final int[] TUNNEL_LENS = new int[]{100}; // Tunnel length (in mm)
        private final int[] TUNNEL_WIDTHS = new int[]{5, 8}; // Tunnel widths (in mm)
        private static final int[] AXISES = new int[]{0, 1}; // Axises ordinals

        public final double LINES_W_mm = 0.5; // Tunnel lines' width
        public final double TEXT_W_mm = 8; // Width of the start text rectangle

        public static final double DRAG_THRSH_mm = 5; // Movement > threshold => Dragging starts

        public TunnelTask(int nBlocks) {
            super(nBlocks);

            for (int i = 0; i < nBlocks; i++) {
                mBlocks.add(genBlock());
            }
        }

        private Block genBlock() {
            Block result = new Block();

            List<Integer> config = new ArrayList<>();
            for (int tunnL : TUNNEL_LENS) {
                for (int tunnW : TUNNEL_WIDTHS) {
                    for (int axis : AXISES) {
                        config.add(tunnL);
                        config.add(tunnW);
                        config.add(axis);

                        // Create trials based on the combination
                        result.mTrials.add(new TunnelTrial(config, LINES_W_mm, TEXT_W_mm));

                        config.clear();
                    }
                }
            }


            // Shuffle trials
            Collections.shuffle(result.mTrials);

            return result;
        }

        @Override
        public String toString() {
            return "TunnelTask";
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


