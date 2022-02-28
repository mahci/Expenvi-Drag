package tools;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Consts {

    //-- Display and related
    public static class DISP {
        public final static int APPLE_DISP_PPI = 109;
        public final static int BENQ_PPI = 89;

        public final static double MM_in_INCH = 25.4;
    }

    //-- Colors and related
    public static class COLORS {
        public final static Color PANEL_BG = Color.decode("#F5F5F5");
    }

    //-- Fonts and related
    public static class FONTS {
        // Fonts
        public static Font SERIF = Font.getFont(Font.SERIF);
        public static Font DIALOG = new Font(Font.DIALOG,  Font.PLAIN, 5);

        // Sizes and spacings
        public static final float TEXT_FONT_SIZE = 20.5f;
        public static final float TEXT_LINE_SPACING = 0.193f;

        // Run-time
        static {
            try {
                File sfRegFile = new File("./res/SF-Regular.ttf");
                File sfLightFile = new File("./res/SF-Light.ttf");

                SERIF = Font.createFont(Font.TRUETYPE_FONT, sfRegFile);
                DIALOG = Font.createFont(Font.TRUETYPE_FONT, sfLightFile);

            } catch (FontFormatException | IOException e) {
                Logs.d("FONTS", "Can't load the font file!");
                e.printStackTrace();
            }
        }
    }

    //-- Sounds and related
    public static class SOUNDS {
        public final static String HIT_SOUND = "hit.wav";
        public final static String MISS_SOUND = "miss.wav";
        public final static String TECH_END_SOUND = "end.wav";
    }

    //-- Strings and related
    public static class STRINGS {
        public final static String SP = ";";
        public final static String TECH = "TECH";
        public final static String SCROLL = "SCROLL";
        public final static String STOP = "STOP";
        public final static String CONFIG = "CONFIG";
        public final static String SENSITIVITY = "SENSITIVITY";
        public final static String GAIN = "GAIN";
        public final static String DENOM = "DENOM";
        public final static String COEF = "COEF";
        public final static String LOG = "LOG";
        public final static String EXP_ID = "EXPID"; // Id for an experiment
        public final static String BLOCK = "BLOCK";
        public final static String TRIAL = "TRIAL";
        public final static String TSK = "TSK";
        public final static String P_INIT = "P";
        public final static String END = "END";

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
