package panels;

import control.Logger;
import experiment.PeekTrial;
import graphic.MoGraphics;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class PeekTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "PeekTaskPanel/";

    // Constants
    private final int DRAG_TICK = 5; // millisecs

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Experiment
    private PeekTrial mTrial;

    // Trial
//    private boolean mTempEntered = false;
    private boolean mDragging = false;
    private boolean mCurtainClosed = false;
    private boolean mPastTempRect = false;
    private Point mLastGrabPos = new Point();
    private Point mRelGrabPos = new Point();
    private boolean mChangeCursor = true;
    private boolean mHightlightObj = false;

    // Colors
    private Color COLOR_OBJECT = COLORS.BLUE_900;
    private Color COLOR_OBJ_HIGHLIGHT = COLORS.YELLOW_900;
    private Color COLOR_OBJ_DEFAULT = COLORS.BLUE_900;
    private Color COLOR_TARGET_RECT = COLORS.GRAY_500;
    private Color COLOR_TEMP_RECT = COLORS.GREEN_700;
    private Color COLOR_CURTAIN = COLORS.ORANGE_200;
    private Color COLOR_TEXT = COLORS.GRAY_900;

    // Threading
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Timer mMoveSampler;

    private int mEventCounter = 0;
    private long mGrabTime;
    private Set<Point> mPointSet = new HashSet<>();

    // Entry
    private boolean mCursorInObject, mCursorInTemp, mCursorInTarget, mObjInTemp, mObjInTarget;

    // Actions ------------------------------------------------------------------------------------
    private ActionListener mMoveListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (mDragging) {
                drag();
            }
        }

    };

    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public PeekTaskPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);

        addMouseMotionListener(this);
        addMouseListener(this);
    }

    /**
     * Set the task
     * @param peekTrial PeekTrial
     * @return Self instance
     */
    public PeekTaskPanel setTask(PeekTask peekTrial) {
        mTask = peekTrial;
        mMoveSampler = new Timer(0, mMoveListener);
        mMoveSampler.setDelay(DRAG_TICK);
        Out.d(NAME, "1 mm to px = ", Utils.mm2px(1));
        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";
        super.showTrial(trNum);

        mTrial = (PeekTrial) mBlock.getTrial(trNum);

        //region LOG
        mTrialInfo = new Logger.TrialInfo();
        mTrialInfo.trial = mTrial.clone();
        //endregion

        repaint();

        mTrialActive = true;
    }

    @Override
    protected void move() {
        final String TAG = NAME + "move";
        super.move();

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

        enableObjHint(mCursorInObject);
    }

    @Override
    public void grab() {
        final String TAG = NAME + "grab";
        super.grab();

        Point curP = getCursorPos();
        if (mCursorInObject) {
            mDragging = true;

            mLastGrabPos = getCursorPos();
            mRelGrabPos = Utils.subPoints(mLastGrabPos, mTrial.objectRect.getLocation());

            mMoveSampler.start();

            //region LOG
            mTrialInfo.grab_time = mInstantInfo.getGrabTime(mTrial.getClass().getSimpleName());
            mTrialInfo.grab_x = curP.x;
            mTrialInfo.grab_y = curP.y;
            //endregion

        } else { // Grab outside the object
            startError();
        }
    }

    protected void drag() {
        final String TAG = NAME + "drag";
        super.drag();

        final Point curP = getCursorPos();

        mEventCounter++;
        mPointSet.add(curP);

        mTrial.moveObject(mRelGrabPos, curP);

        enableObjHint(mCursorInObject);

        // Cursor in Temp
        if (mTrial.tempRect.contains(curP)) {
            if (!mCursorInTemp) { // Entry
                mInstantInfo.logCurTempEntry(); // LOG
                mCursorInTemp = true;
            }
        } else {
            mCursorInTemp = false;
        }

        // Obj in Temp
        if (mTrial.tempRect.contains(mTrial.objectRect)) {
            if (!mObjInTemp) { // Object entry
                Out.d(TAG, "Obj entered temp");
                mInstantInfo.logObjTempEntry(); // LOG
                mTrialInfo.temp_entry_time = mInstantInfo.getTempEntryTime(); // LOG

                mObjInTemp = true;
            }
        } else {
            if (mObjInTemp) { // Object exit (only after entry)
                Out.d(TAG, "Obj exited temp");
                mInstantInfo.logObjTempExit();
                mObjInTemp = false;
            }
        }

        // Obj in Target
        if (mTrial.targetRect.contains(mTrial.objectRect)) {
            if (!mObjInTarget) { // Entry
                Out.d(TAG, "Obj in Target entry");
                //region LOG
                mInstantInfo.logObjTgtEntry();
                mTrialInfo.temp_to_tgt_time = mInstantInfo.getTempToTgtTime();
                mTrialInfo.drag_time = mInstantInfo.getDragTime(mTrial.getClass().getSimpleName());
                //endregion LOG

                mObjInTarget = true;
            }
        } else {
            mObjInTarget = false;
        }

        repaint();
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        super.release(); // always logs release

        if (mDragging) {
            mDragging = false;
            mMoveSampler.stop();

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

        mEventCounter = 0;
        mPointSet.clear();
        enableObjHint(false);
    }

    @Override
    protected void revert() {
        final String TAG = NAME + "revert";
        super.revert(); // always logs revert

        if (mDragging) {
            mDragging = false;
            mMoveSampler.stop();
            enableObjHint(false);

            //region LOG
            mTrialInfo.revert_time = mInstantInfo.getRevertTime();

            mTrialInfo.release_x = getCursorPos().x;
            mTrialInfo.release_y = getCursorPos().y;

            mTrialInfo.trial_time = mInstantInfo.getTrialTime();
            mTrialInfo.total_time = mInstantInfo.getTotalTime();
            //endregion

            if (mObjInTemp) {
                mTrial.revertObject();
                repaint();
                hit();
            } else {
                miss();
            }
        }
    }

    @Override
    protected boolean checkHit() {
        // If exited the temp and is in target
        return mInstantInfo.last_obj_temp_exit > 0 && mObjInTarget;
    }

    private void enableObjHint(boolean enable) {
        if (enable) {
            if (mHightlightObj) COLOR_OBJECT = COLOR_OBJ_HIGHLIGHT;
            if (mChangeCursor) {
                if (mTrial.getAxis().equals(AXIS.VERTICAL)) setCursor(CURSORS.RESIZE_NS);
                else setCursor(CURSORS.RESIZE_EW);
            }
        } else {
            COLOR_OBJECT = COLOR_OBJ_DEFAULT;
            setCursor(CURSORS.DEFAULT);
        }
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
            mMoGraphics.fillRectangle(COLOR_TARGET_RECT, mTrial.targetRect);

            // Draw the temp rectangle
            mMoGraphics.fillRectangle(COLOR_TEMP_RECT, mTrial.tempRect);

            // Draw curtain
            mMoGraphics.fillRectangle(COLOR_CURTAIN, mTrial.curtainRect);

            // Draw the object
            mMoGraphics.fillRectangle(COLOR_OBJECT, mTrial.objectRect);

            // Draw block-trial num
            String stateText =
                    Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                            Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mMoGraphics.drawString(COLOR_TEXT, Consts.FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // TEMP: Draw boundRect
//            mMoGraphics.drawRectangle(COLORS.GRAY_200, mTrial.getBoundRect());

        }
    }

    // Listeners ------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mMouseEnabled) {
            if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
                grab();

                mGrabTime = Utils.nowMillis();
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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mTrialActive) {
            if (mDragging) drag();
            else move();
        }

    }
}
