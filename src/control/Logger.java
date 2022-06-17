package control;

import experiment.Task;
import experiment.Trial;
import panels.MainFrame;
import tools.Utils;

import java.io.*;
import java.nio.file.Files;
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
                mTrialsFilePW.println(GeneralInfo.getLogHeader() + SP + TrialInfo.getLogHeader());
            }

            if (Utils.isFileEmpty(instantsLogFile)) {
                mInstantFilePW.println(GeneralInfo.getLogHeader() + SP + InstantInfo.getLogHeader());
            }

            if (Utils.isFileEmpty(timesLogFile)) {
                mTimesFilePW.println(GeneralInfo.getLogHeader() + SP + TimeInfo.getLogHeader());
            }

        } catch (IOException e) {
            e.printStackTrace();
//            Main.showDialog("Problem in logging the participant!");
        }
    }


    // Log infos (each row = one trial) --------------------------------------------------------------------

    // General info regarding every trial
    public static class GeneralInfo {
        public Task task;
        public TECHNIQUE technique;
        public int blockNum;
        public int trialNum;
        public Trial trial;

//        public static String getLogHeader() {
//            return "technique" + SP +
//                    "block_num" + SP +
//                    "trial_num" + SP +
//                    Trial.getLogHeader();
//        }

        public static String getLogHeader() {
            return "task" + SP +
                    "technique" + SP +
                    "block_num" + SP +
                    "trial_num";
        }

//        @Override
//        public String toString() {
//            return blockNum + SP +
//                    trialNum + SP +
//                    trial.toLogString();
//        }

        @Override
        public String toString() {
            return task + SP +
                    technique + SP +
                    blockNum + SP +
                    trialNum;
        }
    }

    // Main trial info (all times in ms)
    public static class TrialInfo {
        public int searchTime;      // From the first scroll until the last appearance of the target
        public int fineTuneTime;    // From the last appearance of target to the last scroll
        public int scrollTime;      // SearchTime + fineTuneTime (first scroll -> last scroll)
        public int trialTime;       // First scroll -> SPACE
        public int finishTime;      // Last scroll -> SPACE
        public int nTargetAppear;   // Number of target appearances
        public int vtResult;        // Vertical: 1 (Hit) or 0 (Miss)
        public int hzResult;        // Horizontal: 1 (Hit) or 0 (Miss)
        public int result;          //1 (Hit) or 0 (Miss)

        public static String getLogHeader() {
            return "search_time" + SP +
                    "fine_tune_time" + SP +
                    "scroll_time" + SP +
                    "trial_time" + SP +
                    "finish_time" + SP +
                    "n_target_appear" + SP +
                    "vt_result" + SP +
                    "hz_result" + SP +
                    "result";
        }

        @Override
        public String toString() {
            return searchTime + SP +
                    fineTuneTime + SP +
                    scrollTime + SP +
                    trialTime + SP +
                    finishTime + SP +
                    nTargetAppear + SP +
                    vtResult + SP +
                    hzResult + SP +
                    result;
        }
    }

    // Instants of events in a trial (all times in system timestamp (ms))
    public static class InstantInfo {
        public long trialShow;
        public long firstEntry;
        public long lastEntry;
        public long firstScroll;
        public long lastScroll;
        public long targetFirstAppear;
        public long targetLastAppear;
        public long trialEnd;

        public static String getLogHeader() {
            return "trial_show" + SP +
                    "first_entry" + SP +
                    "last_entry" + SP +
                    "first_scroll" + SP +
                    "last_scroll" + SP +
                    "target_first_appear" + SP +
                    "target_last_appear" + SP +
                    "trial_end";
        }

        @Override
        public String toString() {
            return trialShow + SP +
                    firstEntry+ SP +
                    lastEntry + SP +
                    firstScroll + SP +
                    lastScroll + SP +
                    targetFirstAppear + SP +
                    targetLastAppear + SP +
                    trialEnd;
        }
    }

    // Time info (not instnats)
    public static class TimeInfo {
        public long trialTime; // In millisec
        public long blockTime; // In millisec
//        public int techTaskTime; // Each tech|task (In sec)
//        public long homingTime; // In millisec
//        public int techTime; // In sec
//        public int experimentTime; // In sec

        public static String getLogHeader() {
            return "trial_time" + SP +
                    "block_time" + SP +
                    "tech_task_time" + SP +
                    "homing_time" + SP +
                    "tech_time" + SP +
                    "experiment_time";
        }

//        @Override
//        public String toString() {
//            return trialTime + SP +
//                    blockTime+ SP +
//                    techTaskTime + SP +
//                    homingTime + SP +
//                    techTime + SP +
//                    experimentTime;
//        }

        @Override
        public String toString() {
            return trialTime + SP +
                    blockTime;
        }
    }


}
