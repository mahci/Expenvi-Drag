package gui;

import experiment.BarTrial;
import experiment.Block;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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

    // Constants
    private final double BAR_L_mm = 30; // Bar length
    private final double BAR_W_mm = 1; // Bar Width
    private final double TARGET_L_mm = 90; // Lneght of the target lines (> bar L)
    private final double TARGET_W_mm = 1; // Targets width
    private final double BAR_GRAB_TOL_mm = 1; // tolearnce from each side (to grab)
    private final double TARGET_D_mm = 5; // Perpendicular distance betw. the target lines (> bar L)
    private final double DIST_mm = 100; // Distance from center of bar to the middle of the target lines (= rect cent)
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial

    // Experiment
    private BarTask mTask;
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
//    private Group mGroup;
//    private Rectangle mBarRect = new Rectangle();
//    private Rectangle mTarRect1 = new Rectangle();
//    private Rectangle mTarRect2 = new Rectangle();
//    private Rectangle mTarInRect = new Rectangle();
//
//    private Path2D.Double mBarPath = new Path2D.Double();
//    private Path2D.Double mTar1Path = new Path2D.Double();
//    private Path2D.Double mTar2Path = new Path2D.Double();
//    private Path2D.Double mTarInPath = new Path2D.Double();

    // Other
//    private Point mGrabPos = new Point();
//    private DIRECTION mDir;
//    private Dimension mDim;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private long t0;
    private boolean firstMove;

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showTrial();
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

    public BarTaskPanel setTask(BarTask barTask) {
        mTask = barTask;
        return this;
    }

    @Override
    public void start() {
        final String TAG = NAME + "start";

        mBlockNum = 1;
        showBlock();
    }

    private void showBlock() {
        final String TAG = NAME + "showBlock";

        mBlock = mTask.getBlock(mBlockNum);
        Out.d(TAG, mTask.getNumBlocks(), mBlock);
        int positioningSuccess = findTrialListPosition(1);
        if (positioningSuccess == 0) {
            mBlock.setTrialElements();

            mTrialNum = 1;
            showTrial();
        } else {
            Out.e(TAG, "Couldn't find positions for the trials in the block!");
        }
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        firstMove = false;

        mTrial = (BarTrial) mBlock.getTrial(mTrialNum);
        Out.e(TAG, mTrial);

        repaint();

        mTrialActive = true;

//        mDir = Experiment.DIRECTION.random();
//
//        mGroup = new Group(
//                Utils.mm2px(BAR_W_mm), Utils.mm2px(BAR_L_mm),
//                Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_L_mm),
//                Utils.mm2px(TARGET_D_mm), mDir, Utils.mm2px(DIST_mm));

//        Out.d(TAG, mDir);
//
//        if (mGroup.position() == 0) {
//            mGroup.translateToPanel();
//
//            repaint();
//        } else {
//            Out.e(NAME, "Couldn't find suitable position!");
//        }

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
            if (isHit()) hit();
            else miss();

            mGrabbed = false;

            // Wait a certain delay, then show the next trial
            executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);

            Out.d(NAME, (Utils.nowMillis() - t0) / 1000.0);
        }
    }

    @Override
    public boolean isHit() {
        return mTrial.inRect.contains(mTrial.objectRect);
    }

    @Override
    protected void hit() {
        Consts.SOUNDS.playHit();

        mTrialActive = false;

        // Wait a certain delay, then show the next trial (or next block)
        if (mTrialNum < mBlock.getNumTrials()) {
            mTrialNum++;
            executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else if (mBlockNum < mTask.getNumBlocks()) {
            mBlockNum++;
            mBlock = mTask.getBlock(mBlockNum);

            mTrialNum = 1;
            executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
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
        if (findTrialListPosition(trNewInd) == 1) {
            Out.e(TAG, "Couldn't find position for the trials");
            MainFrame.get().showMessage("No positions for trial at " + trNewInd);
        } else {
            // Next trial
            mTrialNum++;
            executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        }

        mTrialActive = false;

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


        // Draw the target lines
        mGraphix.fillRectangle(COLORS.GRAY_900, mTrial.line1Rect);
        mGraphix.fillRectangle(COLORS.GRAY_900, mTrial.line2Rect);

        // Draw the object
        mGraphix.fillRectangle(COLORS.BLUE_900, mTrial.objectRect);

        // Draw block-trial num
        String stateText =
                Consts.STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                        Consts.STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
        mGraphix.drawString(COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                getWidth() - Utils.mm2px(70), Utils.mm2px(10));

        // TEMP: draw bounding box
//        mGraphix.drawRectangle(COLORS.GRAY_400, mTrial.getBoundRect());

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

//            AffineTransform transform = new AffineTransform();
//            transform.translate(dX, dY);
//
//            mGroup.barPath.transform(transform);

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
