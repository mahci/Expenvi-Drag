package panels;

import control.Logger;
import experiment.BarTrial;
import graphic.MoGraphics;
import tools.Consts;
import tools.Out;
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

    private long t0;
    private boolean firstMove;

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
    public void grab() {
        super.grab();

        if (mTrial.objectRect.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        } else {
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

            // Draw block-trial num
            String stateText =
                    Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                            Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mMoGraphics.drawString(COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

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
            if (mGrabbed) {
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


}
