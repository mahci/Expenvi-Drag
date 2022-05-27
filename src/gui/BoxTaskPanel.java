package gui;

import experiment.BoxTrial;
import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
//    private BoxTask mTask;
    private BoxTrial mTrial;

    private int mBlockNum;
    private int mTrialNum;

    // Flags
//    private boolean mTrialActive = false;
    private boolean mGrabbed = false;

    // Shapes
    private Rectangle mObject = new Rectangle();
    private final MoPanel mTargetPnl = new MoPanel();
    private Group mGroup;
//    private Circle nextTrialD = new Circle();

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
    private Dimension mDim;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private long t0, t1;
    private boolean firstMove;

//    private Graphix mGraphix;

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
    public void start() {
        final String TAG = NAME + "start";

        mBlockNum = 1;
        mTrialNum = 1;
        startBlock(mBlockNum);

    }


    @Override
    protected void showTrial(int trNum) {
        String TAG = NAME + "nextTrial";
//        firstMove = false;

        mTrial = (BoxTrial) mBlock.getTrial(trNum);
        Out.d(TAG, mTrialNum);
        Out.e(TAG, mTrial);

        removeAll();

        // Add the target panel (+ effect) to the panel
        BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
        mTrial.targetPanel.setBorder(bord);
        mTrial.targetPanel.setBackground(COLORS.GRAY_200);
        add(mTrial.targetPanel);

        mTrialActive = true;

        repaint();
    }

    @Override
    public void grab() {
        Point curP = getCursorPos();
        if (mTrial.objectRect.contains(curP)) {
            mGrabbed = true;
            mGrabPos = curP;
        }
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        Out.d(TAG, mGrabbed);
        if (mGrabbed) {
            Out.d(TAG, isHit());
            if (isHit()) {
//                moveObjInside();
                hit();
            } else {
                miss();
            }
        }

        mGrabbed = false;

    }

    @Override
    protected void hit() {
        final String TAG = NAME + "hit";
        Consts.SOUNDS.playHit();

        mTrialActive = false;
        moveObjInside();

        // Wait a certain delay, then show the next trial (or next block)
        Out.d(TAG, mTrialNum, mBlock.getNumTrials());
        if (mTrialNum < mBlock.getNumTrials()) {
            Out.d(TAG, "NEXT!");
            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else if (mBlockNum < mTask.getNumBlocks()) {
            mBlockNum++;
            mTrialNum = 1;
            startBlock(mBlockNum);
        } else {
            // Task is finished

        }
    }

    @Override
    protected void miss() {
        final String TAG = NAME + "miss";

        Consts.SOUNDS.playMiss();
        Out.d(TAG, "Missed on trial", mTrialNum);
        // Shuffle back and reposition the next ones
        final int trNewInd = mBlock.dupeShuffleTrial(mTrialNum);
        Out.e(TAG, "TrialNum | Insert Ind | Total", mTrialNum, trNewInd, mBlock.getNumTrials());
        if (findAllTrialsPosition(trNewInd) == 1) {
            Out.e(TAG, "Couldn't find position for the trials");
            MainFrame.get().showMessage("No positions for trial at " + trNewInd);
        } else {
            // Next trial
//            mTrialNum++;
            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        }

        mTrialActive = false;

    }

    public void moveObjInside() {
        final Rectangle tgtBounds = mTrial.targetPanel.getBounds();
//        final Rectangle objBounds = mObject.getBounds();
        final Rectangle intersection = tgtBounds.intersection(mTrial.objectRect);

        final int dMinX = (int) (intersection.getMinX() - mTrial.objectRect.getMinX());
        final int dMaxX = (int) (intersection.getMaxX() - mTrial.objectRect.getMaxX());

        final int dMinY =  (int) (intersection.getMinY() - mTrial.objectRect.getMinY());
        final int dMaxY =  (int) (intersection.getMaxY() - mTrial.objectRect.getMaxY());

//        mObject.translate(dMinX + dMaxX, dMinY + dMaxY);
        mTrial.objectRect.translate(dMinX + dMaxX, dMinY + dMaxY);
//        mObjectLbl.translate(dMinX + dMaxX, dMinY + dMaxY);

        repaint();
    }

    @Override
    public boolean isHit() {
        return mTrial.targetPanel.getBounds().contains(getCursorPos());
    }

    // -------------------------------------------------------------------------------------------

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        mGraphix = new Graphix(g2d);

        // Draw the object
        mGraphix.fillRectangle(COLORS.BLUE_900_ALPHA, mTrial.objectRect);

        // Draw block-trial num
        String stateText =
                Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                        Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
        mGraphix.drawString(COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                getWidth() - Utils.mm2px(70), Utils.mm2px(10));

        // TEMP: draw bound rect
//        mGraphix.drawRectangle(COLORS.GRAY_500, mTrial.getBoundRect());
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

        if (mTrialActive && mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;

            mTrial.objectRect.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!firstMove) {
            t0 = Utils.nowMillis();
            t1 = System.currentTimeMillis();
            firstMove = true;
        }

        mouseDragged(e);
    }

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

}
