package panels;

import com.google.gson.Gson;
import log.Logger;
import control.Server;
import dialogs.PracticeBreakDialog;
import experiment.Block;
import experiment.Experiment;
import experiment.Task;
import dialogs.BreakDialog;
import graphic.MoGraphics;
import graphic.MoRectangle;
import log.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static tools.Consts.*;

public class TaskPanel extends JLayeredPane {
    private final String NAME = "TaskPanel/";

    // Constants
    protected double TB_MARGIN_mm = 20;
    protected double LR_MARGIN_mm = 20;
    protected int MAX_CEHCK_POS = 100;

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Experiment
    protected Task mTask;
    protected Block mBlock;
    protected int mBlockNum, mTrialNum;

    protected Experiment.TASK mTaskType;

    protected boolean mPracticeMode = false;
    protected boolean mDemoMode = false;

    // Flags
    protected boolean mTrialActive = false;
    protected boolean mMouseEnabled = false;
    protected boolean mGrabbed = false;

    // Counters
    protected int mPosCount = 0;

    // Helpers
    protected MoGraphics mMoGraphics;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // Logging
    protected GeneralLog mGenLog;
    protected TrialLog mTrialLog;
    protected InstantLog mInstantLog;
    protected TimeLog mTimeLog;

    protected long mTrialStartTime;
    protected long mBlockStartTime;
    protected long mTaskStartTime;

    protected Gson mGson = new Gson();

    // Actions ------------------------------------------------------------------------------------
    protected final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hit();
        }
    };

    // Methods ------------------------------------------------------------------------------------
    protected void start() {

        mGenLog = new GeneralLog();
        mTimeLog = new TimeLog();

        mBlockNum = 1;
        mTrialNum = 1;

        //region LOG
        mGenLog.task = mTaskType;
        mGenLog.technique = MainFrame.get().mActiveTechnique;
        mGenLog.block_num = mBlockNum;
        mGenLog.trial_num = mTrialNum;

        mTaskStartTime = Utils.nowMillis();
        //endregion

        startBlock(mBlockNum);

        mapKeys();
//        getActionMap().put(KeyEvent.VK_SPACE, NEXT_TRIAL);
    }

    protected void startBlock(int blkNum) {
        final String TAG = NAME + "startBlock";

        mBlock = mTask.getBlock(blkNum);

        // Try to find positions for all the trials in the block
        if (findAllTrialsPosition(1) == 0) {
            mBlock.positionAllTrialsElements();
            Out.d(TAG, "Showing the trials");
            mBlockStartTime = Utils.nowMillis();
            showTrial(1);
        } else {
            Out.e(TAG, "Couldn't find positions for the trials in the block!");
        }
    }

    protected void showTrial(int trNum) {
        //region LOG
        mInstantLog = new InstantLog();
        mTrialLog = new TrialLog();

        mInstantLog.logTrialShow();

        mTrialStartTime = Utils.nowMillis();
        //endregion
    }

    protected boolean checkHit() {
        return false;
    }

    protected void move() {
        //region LOG
        mInstantLog.logMove();

        final long homingStart = MainFrame.get().getmHomingStartTime();
        if (homingStart != 0) {
            mTimeLog.logHomingTime((int) (Utils.nowMillis() - homingStart));
            MainFrame.get().resetHomingStartTime();
        }
        //endregion
    }

    protected void grab() {
        mInstantLog.logGrab(); // LOG
    }

    protected void drag() {
        //region LOG
        mInstantLog.logDragStart();
        mTrialLog.grab_to_drag_time = mInstantLog.getGrabToDragTime();
        //endregion
    }

    protected void release() {
        mInstantLog.logRelease(); // LOG
    }

    protected void revert() {
        mInstantLog.logRevert(); // LOG
    }

    protected void startError() {
        final String TAG = NAME + "startError";

        Consts.SOUNDS.playStartError();
    }

    protected void hit() {
        final String TAG = NAME + "hit";

        Consts.SOUNDS.playHit();

        mTrialLog.result = 1; // LOG

        mTrialActive = false;
        mGrabbed = false;

        logTrialEnd(); // LOG

        // Next...
        next();

    }

    protected void next() {
        if (mTrialNum < mBlock.getNumTrials()) { // Trial -------------------------------------
            mTrialNum++;

            //region LOG
            mGenLog.trial_num = mTrialNum;
            //endregion

            executorService.schedule(() ->
                            showTrial(mTrialNum),
                    mTask.NT_DELAY_ms,
                    TimeUnit.MILLISECONDS);

        } else if (mBlockNum < mTask.getNumBlocks()) { // Block -------------------------------

            // Break dialog
            if (mPracticeMode) {
                // Show dialog after each break
                MainFrame.get().showDialog(new PracticeBreakDialog());
            } else if (mDemoMode) {
                // Just continue with the blocks
            } else { // Real experiment -> show break dialog
                if (mBlockNum == 3) {
                    MainFrame.get().showDialog(new BreakDialog());
                }
            }

            logBlockEnd(); // LOG

            // Next block
            mBlockNum++;
            mTrialNum = 1;

            // LOG
            mGenLog.block_num = mBlockNum;
            mGenLog.trial_num = mTrialNum;
            //---

            executorService.schedule(() ->
                            startBlock(mBlockNum),
                    mTask.NT_DELAY_ms,
                    TimeUnit.MILLISECONDS);
        } else { // Task is finished -----------------------------------------------------------

            // LOG
            logBlockEnd();
            logTaskEnd();
            //---

            MainFrame.get().showEndPanel();

            SOUNDS.playTaskEnd();
        }
    }

    protected void miss() {
        final String TAG = NAME + "miss";

        Consts.SOUNDS.playMiss();

        mTrialLog.result = 0; // LOG

        mTrialActive = false;
        mGrabbed = false;

        logTrialEnd(); // LOG

        // Shuffle back and reposition the next ones
        final  int trNewInd = mBlock.dupeShuffleTrial(mTrialNum);
        Out.e(TAG, "TrialNum | Insert Ind | Total", mTrialNum, trNewInd, mBlock.getNumTrials());
        if (findAllTrialsPosition(trNewInd) == 1) {
            MainFrame.get().showMessage("No positions for trial at " + trNewInd);
        } else { // Next trial

            mTrialNum++;

            mGenLog.trial_num = mTrialNum; // LOG

            executorService.schedule(() -> {
                showTrial(mTrialNum);}, mTask.NT_DELAY_ms,
                    TimeUnit.MILLISECONDS);
        }

    }

    protected void sendGenLog() {
        if (!mPracticeMode && !mDemoMode) {
            final String genLogJSON = mGson.toJson(mGenLog, GeneralLog.class);
            Server.get().send(new Memo(STRINGS.LOG, STRINGS.GENLOG, genLogJSON));
            Out.d(NAME, genLogJSON);
        }
    }

    /**
     * Log wrap everything by the end of the trial
     */
    protected void logTrialEnd() {
        mTrialLog.release_time = mInstantLog.getReleaseTime(mTaskType);
        mTrialLog.revert_time = mInstantLog.getRevertTime();

        mTrialLog.trial_time = mInstantLog.getTrialTime();
        mTrialLog.total_time = mInstantLog.getTotalTime();

        mTimeLog.trial_time = (int) (Utils.nowMillis() - mTrialStartTime);

        Logger.get().logInstant(mGenLog, mInstantLog);
        Logger.get().logTrial(mGenLog, mTrialLog);
        Logger.get().logTime(mGenLog, mTimeLog);

        mTimeLog.trial_time = 0;
        mTimeLog.homing_time = 0;
    }

    protected void logBlockEnd() {
        mTimeLog.block_time = (int) (Utils.nowMillis() - mBlockStartTime);
        Logger.get().logTime(mGenLog, mTimeLog);

        mTimeLog.block_time = 0;
    }

    protected void logTaskEnd() {
        mTimeLog.task_time = (int) (Utils.nowMillis() - mTaskStartTime);
        Logger.get().logTime(mGenLog, mTimeLog);

        mTimeLog.task_time = 0;
    }

    protected Dimension getDispDim() {
        Dimension result = new Dimension();
        result.width = getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
        result.height = getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));

        return result;
    }

    protected int getDispW() {
        return getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
    }

    protected int getDispH() {
        return getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));
    }

    protected Area getDispArea() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);
        return new Area(new Rectangle(
                0, 0,
                getWidth() - 2 * lrMargin, getHeight() - 2 * tbMargin));
    }

    protected MinMax getWidthMinMax() {
        final int hzMargin = Utils.mm2px(LR_MARGIN_mm);
        return new MinMax(hzMargin, getWidth() - hzMargin);
    }

    protected MinMax getHeightMinMax() {
        final int vtMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MinMax(vtMargin, getHeight() - vtMargin);
    }

    protected MoRectangle getPanelRect() {
        final int wMargin = Utils.mm2px(LR_MARGIN_mm);
        final int hMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MoRectangle(
                wMargin, hMargin,
                getWidth() - 2 * wMargin,
                getHeight() - 2 * hMargin);
    }

    protected void setMouseEnabled(boolean enabled) {
        mMouseEnabled = enabled;
    }

    protected void setPracticeMode(boolean prMode) {
        mPracticeMode = prMode;
    }

    protected void setDemoMode(boolean dmMode) {
        mDemoMode = dmMode;
    }

    protected Point findPosition(MoRectangle rect) {
        final int wMargin = Utils.mm2px(LR_MARGIN_mm);
        final int hMargin = Utils.mm2px(TB_MARGIN_mm);

        if (rect.height > getDispH() || rect.width > getDispW()) return null;
        else { return new Point(
                    Utils.randInt(wMargin, getDispW() - rect.width),
                    Utils.randInt(hMargin, getDispH() - rect.height));
        }
    }

    /**
     * Get the cursor position relative to the panel
     * @return Point
     */
    protected Point getCursorPos() {
        Point result = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(result, this);

        return  result;
    }

    /**
     * Recursively find suitable positions for a list of trials, from (incl.) startTrNum
     * @param startTrNum Index of the first trial. If > 1  => prev. Trial restricts, otherwise, free
     * @return Success (0) Fail (1)
     */
    public int findAllTrialsPosition(int startTrNum) {
        final String TAG = NAME + "findAllTrialsPosition";

        final int minNtDist = Utils.mm2px(Experiment.BoxTask.NT_DIST_mm);
//        final int maxNtDist = minNtDist + Utils.mm2px(50);

        if (startTrNum == mBlock.getNumTrials() + 1) return 0;

        Point foundPosition = null;
        Point prevEndPoint = null;
//        MoRectangle prevBoundRect = null;
        if (startTrNum > 1) {
            prevEndPoint = mBlock.getTrial(startTrNum - 1).getEndPoint();
//            prevBoundRect = mBlock.getTrial(startTrNum - 1).getBoundRect();
        }

        // Search position for the start trial
//        foundPosition = findTrialPosition(
//                mBlock.getTrial(startTrNum).getBoundRect(),
//                prevEndPoint,
//                minNtDist, maxNtDist);
//        Out.d(TAG, "startTrNum", startTrNum);
        foundPosition = findPosition(
                mBlock.getTrial(startTrNum).getBoundRect(),
                prevEndPoint,
                minNtDist);

        if (foundPosition != null) { // Position found => set the position and move on to the rest
            mBlock.setTrialLocation(startTrNum, foundPosition);
            return findAllTrialsPosition(startTrNum + 1);
        } else {
//            Out.d(TAG, "Starting again", mPosCount);
            if (mPosCount < MAX_CEHCK_POS) {
                mPosCount++;
                return findAllTrialsPosition(startTrNum);
            }
        }

        return 1;
    }

    public Point findPosition(MoRectangle ntBoundRect, Point ctEndPoint, int minNtDist) {
        final String TAG = NAME + "findPosition";

        MoRectangle dispRect = getPanelRect();

        if (ctEndPoint == null) {
            return findPosition(ntBoundRect);
        } else {
            // Create margined rectangle
//            MoRectangle ctMarginRect = ctBoundRect.getMarginedRectangel(minNtDist);
//            MoRectangle ctMarginRect = new MoRectangle(
//                    ctBoundRect.getCenter().x,
//                    ctBoundRect.getCenter().y,
//                    minNtDist);
            MoRectangle ctMarginRect = new MoRectangle(ctEndPoint, minNtDist);
            MoRectangle ntMarginRect = ntBoundRect.getMarginedRectangel(0);
//            Out.d(TAG, ctMarginRect, ntMarginRect);
            List<DIRECTION> dirs = Arrays.asList(DIRECTION.N, DIRECTION.S, DIRECTION.E, DIRECTION.W);
            Collections.shuffle(dirs);

            // Check each direction
            for (DIRECTION dir : dirs) {
                switch (dir) {
                    case N -> {
//                        Out.d(TAG, "Checking N", ctMarginRect.y - ntMarginRect.height);
                        if (ctMarginRect.y - ntMarginRect.height > dispRect.minY) {
                            ntMarginRect.y = ctMarginRect.y - ntMarginRect.height;
                            ntMarginRect.x = Utils.randInt(
                                    max(dispRect.minX, ctMarginRect.minX - ntMarginRect.width),
                                    min(dispRect.maxX - ntMarginRect.width, ctMarginRect.maxX));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case S -> {
//                        Out.d(TAG, "Checking S", ctMarginRect.maxY + ntMarginRect.height);
                        if (ctMarginRect.maxY + ntMarginRect.height < dispRect.maxY) {
                            ntMarginRect.y = ctMarginRect.maxY;
                            ntMarginRect.x = Utils.randInt(
                                    max(dispRect.minX, ctMarginRect.minX - ntMarginRect.width),
                                    min(dispRect.maxX - ntMarginRect.width, ctMarginRect.maxX));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case E -> {
//                        Out.d(TAG, "Checking E", ctMarginRect.maxX + ntMarginRect.width);
                        if (ctMarginRect.maxX + ntMarginRect.width < dispRect.maxX) {
                            ntMarginRect.x = ctMarginRect.maxX;
                            ntMarginRect.y = Utils.randInt(
                                    max(dispRect.minY, ctMarginRect.minY - ntMarginRect.height),
                                    min(dispRect.maxY - ntMarginRect.height, ctMarginRect.maxY));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case W -> {
//                        Out.d(TAG, "Checking W", ctMarginRect.minX - ntMarginRect.width);
                        if (ctMarginRect.minX - ntMarginRect.width > dispRect.minX) {
                            ntMarginRect.x = ctMarginRect.minX - ntMarginRect.width;
                            ntMarginRect.y = Utils.randInt(
                                    max(dispRect.minY, ctMarginRect.minY - ntMarginRect.height),
                                    min(dispRect.maxY - ntMarginRect.height, ctMarginRect.maxY));

                            return ntMarginRect.getLocation();
                        }
                    }
                }
            }

        }


        return null;
    }

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }
}
