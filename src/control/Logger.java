package control;

import experiment.Experiment;
import experiment.Trial;
import log.*;
import panels.MainFrame;
import tools.Memo;
import tools.Out;
import tools.Utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static tools.Consts.STRINGS.*;

public class Logger {
    private final static String NAME = "Logger/";

    private static Logger self;

    private final String TOP_LOGS_DIR = "/Users/mahmoud/Documents/Academics/PhD/MIDE/Logs";
    private static final String DRAG_LOG_DIR_NAME = "Drag";

    private String mPcLogId;
    private String mPcDateId;
    private String mPracticePcDateId;

    private static Path mLogDirectory; // Main folder for logs
    private static Path mPcLogDirectory; // Folder log path of the participant

    // Different log files
    private PrintWriter mTrialLogFilePW;
    private PrintWriter mInstantLogFilePW;
    private PrintWriter mTimeLogFilePW;
    private PrintWriter mActionLogFilePW;

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

        // Create a folder for the participant (if not already created)
        mPcLogDirectory = mLogDirectory.resolve(mPcLogId);
        Utils.createDirIfNotExisted(mPcLogDirectory);

        // Create/open log files
        openLogFilesToWrite();

    }

    public String getPId() {
        return mPcDateId;
    }

    public String getPracticePId() {
        return mPracticePcDateId;
    }

    /**
     * Log TrialInfo
     * @param genLog GeneralInfo
     * @param trialLog TrialInfo
     */
    public void logTrial(GeneralLog genLog, TrialLog trialLog) {
        final String TAG = NAME + "logTrialInfo";

        try {
            // Open logs if not opened
            if (mTrialLogFilePW == null) openLogFilesToWrite();

            mTrialLogFilePW.println(genLog + SP + trialLog);

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging trial!");
            e.printStackTrace();
        }
    }

    /**
     * Log InstantLog
     * @param genLog GeneralInfo
     * @param instLog InstantLog
     */
    public void logInstant(GeneralLog genLog, InstantLog instLog) {
        final String TAG = NAME + "logInstant";

        try {
            // Open logs if not opened
            if (mInstantLogFilePW == null) openLogFilesToWrite();

            mInstantLogFilePW.println(genLog + SP + instLog);

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging instant!");
        }
    }

    /**
     * Log TimeLog
     * @param genLog GeneralLog
     * @param timeLog TimeLog
     */
    public void logTime(GeneralLog genLog, TimeLog timeLog) {
        final String TAG = NAME + "logInstant";

        try {
            // Open logs if not opened
            if (mTimeLogFilePW == null) openLogFilesToWrite();

            mTimeLogFilePW.println(genLog + SP + timeLog);

        } catch (NullPointerException e) {
//            Main.showDialog("Problem in logging time!");
        }
    }

    /**
     * Log ActionLog
     * @param genLog GeneralLog
     * @param actionLog ActionLog
     */
    public void logAction(GeneralLog genLog, ActionLog actionLog) {
        final String TAG = NAME + "logInstant";

        try {
            // Open logs if not opened
            if (mActionLogFilePW == null) openLogFilesToWrite();

            mActionLogFilePW.println(genLog + SP + actionLog);

        } catch (NullPointerException e) {
            MainFrame.get().showMessage("Problem in logging Action!");
        }
    }

    /**
     * Close all log files
     */
    public void closeLogs() {
        if (mTrialLogFilePW != null) mTrialLogFilePW.close();
        if (mInstantLogFilePW != null) mInstantLogFilePW.close();
        if (mTimeLogFilePW != null) mTimeLogFilePW.close();
    }

    /**
     * Open the log files for writing (if not already opened)
     */
    private void openLogFilesToWrite() {

        mPcDateId = mPcLogId + "_" + Utils.nowDate();

        // Log files for the participant
        final File trialsLogFile = mPcLogDirectory.resolve(mPcDateId + "_" + "TRIALS.txt").toFile();
        final File instantsLogFile = mPcLogDirectory.resolve(mPcDateId + "_" + "INSTANTS.txt").toFile();
        final File timesLogFile = mPcLogDirectory.resolve(mPcDateId + "_" + "TIMES.txt").toFile();
        final File actionsLogFile = mPcLogDirectory.resolve(mPcDateId + "_" + "ACTIONS.txt").toFile();

        // Create files (w/ autoflash) and if not existed, write headers. Append is for later writings
        try {
            mTrialLogFilePW = new PrintWriter(
                    new FileOutputStream(trialsLogFile, true),
                    true);

            mInstantLogFilePW = new PrintWriter(
                    new FileOutputStream(instantsLogFile, true),
                    true);

            mTimeLogFilePW = new PrintWriter(
                    new FileOutputStream(timesLogFile, true),
                    true);

            mActionLogFilePW = new PrintWriter(
                    new FileOutputStream(actionsLogFile, true),
                    true);

            //-- Write headers (only the first time)
            if (Utils.isFileEmpty(trialsLogFile)) {
                // Custom header because of the Trial part
                mTrialLogFilePW.println(
                        Utils.classPropsNames(GeneralLog.class) + SP +
                        Trial.getLogHeader() + SP +
                        Utils.classPropsNames(TrialLog.class).replace("trial;", ""));
            }

            if (Utils.isFileEmpty(instantsLogFile)) {
                mInstantLogFilePW.println(getLogHeaders(GeneralLog.class, InstantLog.class));
            }

            if (Utils.isFileEmpty(timesLogFile)) {
                mTimeLogFilePW.println(getLogHeaders(GeneralLog.class, TimeLog.class));
            }

            if (Utils.isFileEmpty(actionsLogFile)) {
                mActionLogFilePW.println(getLogHeaders(GeneralLog.class, ActionLog.class));
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

}
