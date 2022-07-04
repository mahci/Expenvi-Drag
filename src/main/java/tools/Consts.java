package tools;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static tools.Consts.AXIS.*;

public class Consts {

    //-- Display and related
    public static class DISP {
        public final static int MACBOOK_PPI = 127;
        public final static int APPLE_DISP_PPI = 109;
        public final static int BENQ_PPI = 89;

        public final static int DPI = APPLE_DISP_PPI;
        public final static double MM_in_INCH = 25.4;
    }

    //-- Colors and related
    public static class COLORS {
        public final static Color GRAY_200 = Color.decode("#EEEEEE");
        public final static Color GRAY_400 = Color.decode("#BDBDBD");
        public final static Color GRAY_500 = Color.decode("#9E9E9E");
        public final static Color GRAY_800 = Color.decode("#424242");
        public final static Color GRAY_900 = Color.decode("#212121");

        public final static Color BLUE_50 = Color.decode("#E3F2FD");
        public final static Color BLUE_100 = Color.decode("#BBDEFB");
        public final static Color BLUE_900 = Color.decode("#0D47A1");
        public final static Color BLUE_900_ALPHA = new Color(13, 71, 161, 170);

        public final static Color PURPLE_100 = Color.decode("#E1BEE7");
        public final static Color PURPLE_900 = Color.decode("#4A148C");

        public final static Color INDIGO_900 = Color.decode("#1A237E");

        public final static Color GREEN_100 = Color.decode("#C8E6C9");
        public final static Color GREEN_200 = Color.decode("#A5D6A7");
        public final static Color GREEN_400 = Color.decode("#66BB6A");
        public final static Color GREEN_700 = Color.decode("#388E3C");
        public final static Color GREEN_900 = Color.decode("#1B5E20");
        public final static Color GREEN_A400 = Color.decode("#00E676");

        public final static Color YELLOW_100 = Color.decode("#FFF9C4");
        public final static Color YELLOW_800 = Color.decode("#F9A825");
        public final static Color YELLOW_900 = Color.decode("#F57F17");

        public final static Color ORANGE_400 = Color.decode("#FFA726");
        public final static Color ORANGE_200 = Color.decode("#FFCC80");
    }

    //-- Fonts and related
    public static class FONTS {
        // Fonts
        public static Font SERIF = Font.getFont(Font.SERIF);
        public static Font DIALOG = new Font(Font.DIALOG,  Font.PLAIN, 12);
        public static Font STATUS = new Font(Font.DIALOG,  Font.PLAIN, 18);

        // Sizes and spacings
        public static final float TEXT_FONT_SIZE = 20.5f;
        public static final float TEXT_LINE_SPACING = 0.193f;

        // Run-time
        static {
            try {
                File sfRegFile = new File("./res/SF-Regular.ttf");
                File sfLightFile = new File("./res/SF-Light.ttf");

                SERIF = Font.createFont(Font.TRUETYPE_FONT, sfRegFile);
//                DIALOG = Font.createFont(Font.TRUETYPE_FONT, sfLightFile);

            } catch (FontFormatException | IOException e) {
                Out.d("FONTS", "Can't load the font file!");
                e.printStackTrace();
            }
        }
    }

    //-- Sounds and related
    public static class SOUNDS {
        private static Clip startErrClip, hitClip, missClip, taskEndClip;

        static {
            try {
                final File startErrFile = new File("./res/start_err.wav");
                final File hitFile = new File("./res/hit.wav");
                final File missFile = new File("./res/miss.wav");
                final File taskEndFile = new File("./res/end.wav");

                startErrClip = AudioSystem.getClip();
                startErrClip.open(AudioSystem.getAudioInputStream(startErrFile));

                hitClip = AudioSystem.getClip();
                hitClip.open(AudioSystem.getAudioInputStream(hitFile));

                missClip = AudioSystem.getClip();
                missClip.open(AudioSystem.getAudioInputStream(missFile));

                taskEndClip = AudioSystem.getClip();
                taskEndClip.open(AudioSystem.getAudioInputStream(taskEndFile));

            } catch (NullPointerException | IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        public static void playStartError() {
            startErrClip.setMicrosecondPosition(0); // Reset to the start of the file
            startErrClip.start();
        }

        public static void playHit() {
            hitClip.setMicrosecondPosition(0); // Reset to the start of the file
            hitClip.start();
        }

        public static void playMiss() {
            missClip.setMicrosecondPosition(0); // Reset to the start of the file
            missClip.start();
        }

        public static void playTaskEnd() {
            taskEndClip.setMicrosecondPosition(0); // Reset to the start of the file
            taskEndClip.start();
        }
    }

    public static class CURSORS {
        public static Cursor RESIZE_NS, RESIZE_EW, DEFAULT;

        static {
            RESIZE_NS = Toolkit.getDefaultToolkit().createCustomCursor(
                    new ImageIcon("./res/Resize_NS.png").getImage(),
                    new Point(8,10),
                    "Resize_NS");
            RESIZE_EW = Toolkit.getDefaultToolkit().createCustomCursor(
                    new ImageIcon("./res/Resize_EW.png").getImage(),
                    new Point(15,8),
                    "Resize_NS");

            DEFAULT = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
    }

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
            AXIS result = VERTICAL;
            switch (this) {
                case N, S -> result = VERTICAL;
                case E, W -> result = HORIZONTAL;
                case NE, SW -> result = FOR_DIAG;
                case NW, SE -> result = BACK_DIAG;
            }

            return result;
        }
        public DIRECTION opposite() {
            switch (this) {
                case N -> { return S; }
                case S -> { return N; }
                case W -> { return E; }
                case E -> { return W; }

                case NE -> { return SW; }
                case NW -> { return SE; }
                case SE -> { return NW; }
                case SW -> { return NE; }
            }

            return N;
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

        public static AXIS get(DIRECTION dir) {
            AXIS result = VERTICAL;
            switch (dir) {
                case N, S -> result = VERTICAL;
                case E, W -> result = HORIZONTAL;
                case NE, SW -> result = FOR_DIAG;
                case NW, SE -> result = BACK_DIAG;
            }

            return result;
        }
    }

    public enum STRAIGHTNESS {
        STRAIGHT (0), DIAGONAL (1);
        private final int n;

        STRAIGHTNESS(int i) {
            n = i;
        }

        public static STRAIGHTNESS get(int n) {
            return STRAIGHTNESS.values()[n];
        }

        public DIRECTION randDir() {
            switch (this) {
                case STRAIGHT -> {
                    return DIRECTION.get(Utils.randInt(0, 4));
                }
                case DIAGONAL -> {
                    return DIRECTION.get(Utils.randInt(4, 8));
                }
            }

            return null;
        }
    }

    //------------------------------------------------------------------------------------------

    //-- Strings and related
    public static class STRINGS {
        public final static String SP = ";";
        public final static String MSP = "&";
        public static final String INTRO = "INTRO";
        public static final String MOOSE = "MOOSE";
        public final static String TECH = "TECH";
        public final static String CONFIG = "CONFIG";
        public final static String CONNECTION = "CONNECTION";
        public final static String LOG = "LOG";
        public final static String EXP_ID = "EXPID"; // Id for an experiment
        public final static String GENLOG = "GENLOG";
        public final static String BLOCK = "BLOCK";
        public final static String TRIAL = "TRIAL";
        public final static String TSK = "TASK"; // TSK to not confuse with TASK
        public final static String END = "END";
        public final static String P_INIT = "P";

        public final static String GRAB = "GRAB";
        public final static String DRAG = "DRAG";
        public final static String RELEASE = "RELEASE";
        public final static String REVERT = "REVERT";

        public final static String DEMO_TITLE = "Welcome to the scrolling experiment!";
        public final static String DEMO_NEXT = "First, let's have a demo >";

        public final static String SHORT_BREAK_TEXT =
                "<html>Time for a quick break! To continue, press <B>ENTER</B>.</html>";

        public static final String DLG_BREAK_TITLE  = "Time for a break!";
        public static final String DLG_BREAK_TEXT   =
                "<html>When ready, press <B>BLUE + RED</B> keys to start the next block</html>";

        public final static String EXP_START_MESSAGE =
                "To begin the experiment, press SPACE.";
        public final static String END_EXPERIMENT_MESSAGE =
                "All finished! Thank you for participating in this experiment!";



    }

}
