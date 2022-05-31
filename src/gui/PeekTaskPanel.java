package gui;

import experiment.BarTrial;
import experiment.Experiment;
import experiment.PeekTrial;
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
    private boolean mGrabbed = false;
    private boolean mWentInsideTarget = false;
    private Point mGrabPos = new Point();

    // Executor
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

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
        return this;
    }

    @Override
    public void start() {
        final String TAG = NAME + "start";

        mBlockNum = 1;
        mTrialNum = 1;
        startBlock(mBlockNum);
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";

        mTrial = (PeekTrial) mBlock.getTrial(trNum);

        repaint();
        mTrialActive = true;
    }

    @Override
    public void grab() {
        if (mTrial.objectRect.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        Out.d(TAG, mGrabbed);
//        if (mGrabbed) {
//            Out.d(TAG, isHit());
//            if (isHit()) {
////                moveObjInside();
//                hit();
//            } else {
//                miss();
//            }
//        }

        mGrabbed = false;
    }

    @Override
    protected boolean isHit() {
        return false;
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

        mGraphix = new Graphix(g2d);

        if (mTrial != null) {

            // Draw curtain
            mGraphix.fillRectangle(COLORS.ORANGE_200, mTrial.curtainRect);

            // Draw the target
            mGraphix.fillRectangle(COLORS.GRAY_500, mTrial.targetRect);

            // Draw the object
            mGraphix.fillRectangle(COLORS.BLUE_900, mTrial.objectRect);


            // Draw block-trial num
            String stateText =
                    Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                            Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mGraphix.drawString(Consts.COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // TEMP: Draw boundRect
//            mGraphix.drawRectangle(COLORS.GRAY_200, mTrial.getBoundRect());

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
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) {
            release();
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
        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;

            mTrial.moveObject(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseDragged(e);
    }
}
