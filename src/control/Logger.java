package control;

import com.sun.tools.javac.Main;
import experiment.BarTrial;
import experiment.BoxTrial;
import experiment.Task;
import experiment.Trial;
import panels.MainFrame;
import tools.Out;
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

            //-- Write headers (only the first time)
            if (Utils.isFileEmpty(trialsLogFile)) {
                // Custom header because of the Trial part
                mTrialsFilePW.println(
                        Utils.classPropsNames(GeneralInfo.class) + SP +
                        Trial.getLogHeader() + SP +
                        Utils.classPropsNames(TrialInfo.class).replace("trial;", ""));
            }

            if (Utils.isFileEmpty(instantsLogFile)) {
                mInstantFilePW.println(getLogHeaders(GeneralInfo.class, InstantInfo.class));
            }

            if (Utils.isFileEmpty(timesLogFile)) {
                mTimesFilePW.println(getLogHeaders(GeneralInfo.class, TimeInfo.class));
            }

        } catch (IOException e) {
            e.printStackTrace();
            MainFrame.get().showMessage("Problem in opening log files");
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

        public int point_time;          // From first movement to last entry ({Tunnel} 0)

        public int grab_time;           // From last entry to grab ({Tunnel} move > last grab)
        public int grab_x;              // Last GRAB X coordinate
        public int grab_y;              // Last GRAB Y coordinate

        public int temp_entry_time;     // {Peek} last grab > temp entry
        public int temp_to_tgt_time;    // {Peek} last temp exit > last target entry

        public int tunnel_entry_time;   // {Tunnel} last grab > tunnel entry

        // From last GRAB to last target entry ({Tunnel} tunnel entry > tunnel exit) ({Bar, Peek} obj in target entry)
        public int drag_time;

        // From last target entry to release ({Tunnel} tunnel exit > release) ({Bar, Peek} obj in target entry)
        public int release_time;
        public int release_x;           // RELEASE X coordinate
        public int release_y;           // RELEASE Y coordinate

        // {Peek} From last obj into temp entry to revert
        public int revert_time;
        public int revert_x;           // {Peek} REVERT X coordinate
        public int revert_y;           // {Peek} REVERT Y coordinate

        public int trial_time;          // From last GRAB to last RELEASE/REVERT
        public int total_time;          // From first move to last RELEASE/REVERT

        public int result;              //1 (Hit) or 0 (Miss)

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

        // first/last entry of the cursor into the object
        public long first_cur_obj_entry;
        public long last_cur_obj_entry;

        // first/last grab moments (last only different if start_error)
        public long first_grab;
        public long last_grab;

        // the moment of DRAG start (no first/last <= always hit/miss after first)
        public long drag_start;

        // {Peek} first/last time the cursor (while dragging) enters temp
        public long first_cur_temp_entry;
        public long last_cur_temp_entry;

        // {Peek} first/last time the object enters temp
        public long first_obj_temp_entry;
        public long last_obj_temp_entry;

        // {Peek} first/last time the object exits temp
        public long first_obj_temp_exit;
        public long last_obj_temp_exit;

        // entry moment to the tunnel (while dragging)
        public long tunnel_entry;

        // first/las time the cursor enters the target (or exit Tunnel) while dragging
        public long first_cur_tgt_entry;
        public long last_cur_tgt_entry;

        // first/last time the object fully enters the target, or zero if never {Box}
        public long first_obj_tgt_entry;
        public long last_objt_tgt_entry;

        // first/last release moment (last only different if start_error)
        public long first_release;
        public long last_release;

        // first/last revert moment (last only different if start_error)
        public long first_revert;
        public long last_revert;

        public void logMove() {
            if (first_move == 0) first_move = Utils.nowMillis();
        }

        public void logCurObjEntry() {
            if (first_cur_obj_entry == 0) {
                first_cur_obj_entry = Utils.nowMillis();
                last_cur_obj_entry = Utils.nowMillis();
            } else {
                last_cur_obj_entry = Utils.nowMillis();
            }
        }

        public void logGrab() {
            if (first_grab == 0) {
                first_grab = Utils.nowMillis();
                last_grab = Utils.nowMillis();
            } else {
                last_grab = Utils.nowMillis();
            }
        }

        public void logCurTempEntry() {
            if (first_cur_temp_entry == 0) {
                first_cur_temp_entry = Utils.nowMillis();
                last_cur_temp_entry = Utils.nowMillis();
            } else {
                last_cur_temp_entry = Utils.nowMillis();
            }
        }

        public void logObjTempEntry() {
            if (first_obj_temp_entry == 0) {
                first_obj_temp_entry = Utils.nowMillis();
                last_obj_temp_entry = Utils.nowMillis();
            } else {
                last_obj_temp_entry = Utils.nowMillis();
            }
        }

        public void logObjTempExit() {
            if (first_obj_temp_exit == 0) {
                first_obj_temp_exit = Utils.nowMillis();
                last_obj_temp_exit = Utils.nowMillis();
            } else {
                last_obj_temp_exit = Utils.nowMillis();
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
            Out.d(NAME, last_objt_tgt_entry);
        }

        public void logRelease() {
            if (first_release == 0) {
                first_release = Utils.nowMillis();
                last_release = Utils.nowMillis();
            } else {
                last_release = Utils.nowMillis();
            }
        }

        public void logRevert() {
            if (first_revert == 0) {
                first_revert = Utils.nowMillis();
                last_revert = Utils.nowMillis();
            } else {
                last_revert = Utils.nowMillis();
            }
        }

        public int getPointTime(String taskType) {
            if (taskType.equals("TunnelTrial")) return -1;
            else return (int) (last_cur_obj_entry - first_move);
        }

        public int getGrabTime(String taskType) {
            if (taskType.equals("TunnelTrial")) return (int) (last_grab - first_move);
            else return (int) (last_grab - last_cur_obj_entry);
        }

        public int getDragTime(String taskType) {
            int result = -1;
            switch (taskType) {
                case "BoxTrial", "TunnelTrial" -> { // For Tunnel, cur_tgt_entry is exit
                    if (last_cur_tgt_entry > 0) result = (int) (last_cur_tgt_entry - last_grab);
                }
                case "BarTrial", "PeekTrial" -> {
                    if (last_objt_tgt_entry > 0) result = (int) (last_objt_tgt_entry - last_grab);
                }
            }

            return result;
        }

        public int getTempEntryTime() {
            if (last_obj_temp_entry > 0) return (int) (last_obj_temp_entry - last_grab);
            else return -1;
        }

        public int getTempToTgtTime() {
            if (last_objt_tgt_entry > 0) return (int) (last_objt_tgt_entry - last_obj_temp_exit);
            else return -1;
        }

        public int getReleaseTime(String taskType) {
            int result = -1;
            switch (taskType) {
                case "BoxTrial", "TunnelTrial" -> { // For Tunnel, cur_tgt_entry is exit
                    if (last_cur_tgt_entry > 0) result = (int) (last_release - last_cur_tgt_entry);
                }
                case "BarTrial", "PeekTrial" -> {
                    Out.d(NAME, last_release, last_objt_tgt_entry);
                    if (last_objt_tgt_entry > 0) result = (int) (last_release - last_objt_tgt_entry);
                }
            }

            return result;
        }

        public int getRevertTime() {
            if (last_obj_temp_entry > 0) return (int) (last_revert - last_obj_temp_entry);
            else return -1;
        }

        public int getTrialTime() {
            if (last_release > 0) return (int) (last_release - last_grab);
            else return (int) (last_revert - last_grab);
        }

        public int getTotalTime() {
            if (last_release > 0) return (int) (last_release - first_move);
            else return (int) (last_revert - first_move);
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
