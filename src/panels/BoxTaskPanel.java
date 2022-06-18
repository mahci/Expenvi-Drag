package panels;

import control.Logger;
import experiment.BoxTrial;
import experiment.Experiment;
import graphic.MoGraphics;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static tools.Consts.*;

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

    // Flags

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
    private Dimension mDim;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private long t0, t1;

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
    public BoxTaskPanel(Dimension dim) {
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
     * @param boxTask BoxTask
     * @return Self instance
     */
    public BoxTaskPanel setTask(Experiment.BoxTask boxTask) {
        mTask = boxTask;
        return this;
    }

    @Override
    protected void showTrial(int trNum) {
        String TAG = NAME + "nextTrial";
        super.showTrial(trNum);

        mTrial = (BoxTrial) mBlock.getTrial(trNum);

        // LOG
        mTrialInfo = new Logger.TrialInfo();
        mTrialInfo.trial = mTrial.clone();

        repaint();

        mTrialActive = true;
    }

    @Override
    public void grab() {
        super.grab();

        Point curP = getCursorPos();
        if (mTrial.objectRect.contains(curP)) {
            mGrabbed = true;
            mGrabPos = curP;
        } else { // Grab outside the object
            startError();
        }
    }

    @Override
    protected void drag() {
        super.drag();

        final Point curP = getCursorPos();

        // LOG
        if (mTrial.targetRect.contains(curP)) mInstantInfo.logCurTgtEntry();
        if (mTrial.targetRect.contains(curP)) mInstantInfo.logCurTgtEntry();
        if (mTrial.targetRect.contains(mTrial.objectRect)) mInstantInfo.logObjTgtEntry();

        final int dX = curP.x - mGrabPos.x;
        final int dY = curP.y - mGrabPos.y;

        mTrial.objectRect.translate(dX, dY);

        mGrabPos = curP;

        repaint();
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        super.release();

        if (mGrabbed) {
            if (checkHit()) {
                hit();
            } else {
                miss();
            }
        }

        mGrabbed = false;
    }

    @Override
    protected void revert() {
        super.revert();
        miss();
    }

    @Override
    protected void hit() {
        final String TAG = NAME + "hit";

        moveObjInside();

        super.hit();

        // Wait a certain delay, then show the next trial (or next block)
//        Out.d(TAG, mTrialNum, mBlock.getNumTrials());
//        if (mTrialNum < mBlock.getNumTrials()) {
//            Out.d(TAG, "NEXT!");
//            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
//        } else if (mBlockNum < mTask.getNumBlocks()) {
//            mBlockNum++;
//            mTrialNum = 1;
//            startBlock(mBlockNum);
//        } else {
//            // Task is finished
//        }
    }

    public void moveObjInside() {
        if (mTrial != null) {
//        final Rectangle tgtBounds = mTrial.targetPanel.getBounds();
//        final Rectangle objBounds = mObject.getBounds();
            final Rectangle intersection = mTrial.targetRect.intersection(mTrial.objectRect);

            final int dMinX = (int) (intersection.getMinX() - mTrial.objectRect.getMinX());
            final int dMaxX = (int) (intersection.getMaxX() - mTrial.objectRect.getMaxX());

            final int dMinY = (int) (intersection.getMinY() - mTrial.objectRect.getMinY());
            final int dMaxY = (int) (intersection.getMaxY() - mTrial.objectRect.getMaxY());

//        mObject.translate(dMinX + dMaxX, dMinY + dMaxY);
            mTrial.objectRect.translate(dMinX + dMaxX, dMinY + dMaxY);
//        mObjectLbl.translate(dMinX + dMaxX, dMinY + dMaxY);

            repaint();
        }
    }

    @Override
    public boolean checkHit() {
        return mTrial.targetRect.contains(getCursorPos());
    }

//    @Override
//    protected void miss() {
//        Consts.SOUNDS.playMiss();
//
//        mTrialActive = false;
//
//        // Shuffle back and reposition the next ones
//        final  int trNewInd = mBlock.dupeShuffleTrial(mTrialNum);
////        Out.e(TAG, "TrialNum | Insert Ind | Total", mTrialNum, trNewInd, mBlock.getNumTrials());
//        if (findAllTrialsPosition(trNewInd) == 1) {
//            MainFrame.get().showMessage("No positions for trial at " + trNewInd);
//        } else {
//            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
//        }
//    }

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

        // Draw block-trial num
        String stateText =
                STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                        STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
        mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                getWidth() - Utils.mm2px(70), Utils.mm2px(10));

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
                if (mTrial.targetRect.contains(e.getPoint())) mInstantInfo.logCurTgtEntry(); // LOG
                if (mTrial.targetRect.contains(mTrial.objectRect)) mInstantInfo.logObjTgtEntry(); // LOG

                drag();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mTrialActive) {
            // LOG
            mInstantInfo.logMove();
            if (mTrial.objectRect.contains(e.getPoint())) mInstantInfo.logCurObjEntry();

            if (mGrabbed) {
                drag();
            }

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
