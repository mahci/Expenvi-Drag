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
    private int mBlockNum;
    private int mTrialNum;

    // Trial
    private boolean mTrialStarted = false;
    private boolean mDragging = false;
    private boolean mCurtainClosed = false;
    private boolean mPastTempRect = false;
    private Point mLastGrabPos = new Point();
    private boolean mChangeCursor = true;
    private boolean mHightlightObj = false;

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

        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";

        mTrial = (PeekTrial) mBlock.getTrial(trNum);
        Out.d(TAG, mTrial);

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

    protected void drag(int dX, int dY) {
        mTrial.moveObject(dX, dY);
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
    protected boolean checkHit() {
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

            // Draw the temp rectangle
            mMoGraphics.fillRectangle(COLORS.GREEN_700, mTrial.tempRect);

            // Draw curtain
            mMoGraphics.fillRectangle(COLORS.ORANGE_200, mTrial.curtainRect);

            // Draw the object
            mMoGraphics.fillRectangle(COLORS.BLUE_900, mTrial.objectRect);

            // Draw block-trial num
            String stateText =
                    Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                            Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mMoGraphics.drawString(Consts.COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // TEMP: Draw boundRect
//            mMoGraphics.drawRectangle(COLORS.GRAY_200, mTrial.getBoundRect());

        }
    }

    // ----------------------------------------------------------------------------------------------


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
        mEventCounter++;
        mPointSet.add(e.getPoint());
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
//        mouseDragged(e);
    }
}
