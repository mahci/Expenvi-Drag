package panels;

import control.Logger;
import experiment.BarTrial;
import graphic.MoGraphics;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class BarTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BarTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Experiment
    private BarTrial mTrial;

    // Config
    private final boolean mChangeCursor = false;
    private final boolean mHighlightObj = true;

    // Flags
    private boolean mIsCursorNearObj = false;

    // Shapes
    private Point mGrabPos = new Point();

    // Executor
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // Entry
    private boolean mCursorInObject, mCursorInTarget, mObjInTarget;

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hit();
        }
    };

    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public BarTaskPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);

        addMouseMotionListener(this);
        addMouseListener(this);

        // Key maps
        mapKeys();
        getActionMap().put(KeyEvent.VK_SPACE, NEXT_TRIAL);
    }

    /**
     * Set the task
     * @param barTask BarTask
     * @return Self instance
     */
    public BarTaskPanel setTask(BarTask barTask) {
        mTask = barTask;
        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";
        super.showTrial(trNum);

        mTrial = (BarTrial) mBlock.getTrial(trNum);

        // LOG
        mTrialInfo = new Logger.TrialInfo();
        mTrialInfo.trial = mTrial.clone();

        repaint();
        mTrialActive = true;
    }

    @Override
    protected void move() {
        final String TAG = NAME + "move";
        super.move(); // Log move is done in the super

        Point curP = getCursorPos();

        if (mTrial.objectRect.contains(curP)) {

            if (!mCursorInObject) { // Entry (only after exit)
                //region LOG
                mInstantInfo.logCurObjEntry();
                mTrialInfo.point_time = mInstantInfo.getPointTime(mTrial.getClass().getSimpleName());
                //endregion

                mCursorInObject = true;
            }
        } else {
            mCursorInObject = false;
        }
    }

    @Override
    public void grab() {
        final String TAG = NAME + "grab";
        super.grab();

        Point curP = getCursorPos();
        if (mTrial.objectRect.contains(curP)) {
            mGrabbed = true;
            mGrabPos = curP;

            //region LOG
            mTrialInfo.grab_time = mInstantInfo.getGrabTime(mTrial.getClass().getSimpleName());
            mTrialInfo.grab_x = curP.x;
            mTrialInfo.grab_y = curP.y;
            //endregion

        } else {
            startError();
        }
    }

    @Override
    protected void drag() {
        super.drag();

        final Point curP = getCursorPos();

        if (mTrial.targetRect.contains(curP)) {
            if (!mCursorInTarget) { // Entry
                mInstantInfo.logCurTgtEntry(); // LOG
                mCursorInTarget = true;
            }
        } else {
            mCursorInTarget = false;
        }

        if (mTrial.targetRect.contains(mTrial.objectRect)) {
            if (!mObjInTarget) { // Object entry
                mInstantInfo.logObjTgtEntry(); // LOG
                mTrialInfo.drag_time = mInstantInfo.getDragTime(mTrial.getClass().getSimpleName()); // LOG

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
        super.release();

        if (mGrabbed) {
            //region LOG
            mTrialInfo.release_time = mInstantInfo.getReleaseTime(mTrial.getClass().getSimpleName());

            mTrialInfo.release_x = getCursorPos().x;
            mTrialInfo.release_y = getCursorPos().y;

            mTrialInfo.trial_time = mInstantInfo.getTrialTime();
            mTrialInfo.total_time = mInstantInfo.getTotalTime();
            //endregion

            final boolean trialResult = checkHit();
            mTrialInfo.result = Utils.bool2Int(trialResult); // LOG

            if (trialResult) hit();
            else miss();
        }

        mGrabbed = false;
    }

    @Override
    protected void revert() {
        super.revert();

        if (mCursorInObject) startError();

        if (mGrabbed) {
            //region LOG
            mTrialInfo.revert_x = getCursorPos().x;
            mTrialInfo.revert_y = getCursorPos().y;

            mTrialInfo.trial_time = mInstantInfo.getTrialTime();
            mTrialInfo.total_time = mInstantInfo.getTotalTime();
            //endregion

            miss();
        }
    }

    @Override
    public boolean checkHit() {
        return mTrial.targetRect.contains(mTrial.objectRect);
    }

    // -------------------------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        mMoGraphics = new MoGraphics(g2d);

        if (mTrial != null) {

            // Draw the target
            mMoGraphics.fillRectangle(COLORS.GRAY_500, mTrial.targetRect);
//        mMoGraphics.fillRectangle(COLORS.GRAY_900, mTrial.line1Rect);
//        mMoGraphics.fillRectangle(COLORS.GRAY_900, mTrial.line2Rect);

            // Draw the object
            mMoGraphics.fillRectangle(COLORS.BLUE_900, mTrial.objectRect);

            // Draw block-trial num (on practice -> technique)
            if (!mPracticeMode) {
                String stateText =
                        STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " — " +
                                STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
                mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                        getWidth() - Utils.mm2px(80), Utils.mm2px(12));
            } else {
                String stateText = MainFrame.get().mActiveTechnique.getTitle();
                mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                        getWidth() - Utils.mm2px(60), Utils.mm2px(12));
            }

            // TEMP: draw bounding box
//            mMoGraphics.drawRectangle(COLORS.GRAY_400, mTrial.getBoundRect());
        }

    }

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }

    // -------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mMouseEnabled) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                grab();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mMouseEnabled) {
            if (e.getButton() == MouseEvent.BUTTON1) {
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


}
