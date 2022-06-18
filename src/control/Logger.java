package control;

import experiment.Task;
import experiment.Trial;
import tools.Utils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tools.Consts.STRINGS.*;
import static experiment.Experiment.*;

public class Logger {
    private final static String NAME = "Logger/";

    private static Logger self;

    private final String TOP_LOGS_DIR = "/Users/mahmoud/Documents/Academics/PhD/MIDE/Logs";
    private static final String DRAG_LOG_DIR_NAME = "Drag";

    private String mPcLogId = "";
    private static Path mLogDirectory; // Main folder for logs
    private static Path mPcLogDirectory; // Folder log path of the participant

    // Different log files
//    private Path mTrialsFilePath;
    private PrintWriter mTrialsFilePW;

//    private Path mInstantsLogPath;
    private PrintWriter mInstantFilePW;

//    private Path mTimesFilePath;
    private PrintWriter mTimesFilePW;

    // -------------------------------------------------------------------------------------------

    /**
     * Get the instance
     * @return Singleton instance
     */
    public static Logger get() {
        if (self == null) self = new Logger();
        return self;
    }

    /**
     * Private constructor
     */
    private Logger() {
        // Create log directory
//        final Path parentPath = Paths.get("").toAbsolutePath().getParent();
        final Path parentPath = Paths.get(TOP_LOGS_DIR);
        mLogDirectory = parentPath.resolve(DRAG_LOG_DIR_NAME);

        // Create the folder if doesn't exist
        Utils.createDirIfNotExisted(mLogDirectory);
    }

    /**
     * Log when a new particiapnt starts (create folder)
     * @param pId Participant's ID
     */
    public void logParticipant(int pId) {
        final String TAG = NAME + "logParticipant";

        mPcLogId = P_INIT + pId;
//        final String pcExpLogId = pcLogId + "_" + Utils.nowDate(); // Experiment Id

        // Create a folder for the participant (if not already created)
        mPcLogDirectory = mLogDirectory.resolve(mPcLogId);
        Utils.createDirIfNotExisted(mPcLogDirectory);

        // Create/open log files
        openLogFilesToWrite();

    }

    /**
     * Log TrialInfo
     * @param genInfo GeneralInfo
     * @param trialInfo TrialInfo
     */
    public void logTrialInfo(GeneralInfo genInfo, TrialInfo trialInfo) {
        final String TAG = NAME + "logTrialInfo";

        try {
            // Open logs if not opened
            if (mTrialsFilePW == null) openLogFilesToWrite();

            mTrialsFilePW.println(genInfo + SP + trialInfo);
//            mTrialsFilePW.flush();

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging trial!");
            e.printStackTrace();
        }
    }

    /**
     * Log InstantInfo
     * @param genInfo GeneralInfo
     * @param instInfo InstantInfo
     */
    public void logInstantInfo(GeneralInfo genInfo, InstantInfo instInfo) {
        final String TAG = NAME + "logInstant";

        try {
            // Open logs if not opened
            if (mInstantFilePW == null) openLogFilesToWrite();

            mInstantFilePW.println(genInfo + SP + instInfo);
//            mInstantFilePW.flush();

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging instant!");
        }
    }

    /**
     * Log TimeInfo
     * @param genInfo GeneralInfo
     * @param timeInfo TimeInfo
     */
    public void logTimeInfo(GeneralInfo genInfo, TimeInfo timeInfo) {
        final String TAG = NAME + "logInstant";

        try {
            // Open logs if not opened
            if (mTimesFilePW == null) openLogFilesToWrite();

            mTimesFilePW.println(genInfo + SP + timeInfo);
//            mTimesFilePW.flush();

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging time!");
        }
    }

    /**
     * Close all log files
     */
    public void closeLogs() {
        if (mTrialsFilePW != null) mTrialsFilePW.close();
        if (mInstantFilePW != null) mInstantFilePW.close();
        if (mTimesFilePW != null) mTimesFilePW.close();
    }

    private void openLogFilesToWrite() {

        final String pcDateId = mPcLogId + "_" + Utils.nowDate();

        // Log files for the participant
        final File trialsLogFile = mPcLogDirectory.resolve(pcDateId + "_" + "TRIALS.txt").toFile();
        final File instantsLogFile = mPcLogDirectory.resolve(pcDateId + "_" + "INSTANTS.txt").toFile();
        final File timesLogFile = mPcLogDirectory.resolve(pcDateId + "_" + "TIMES.txt").toFile();

        // Create files (w/ autoflash) and if not existed, write headers. Append is for later writings
        try {
            mTrialsFilePW = new PrintWriter(
                    new FileOutputStream(trialsLogFile, true),
                    true);

            mInstantFilePW = new PrintWriter(
                    new FileOutputStream(instantsLogFile, true),
                    true);

            mTimesFilePW = new PrintWriter(
                    new FileOutputStream(timesLogFile, true),
                    true);

            // Write headers first time
            if (Utils.isFileEmpty(trialsLogFile)) {
                mTrialsFilePW.println(
                        Utils.classPropsNames(GeneralInfo.class) + SP +
                        Trial.getLogHeader() + SP +
                        Utils.classPropsNames(TrialInfo.class));
            }

            if (Utils.isFileEmpty(instantsLogFile)) {
                mInstantFilePW.println(getLogHeaders(GeneralInfo.class, InstantInfo.class));
            }

            if (Utils.isFileEmpty(timesLogFile)) {
                mTimesFilePW.println(getLogHeaders(GeneralInfo.class, TimeInfo.class));
            }

        } catch (IOException e) {
            e.printStackTrace();
//            Main.showDialog("Problem in logging the participant!");
        }
    }

    /**
     * Get concated log headers of two classes
     * @param cl1 Class 1
     * @param cl2 Class 2
     * @return Concated log headers (with SP in between)
     */
    private String getLogHeaders(Class<?> cl1, Class<?> cl2) {
        return Utils.classPropsNames(cl1) + SP + Utils.classPropsNames(cl2);
    }


    // Log infos (each row = one trial) --------------------------------------------------------------------

    // General info regarding every trial
    public static class GeneralInfo {
        public Task task;
        public TECHNIQUE technique;
        public int block_num;
        public int trial_num;

        @Override
        public String toString() {
            final Field[] fields = getClass().getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            try {
                for (Field field : fields) {
                    sb.append(field.get(this)).append(SP);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return sb.deleteCharAt(sb.length() - 1).toString();
        }

    }

    // Main trial info (all times in ms)
    public static class TrialInfo {
        public Trial trial;

        public int point_time;      // From first movement to last entry ({Tunnel} 0)
        public int grab_time;       // From last entry to grab ({Tunnel} move > last grab)

        public int grab_x;          // Last GRAB X coordinate
        public int grab_y;          // Last RELEASE Y coordinate

        public int entry_time;      // {Tunnel} last grab > tunnel entry
        public int drag_time;       // From grab to last target entry ({Tunnel} tunnel entry > tunnel exit)
        public int release_time;    // From last target entry to release ({Tunnel} tunnel exit > release)

        public int release_x;       // RELEASE X coordinate
        public int release_y;       // RELEASE Y coordinate

        public int trial_time;      // From last GRAB to last RELEASE
        public int total_time;      // From first move to last RELEASE

        public int result;          //1 (Hit) or 0 (Miss)

        @Override
        public String toString() {
            final Field[] fields = getClass().getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            try {
                for (Field field : fields) {
                    sb.append(field.get(this)).append(SP);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return sb.deleteCharAt(sb.length() - 1).toString();
        }

    }

    // Instants of events in a trial (all times in system timestamp (ms))
    public static class InstantInfo {
        public long trial_show;             // the moment trial is shown on the screen
        public long first_move;             // first movement of the cursor

        public long first_cur_obj_entry;    // first entry to the object
        public long last_cur_obj_entry;     // last entry to the object

        public long first_grab;             // the moment of the first grab
        public long last_grab;              // the moment of the last grab

        public long drag_start;             // the moment of DRAG start (no first/last <= always hit/miss after first)

        public long temp_entry;             // entry moment to the Temp area {Peek}
        public long tunnel_entry;           // entry moment to the tunnel

        public long first_cur_tgt_entry;    // first time the cursor enters the target (or exit Tunnel) while dragging
        public long last_cur_tgt_entry;     // last time the cursor enters the target (or exit Tunnel) while dragging

        public long first_obj_tgt_entry;
        public long last_objt_tgt_entry;

        public long first_release;
        public long last_release;

        public long revert;                 // The moment of REVERT

        public void logMove() {
            if (first_move == 0) first_move = Utils.nowMillis();
        }

        public void logGrab() {
            if (first_grab == 0) {
                first_grab = Utils.nowMillis();
                last_grab = Utils.nowMillis();
            } else {
                last_grab = Utils.nowMillis();
            }
        }

        public void logCurObjEntry() {
            if (first_cur_obj_entry == 0) {
                first_cur_obj_entry = Utils.nowMillis();
                last_cur_obj_entry = Utils.nowMillis();
            } else {
                last_cur_obj_entry = Utils.nowMillis();
            }
        }

        public void logCurTgtEntry() {
            if (first_cur_tgt_entry == 0) {
                first_cur_tgt_entry = Utils.nowMillis();
                last_cur_tgt_entry = Utils.nowMillis();
            } else {
                last_cur_tgt_entry = Utils.nowMillis();
            }
        }

        public void logObjTgtEntry() {
            if (first_obj_tgt_entry == 0) {
                first_obj_tgt_entry = Utils.nowMillis();
                last_objt_tgt_entry = Utils.nowMillis();
            } else {
                last_objt_tgt_entry = Utils.nowMillis();
            }
        }

        public void logRelease() {
            if (first_release == 0) {
                first_release = Utils.nowMillis();
                last_release = Utils.nowMillis();
            } else {
                last_release = Utils.nowMillis();
            }
        }

        @Override
        public String toString() {
            final Field[] fields = getClass().getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            try {
                for (Field field : fields) {
                    sb.append(field.getLong(this)).append(SP);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }

    // Time info (not instnats)
    public static class TimeInfo {
        public int trial_time;     // From trial_show to hit/miss
        public int block_time;     // From first trial_show to the last release in the block
        public int homing_time;    // From comb key press to first move
        public int task_time;       // From first trial in the task to the last
        public long exp_time;       // From first trial_show to the last trial's release

        @Override
        public String toString() {
            final Field[] fields = getClass().getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            try {
                for (Field field : fields) {
                    sb.append(field.get(this)).append(SP);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }


}
