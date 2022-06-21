package panels;

import control.Logger;
import experiment.BoxTrial;
import graphic.MoGraphics;
import log.ActionLog;
import log.TrialLog;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static tools.Consts.*;
import static experiment.Experiment.*;

import static tools.Consts.COLORS;

public class BoxTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BoxTaskPanel/";
    
    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial

    // Experiment
    private BoxTrial mTrial;

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
    private Dimension mDim;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // Entry
    private boolean mCursorInObject, mCursorInTarget, mObjInTarget;

    // Actions ------------------------------------------------------------------------------------
//    private final Action NEXT_TRIAL = new AbstractAction() {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            hit();
//        }
//    };

    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public BoxTaskPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);

        addMouseMotionListener(this);
        addMouseListener(this);
        
        mTaskType = TASK.BOX;

        // Key maps
//        mapKeys();
//        getActionMap().put(KeyEvent.VK_SPACE, NEXT_TRIAL);
    }

    /**
     * Set the task
     * @param boxTask BoxTask
     * @return Self instance
     */
    public BoxTaskPanel setTask(BoxTask boxTask) {
        mTask = boxTask;
        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";
        super.showTrial(trNum);

        // Reset flags
        mCursorInObject = false;
        mCursorInTarget = false;
        mObjInTarget = false;

        mTrial = (BoxTrial) mBlock.getTrial(trNum);

        //region LOG
        mTrialLog.trial = mTrial.clone();
        //endregion

        repaint();
        mTrialActive = true;
    }

    @Override
    protected void move() {
        final String TAG = NAME + "move";
        super.move();

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.MOVE, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mTrial.objectRect.contains(curP)) {

            if (!mCursorInObject) { // Entry (only after exit)
                //region LOG
                mInstantLog.logCurObjEntry();
                mTrialLog.point_time = mInstantLog.getPointTime();
                //endregion

                mCursorInObject = true;
            }
        } else {
            mCursorInObject = false;
        }

    }

    @Override
    public void grab() {
        super.grab();   // grab Instant logging done in the superclass

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.GRAB, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mCursorInObject) {
            mGrabbed = true;
            mGrabPos = curP;

            //region LOG
            mTrialLog.grab_time = mInstantLog.getGrabTime();
            mTrialLog.logGrabPoint(curP);
            //endregion

        } else { // Grab outside the object
            startError();
        }
    }

    @Override
    protected void drag() {
        super.drag();

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.DRAG, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mTrial.targetRect.contains(curP)) {
            if (!mCursorInTarget) { // Entry
                //region LOG
                mInstantLog.logCurTgtEntry();
                mTrialLog.drag_time = mInstantLog.getDragTime(mTaskType);
                //endregion

                mCursorInTarget = true;
            }
        } else {
            mCursorInTarget = false;
        }

        if (mTrial.targetRect.contains(mTrial.objectRect)) {
            if (!mObjInTarget) { // Object entry
                //region LOG
                mInstantLog.logObjTgtEntry();
                mTrialLog.drag_time = mInstantLog.getDragTime(mTaskType);
                //endregion

                mObjInTarget = true;
            }
        } else {
            mObjInTarget = false;
        }

        final int dX = curP.x - mGrabPos.x;
        final int dY = curP.y - mGrabPos.y;

        mTrial.objectRect.translate(dX, dY);

        mGrabPos = curP;

        repaint();
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        super.release(); // always logs release

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.RELEASE, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mGrabbed) {
            //region LOG
            mTrialLog.logReleasePoint(curP);

            mTrialLog.release_time = mInstantLog.getReleaseTime(mTaskType);
            mTrialLog.revert_time = mInstantLog.getRevertTime();
            mTrialLog.trial_time = mInstantLog.getTrialTime();
            mTrialLog.total_time = mInstantLog.getTotalTime();
            //endregion

            final boolean trialResult = checkHit();
            mTrialLog.result = Utils.bool2Int(trialResult); // LOG

            if (trialResult) hit();
            else miss();
        }

        mGrabbed = false;
    }

    @Override
    protected void revert() {
        super.revert(); // always logs revert

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.REVERT, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mGrabbed) {
            //region LOG
            mTrialLog.logRevertPoint(curP);

            mTrialLog.release_time = mInstantLog.getReleaseTime(mTaskType);
            mTrialLog.revert_time = mInstantLog.getRevertTime();
            mTrialLog.trial_time = mInstantLog.getTrialTime();
            mTrialLog.total_time = mInstantLog.getTotalTime();
            //endregion

            miss();
        } else {
            startError();
        }
    }

    @Override
    protected void hit() {
        final String TAG = NAME + "hit";

        moveObjInside();

        super.hit();
    }

    @Override
    public boolean checkHit() {
        return mTrial.targetRect.contains(getCursorPos());
    }

    public void moveObjInside() {
        if (mTrial != null) {
            final Rectangle intersection = mTrial.targetRect.intersection(mTrial.objectRect);

            final int dMinX = (int) (intersection.getMinX() - mTrial.objectRect.getMinX());
            final int dMaxX = (int) (intersection.getMaxX() - mTrial.objectRect.getMaxX());

            final int dMinY = (int) (intersection.getMinY() - mTrial.objectRect.getMinY());
            final int dMaxY = (int) (intersection.getMaxY() - mTrial.objectRect.getMaxY());

            mTrial.objectRect.translate(dMinX + dMaxX, dMinY + dMaxY);

            repaint();
        }
    }


    // -------------------------------------------------------------------------------------------
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        mMoGraphics = new MoGraphics(g2d);

        // Draw the target
        mMoGraphics.fillRectangle(COLORS.GRAY_400, mTrial.targetRect);

        // Draw the object
        mMoGraphics.fillRectangle(COLORS.BLUE_900_ALPHA, mTrial.objectRect);

        // Draw block-trial num (on practice show active technique)
        if (!mPracticeMode) {
            String stateText =
                    STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " — " +
                            STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(60), Utils.mm2px(12));
        } else {
            String stateText = MainFrame.get().mActiveTechnique.getTitle();
            mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(60), Utils.mm2px(12));
        }

        // TEMP: draw bound rect
//        mMoGraphics.drawRectangle(COLORS.GRAY_500, mTrial.getBoundRect());
    }

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }

    // -------------------------------------------------------------------------------------------
    @Override
    public void mouseDragged(MouseEvent e) {
        if (mMouseEnabled) {
            if (mTrialActive && mGrabbed) {
                drag();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mTrialActive) {
            if (mGrabbed) drag();
            else move();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mMouseEnabled) {
            if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
                grab();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mMouseEnabled) {
            if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
                release();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }



}
