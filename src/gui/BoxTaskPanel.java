package gui;

import experiment.Block;
import experiment.BoxTrial;
import experiment.Experiment;
import experiment.TunnelTrial;
import tools.Consts;
import tools.MinMax;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static experiment.Experiment.*;
import static java.lang.Math.*;

import static tools.Consts.COLORS;

public class BoxTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BoxTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
//    private final double OBJECT_W_mm = 20; // Object width (always square)
//    private final double TARGET_W_mm = 60; // Window width (always squeate)
//    private final double DIST_mm = 100; // Distance from edge/corner of Object to edge/corner of Target
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial
//    private final double NEXT_TRIAL_DIST_mm = 50; // Measured from center of Obj. to the next center
    private int MAX_CEHCK_POS = 100;

    // Experiment
    private BoxTask mTask;
    private Block mBlock;
    private BoxTrial mTrial;

    private int mBlockNum;
    private int mTrialNum;

    // Flags
    private boolean mTrialActive = false;
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

    private int mPosCount = 0;

    private Graphix mGraphix;

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
    public BoxTaskPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);


        addMouseMotionListener(this);
        addMouseListener(this);

        // Key maps
        mapKeys();
        getActionMap().put(KeyEvent.VK_SPACE, NEXT_TRIAL);
    }

    public BoxTaskPanel setTask(Experiment.BoxTask boxTask) {
        mTask = boxTask;
        return this;
    }

    @Override
    public void start() {
        final String TAG = NAME + "start";
//        mTargetPnl.setSize(Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_W_mm));
//        BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
//        mTargetPnl.setBorder(bord);
//        mTargetPnl.setBackground(COLORS.GRAY_200);
//
//        mObject.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));

//        mDir = DIRECTION.random();
//        mDir = DIRECTION.E;
//        firstRandPos();
//        translateToPanel();
//        mGroup = new Group(
//                Utils.mm2px(OBJECT_W_mm), Utils.mm2px(TARGET_W_mm),
//                mDir, Utils.mm2px(DIST_mm));

//        if (mGroup.position() == 0) {
//            mGroup.translateToPanel();
//
//            removeAll();
//            add(mGroup.target, DEFAULT_LAYER);
//
//            repaint();
//        } else {
//            Out.e(NAME, "Couldn't find suitable position!");
//        }

//        showTrial();

        mBlockNum = 0;
        showBlock();

    }

    private void showBlock() {
        final String TAG = NAME + "showBlock";

        mBlock = mTask.getBlock(mBlockNum);
        Out.d(TAG, mTask.getNumBlocks(), mBlock);
        int positioningSuccess = findTrialListPosition(0);
        if (positioningSuccess == 0) {
//            setTrialPositions();
            mBlock.setTrialElements();

            mTrialNum = 0;
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

        mTrial = (BoxTrial) mBlock.getTrial(mTrialNum);
        Out.e(TAG, mTrial);

        removeAll();

        // Add the target panel (+ effect) to the panel
        BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
        mTrial.targetPanel.setBorder(bord);
        mTrial.targetPanel.setBackground(COLORS.GRAY_200);

        add(mTrial.targetPanel);

        repaint();

        mTrialActive = true;
    }

    @Override
    public void release() {
        final String TAG = NAME + "release";
        Out.d(TAG, mGrabbed);
        if (mGrabbed) {
            Out.d(TAG, isHit());
            if (isHit()) {
                Consts.SOUNDS.playHit();
                Out.d(TAG, "Hit");
//                mGroup.moveObjInsideTarget();
                moveObjInside();

                repaint();

                hit();

            } else {
                Consts.SOUNDS.playMiss();

                miss();
            }
        }

        mGrabbed = false;

    }

    private void hit() {
        Consts.SOUNDS.playHit();

        mTrialActive = false;

        // Wait a certain delay, then show the next trial (or next block)
        if (mTrialNum < mBlock.getNumTrials() - 1) {
            mTrialNum++;
            executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else if (mBlockNum < mTask.getNumBlocks() - 1) {
            mBlockNum++;
            mBlock = mTask.getBlock(mBlockNum);

            mTrialNum = 0;
            executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
        } else {
            // Task is finished

        }
    }

    private void miss() {
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
    }

    /**
     * Recursively find suitable positions for a list of trials, from (incl.) trInd
     * @param trInd Index of the first trial. If > 0 => prev. Trial restricts, otherwise, free
     * @return Success (0) Fail (1)
     */
    public int findTrialListPosition(int trInd) {
        final String TAG = NAME + "findTrialListPosition";

        final int minNtDist = Utils.mm2px(BoxTask.NT_DIST_mm);
        int maxNtDist = minNtDist;

        Point foundPosition = null;
        Point refP = null;

        // Find position for the trInd trial
        if (trInd > 0) {
            refP = mBlock.getTrial(trInd - 1).getEndPoint();
            maxNtDist = minNtDist + Utils.mm2px(100);
        }

        foundPosition = findTrialPosition(mBlock.getTrial(trInd).getBoundRect(), refP, minNtDist, maxNtDist);
        if (foundPosition != null) mBlock.setTrialLocation(trInd, foundPosition);
        else {
            // TODO: find a solution...
            return 1;
        }

        // Next trials
        for (int ti = trInd + 1; ti < mBlock.getNumTrials(); ti++) {
            Out.d(TAG, "Finding position for trial", ti);
            foundPosition = findTrialPosition(
                    mBlock.getTrial(ti).getBoundRect(),
                    mBlock.getTrial(ti - 1).getEndPoint(),
                    minNtDist, maxNtDist);

            // Search to the max cound
            if (foundPosition == null) {
                Out.d(TAG, "Position not found for trial");
                if (mPosCount < MAX_CEHCK_POS) {
                    mPosCount++;
                    return findTrialListPosition(trInd);
                } else {
                    return 1;
                }
            } else {
                Out.d(TAG, "Position found for trial");
                mBlock.setTrialLocation(ti, foundPosition);
            }
        }

        return 0;
    }

    @Override
    public boolean isHit() {
        return mTrial.targetPanel.getBounds().contains(getCursorPos());
    }

    // -------------------------------------------------------------------------------------------

//    @Override
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
                Consts.STRINGS.BLOCK + " " + (mBlockNum + 1) + "/" + mTask.getNumBlocks() + " --- " +
                        Consts.STRINGS.TRIAL + " " + (mTrialNum + 1) + "/" + mBlock.getNumTrials();
        mGraphix.drawString(COLORS.GRAY_900, Consts.FONTS.STATUS, stateText,
                getWidth() - Utils.mm2px(70), Utils.mm2px(10));
    }

    /**
     * Get the cursor position relative to the panel
     * @return Point
     */
    private Point getCursorPos() {
        Point result = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(result, this);

        return result;
    }

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }


    // -------------------------------------------------------------------------------------------
    // Group of things to show!
    private class Group {
        public Rectangle object = new Rectangle();
        public MoPanel target = new MoPanel();
        private Rectangle circumRect = new Rectangle();

        public DIRECTION dir;
        public int dist;

        // Helper vars
        private int mObjHalfW, mTarHalfW, mSideDist, mLongL, mSideL, mDiffHalf;

        public Group(int objW, int tgtW, DIRECTION dir, int dist) {
            object.setSize(objW, objW);

            target.setSize(tgtW, tgtW);
            BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
            target.setBorder(bord);
            target.setBackground(COLORS.GRAY_200);

            this.dist = dist;
            this.dir = dir;

            // Set helper vars
            mObjHalfW = object.width / 2;
            mTarHalfW = target.getWidth() / 2;
            mSideDist = (int) (dist * 1.0 / sqrt(2));
            mLongL = mObjHalfW + this.dist + target.getWidth();
            mSideL = (int) (mObjHalfW + (this.dist * 1.0 / sqrt(2)) + target.getWidth());
            mDiffHalf = mTarHalfW - mObjHalfW;

            // Create the cicrum rectangle
            circumRect.setLocation(-1, -1);
            setCircumRectSize(dir);

        }

        private void setCircumRectSize(DIRECTION dir) {

            final int tgtW = target.getWidth();
            final int objW = object.width;

            switch (dir) {
                case N, S -> circumRect.setSize(tgtW, tgtW + objW + dist);
                case E, W -> circumRect.setSize(tgtW + objW + dist, tgtW);
                case NE, NW, SE, SW -> {
                    final int s = (int) (tgtW + objW + mSideDist);
                    circumRect.setSize(s, s); // Square
                }
            };
        }

        /**
         * Position things w/o any constraints (only fitting the display area)
         * @return Success: 0, fail: 1
         */
        public int position() {

            // If it fits the display area, position obj. and target accordingly and return 0
            while(!getDispArea().contains(circumRect)) {
                positionCircumRect();
            }

            positionElements();
            return 0;
        }

        public int position(Point curPoint, DIRECTION dir, int ntD) {
            final String TAG = NAME + "position";

            this.dir = dir;
            setCircumRectSize(dir);

            // Find the possible Xs
            final Circle nextTrialRange = new Circle(curPoint, ntD);

            // Get all the candidate points
            List<Point> candidPoints = new ArrayList<>();
            final int cx = curPoint.x;
            final int cy = curPoint.y;
            final double d2 = pow(ntD, 2);
            final int minY = max(0, cy - ntD);
            final int maxY = min(getDispDim().height, cy + ntD);
            for (int y = minY; y < maxY; y++) {
//                Out.d(TAG, y, cy, d2, pow(y - cy, 2), sqrt(d2 - pow(y - cy, 2)), cx);
                Point p1 = new Point((int) (sqrt(d2 - pow(y - cy, 2)) + cx), y);
                Point p2 = new Point((int) (-sqrt(d2 - pow(y - cy, 2)) + cx), y);
                candidPoints.add(p1);
                candidPoints.add(p2);
            }

            // Try to find a position
            for (Point p : candidPoints) {
//                Out.d(TAG, p);
                positionCircumRect(p.x, p.y);

                if (getDispArea().contains(circumRect)) {
                    positionElements();
                    return 0;
                }
            }

            return 1;
        }

        private void positionCircumRect() {
            int dispW = getDispDim().width;
            int dispH = getDispDim().height;

            circumRect.setLocation(
                    Utils.randInt(0, dispW - circumRect.width),
                    Utils.randInt(0, dispH - circumRect.height));
        }

        /**
         * Position circum rectangle based on object's center point
         * @param objCX Object center X
         * @param objCY Object center Y
         */
        private void positionCircumRect(int objCX, int objCY) {

            switch (dir) {
                case N -> circumRect.setLocation(objCX - mTarHalfW, objCY - mLongL);
                case S -> circumRect.setLocation(objCX - mTarHalfW, objCY - mObjHalfW);

                case E -> circumRect.setLocation(objCX - mObjHalfW, objCY - mTarHalfW);
                case W -> circumRect.setLocation(objCX - mLongL, objCY - mTarHalfW);

                case NE -> circumRect.setLocation(objCX - mObjHalfW, objCY - mSideL);
                case NW -> circumRect.setLocation(objCX - mSideL, objCY - mSideL);

                case SE -> circumRect.setLocation(objCX - mObjHalfW, objCY - mObjHalfW);
                case SW -> circumRect.setLocation(objCX - mSideL, objCY - mObjHalfW);
            };
        }

        private void positionElements() {

            switch (dir) {
                case N -> {
                    object.setLocation(
                            circumRect.x + mDiffHalf,
                            circumRect.y + circumRect.height - object.width);
                    target.setLocation(circumRect.getLocation()); // UL point the same
                }

                case S -> {
                    object.setLocation(circumRect.x + mDiffHalf, circumRect.y);
                    target.setLocation(circumRect.x, circumRect.y + circumRect.height - target.getWidth());
                }

                case E -> {
                    object.setLocation(circumRect.x, circumRect.y + mDiffHalf);
                    target.setLocation(circumRect.x + circumRect.width - target.getWidth(), circumRect.y);
                }

                case W -> {
                    object.setLocation(
                            circumRect.x + circumRect.width - object.width,
                            circumRect.y + mDiffHalf);
                    target.setLocation(circumRect.getLocation()); // UL point the same
                }

                case NE -> {
                    object.setLocation(circumRect.x, circumRect.y + circumRect.height - object.width);
                    target.setLocation(circumRect.x + circumRect.width - target.getWidth(), circumRect.y);
                }

                case NW -> {
                    object.setLocation(
                            circumRect.x + circumRect.width - object.width,
                            circumRect.y + circumRect.height - object.width);
                    target.setLocation(circumRect.getLocation());
                }

                case SE -> {
                    object.setLocation(circumRect.getLocation());
                    target.setLocation(
                            circumRect.x + circumRect.width - target.getWidth(),
                            circumRect.y + circumRect.height - target.getWidth());
                }

                case SW -> {
                    object.setLocation(circumRect.x + circumRect.width - object.width, circumRect.y);
                    target.setLocation(circumRect.x, circumRect.y + circumRect.height - target.getWidth());
                }
            };
        }

        public void translateToPanel() {
            final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
            final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

            circumRect.translate(lrMargin, tbMargin);
            positionElements();
        }

        public void translateObject(int dX, int dY) {
            object.translate(dX, dY);
        }

        /**
         * Move obj. inside only if not already in
         */
        public void moveObjInsideTarget() {

            if (target.getBounds().contains(object)) return;

            final Rectangle tgtBounds = target.getBounds();
            final Rectangle objBounds = object.getBounds();
            final Rectangle intersection = tgtBounds.intersection(objBounds);

            final int dMinX = (int) (intersection.getMinX() - objBounds.getMinX());
            final int dMaxX = (int) (intersection.getMaxX() - objBounds.getMaxX());

            final int dMinY =  (int) (intersection.getMinY() - objBounds.getMinY());
            final int dMaxY =  (int) (intersection.getMaxY() - objBounds.getMaxY());

            object.translate(dMinX + dMaxX, dMinY + dMaxY);
        }

        public boolean objectContains(Point p) {
            return object.contains(p);
        }

        public boolean targetContains(Point p) {
            return target.getBounds().contains(p);
        }

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
        Out.d(NAME, "mouseReleased", e.getButton());
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
