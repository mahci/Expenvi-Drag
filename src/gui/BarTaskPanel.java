package gui;

import experiment.BarTrial;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class BarTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BarTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Experiment
    private BarTrial mTrial;

    private int mBlockNum;
    private int mTrialNum;

    // Config
    private final boolean mChangeCursor = false;
    private final boolean mHighlightObj = true;

    // Flags
    private boolean mGrabbed = false;
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
//        setDoubleBuffered(true); //set Double buffering for JPanel

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
    public void start() {
        final String TAG = NAME + "start";

        mBlockNum = 1;
        mTrialNum = 1;
        startBlock(mBlockNum);
    }

    @Override
    protected void showTrial(int trNum) {
        final String TAG = NAME + "showTrial";
        Out.d(TAG, trNum);
        mTrial = (BarTrial) mBlock.getTrial(trNum);
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
        if (mGrabbed) {

            if (isHit()) {
                hit();
            } else {
                miss();
            }
        }

        mGrabbed = false;
    }

    @Override
    public boolean isHit() {
        return mTrial.targetRect.contains(mTrial.objectRect);
    }

    @Override
    protected void hit() {
        Consts.SOUNDS.playHit();

        mTrialActive = false;

        // Wait a certain delay, then show the next trial (or next block)
        if (mTrialNum < mBlock.getNumTrials()) {
            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else if (mBlockNum < mTask.getNumBlocks()) {
            mBlockNum++;
            mTrialNum = 1;
//            mBlock = mTask.getBlock(mBlockNum);
            startBlock(mBlockNum);
//            mTrialNum = 0;
//            executorService.schedule(this::nextTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else {
            // Task is finished
        }
    }

    @Override
    protected void miss() {
        final String TAG = NAME + "miss";

        mTrialActive = false;

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
            executorService.schedule(() -> showTrial(++mTrialNum), mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
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

        mGraphix = new Graphix(g2d);

        if (mTrial != null) {

            // Draw the target
            mGraphix.fillRectangle(COLORS.GRAY_500, mTrial.targetRect);
//        mGraphix.fillRectangle(COLORS.GRAY_900, mTrial.line1Rect);
//        mGraphix.fillRectangle(COLORS.GRAY_900, mTrial.line2Rect);

            // Draw the object
            mGraphix.fillRectangle(COLORS.BLUE_900, mTrial.objectRect);

            // Draw block-trial num
            String stateText =
                    Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                            Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mGraphix.drawString(COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // TEMP: draw bounding box
            mGraphix.drawRectangle(COLORS.GRAY_400, mTrial.getBoundRect());
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
        if (e.getButton() == MouseEvent.BUTTON1) {
            grab();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
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

            mTrial.objectRect.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (!firstMove) t0 = Utils.nowMillis();

        // When the cursor gets near the bar TODO
//        mIsCursorNearObj = mGroup.barPath.contains(e.getPoint());
//        if (mIsCursorNearObj) {
//            if (mChangeCursor) setCursor(new Cursor(Cursor.HAND_CURSOR));
//        } else {
//            if (mChangeCursor) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//        }

        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;

//            AffineTransform transform = new AffineTransform();
//            transform.translate(dX, dY);

//            mGroup.barPath.transform(transform);

            mTrial.objectRect.translate(dX, dY);

            mGrabPos = e.getPoint();
//            mouseDragged(e);
        }

        repaint();

    }


}
