package panels;

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

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Experiment
    private PeekTrial mTrial;

    // Trial
    private boolean mTrialStarted = false;
    private boolean mTempEntered = false;
    private boolean mDragging = false;
    private boolean mCurtainClosed = false;
    private boolean mPastTempRect = false;
    private Point mLastGrabPos = new Point();
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
    private Timer mDragTimer;

    private int mEventCounter = 0;
    private long mGrabTime;
    private Set<Point> mPointSet = new HashSet<>();


    // Actions ------------------------------------------------------------------------------------
    private ActionListener mDrageListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (mTrialStarted) {
                if (!mTrial.isPointInRange(getCursorPos())) {
                    mTrialStarted = false;
                } else {
                    final int dX = getCursorPos().x - mLastGrabPos.x;
                    final int dY = getCursorPos().y - mLastGrabPos.y;

                    mLastGrabPos = getCursorPos();

                    mTrial.moveObject(dX, dY);

                    repaint();
                }
            } else {
                if (mTrial.objectRect.contains(getCursorPos())) {
                    mTrialStarted = true;
                    mLastGrabPos = getCursorPos();
                }
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
        mDragTimer = new Timer(0, mDrageListener);
        mDragTimer.setDelay(5);
        Out.d(NAME, "1 mm to px = ", Utils.mm2px(1));
        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";

        mTrial = (PeekTrial) mBlock.getTrial(trNum);
        Out.d(TAG, mTrial);

        // Set flags
        mTempEntered = false;

        repaint();
        mTrialActive = true;
    }

    @Override
    public void grab() {
        if (mTrial.objectRect.contains(getCursorPos())) {
            mTrialStarted = true;
            mDragging = true;

            mLastGrabPos = getCursorPos();

            mDragTimer.start();
        }
    }

    protected void drag() {
//        mTrial.moveObject(dX, dY);
        mEventCounter++;
        mPointSet.add(getCursorPos());

        // Only set it once per trial
        if (!mTempEntered && mTrial.tempRect.contains(mTrial.objectRect)) mTempEntered = true;

        repaint();
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";

        if (mTrialStarted) {
            if (checkHit()) {
//                moveObjInside();
                hit();
            } else {
                miss();
            }

        }

        mTrialStarted = false;
        mDragTimer.stop();
    }

    @Override
    protected void revert() {
        final String TAG = NAME + "revert";

        if (mTrialStarted) {
            if (mTrial.tempRect.contains(mTrial.objectRect)) {
                hit();
                mTrial.revertObject();
            } else {
                miss();
            }

        }

        mTrialStarted = false;
        mDragTimer.stop();
    }

    @Override
    protected boolean checkHit() {
        return mTempEntered && mTrial.targetRect.contains(mTrial.objectRect);
    }

    private void enableObjHint(boolean enable) {
        if (enable) {
            if (mHightlightObj) COLOR_OBJECT = COLOR_OBJ_HIGHLIGHT;
            if (mChangeCursor) {
                if (mTrial.axis.equals(AXIS.VERTICAL)) setCursor(CURSORS.RESIZE_NS);
                else setCursor(CURSORS.RESIZE_EW);
            }
        } else {
            COLOR_OBJECT = COLOR_OBJ_DEFAULT;
            setCursor(CURSORS.DEFAULT);
        }
    }

    private void revertObjToTarget() {
        mPointSet.add(getCursorPos());
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
        if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
            grab();

            mGrabTime = Utils.nowMillis();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
            release();

            Out.d(NAME, "Drag time | n(drag event)", Utils.nowMillis() - mGrabTime, mPointSet.size());
//            Out.d(NAME, mPointSet);
            mGrabTime = 0;
            mEventCounter = 0;
            mPointSet.clear();
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
        drag();
//        if (mTrialActive) {
//            final int dX = e.getX() - mLastCurPos.x;
//            final int dY = e.getY() - mLastCurPos.y;
//
//            mLastCurPos = e.getPoint();
//
//            drag(dX, dY);
//
//            repaint();
//        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mTrialActive) {
            if (mTrial.objectRect.contains(e.getPoint())) enableObjHint(true);
            else enableObjHint(false);

            repaint();
        }

        if (mDragging) {
            drag();
        }
    }
}
