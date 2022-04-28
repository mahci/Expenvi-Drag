package gui;

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
    private final double OBJECT_W_mm = 20; // Object width (always square)
    private final double TARGET_W_mm = 60; // Window width (always squeate)
    private final double DIST_mm = 100; // Distance from edge/corner of Object to edge/corner of Target
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial
    private final double NEXT_TRIAL_DIST_mm = 50; // Measured from center of Obj. to the next center

    // Flags
    private boolean mGrabbed = false;

    // Shapes
    private Rectangle mObject = new Rectangle();
    private Rectangle mTarget = new Rectangle();
    private final MoPanel mTargetPnl = new MoPanel();
    private Group mGroup;
//    private Circle nextTrialD = new Circle();

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
    private Dimension mDim;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

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

    @Override
    public void start() {
        final String TAG = NAME + "start";
//        mTargetPnl.setSize(Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_W_mm));
//        BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
//        mTargetPnl.setBorder(bord);
//        mTargetPnl.setBackground(COLORS.GRAY_200);
//
//        mObject.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));

        mDir = DIRECTION.random();
//        mDir = DIRECTION.E;
//        firstRandPos();
//        translateToPanel();
        mGroup = new Group(
                Utils.mm2px(OBJECT_W_mm), Utils.mm2px(TARGET_W_mm),
                mDir, Utils.mm2px(DIST_mm));

        if (mGroup.position() == 0) {
            mGroup.translateToPanel();

            removeAll();
            add(mGroup.mTarget, DEFAULT_LAYER);

            repaint();
        } else {
            Out.e(NAME, "Couldn't find suitable position!");
        }

//        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = DIRECTION.random();
//        mDir = DIRECTION.W;
//
        int nextTrialD = Utils.mm2px(NEXT_TRIAL_DIST_mm);
        Out.d(TAG, mDir, nextTrialD);
        if (mGroup.position(getCursorPos(), mDir, nextTrialD) == 0) {
            Out.d(TAG, "Successfully positioned");
            mGroup.translateToPanel();

            removeAll();
            add(mGroup.mTarget, DEFAULT_LAYER);

            repaint();
        } else {
            Out.e(TAG, "Couldn't find a suitable position!");
        }
//        Rectangle otRect = randPos();
//
//        if (otRect.x != -1) { // valid location
//
//            translateToPanel();
//
//            removeAll();
//            add(mTargetPnl, DEFAULT_LAYER);
//
//            repaint();
//        }

    }

    private void firstRandPos() {
        String TAG = NAME + "randPos";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // In px
        final int dist = Utils.mm2px(DIST_mm);
        final int sideDist = (int) (dist / sqrt(2));

        final int tgtW = mTargetPnl.getWidth();
        final int objW = mObject.width;
        int combW = tgtW + dist + objW; // Combined rectangle side

        // NEWS
        switch (mDir) {
            case N -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - tgtW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() + ((tgtW - objW) / 2),
                        mTargetPnl.getY() + (tgtW + dist));
            }

            case S -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - tgtW),
                        Utils.randInt(objW + dist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + ((tgtW - objW) / 2),
                        mTargetPnl.getY() - (objW + dist));
            }

            case E -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + dist, dispW - tgtW),
                        Utils.randInt(0, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + dist),
                        mTargetPnl.getY() + ((tgtW - objW) / 2));
            }

            case W -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - combW),
                        Utils.randInt(0, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + dist),
                        mTargetPnl.getY() + ((tgtW - objW) / 2));
            }
        }

        // Diagonal
        combW = tgtW + sideDist + objW; // Combined rectangle side

        switch (mDir) {
            case NE -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + sideDist, dispW - tgtW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + sideDist),
                        mTargetPnl.getY() + (tgtW + sideDist));
            }

            case NW -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - combW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + sideDist),
                        mTargetPnl.getY() + (tgtW + sideDist));
            }

            case SE -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + sideDist, dispW - tgtW),
                        Utils.randInt(objW + sideDist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + sideDist),
                        mTargetPnl.getY() - (objW + sideDist));
            }

            case SW -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, combW),
                        Utils.randInt(objW + sideDist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + sideDist),
                        mTargetPnl.getY() - (objW + sideDist));
            }
        }
    }

    private Rectangle randPos() {
        String TAG = NAME + "randPos";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // In px
        final int dist = Utils.mm2px(DIST_mm);
        final int sideDist = (int) (dist / sqrt(2));

        final int tgtW = mTargetPnl.getWidth();
        final int objW = mObject.width;
        int combW = tgtW + dist + objW; // Combined rectangle side

        // Next trial dist circle
        final int nextTrialD = Utils.mm2px(NEXT_TRIAL_DIST_mm);
        final Circle nextTrialRange = new Circle(mObject.getCenterX(), mObject.getCenterY(), nextTrialD);

        final MinMax objCntPossX = getObjCntPossX(objW, tgtW, dist, mDir, getDispDim());
        final MinMax objCntPossY = getObjCntPossY(objW, tgtW, dist, mDir, getDispDim());

        // Range of Y (based on the next_trial_dist)
        final MinMax nextTrialY = new MinMax(
                (int) (mObject.getCenterY() - nextTrialD),
                (int) (mObject.getCenterY() + nextTrialD));

        Point objCnt = new Point(-1, -1);
        Rectangle otRect = new Rectangle(-1, -1, 0, 0);
        while (objCnt.x == -1) { // Still haven't found a proper position
            objCnt.y = Utils.randIntBetween(nextTrialY);

            List<Integer> possibleXs = nextTrialRange.intersection(objCnt.y);
            if (possibleXs.isEmpty()) {
                Out.d(TAG, "Couldn't position the trial");
            } else {
                final int ind = Utils.randInt(0, 2); // Flip a coin!
                objCnt.x = possibleXs.get(ind);
                otRect = getOTRect(objW, tgtW, dist, objCnt, mDir);

                if (!checkRectFit(otRect)) {
                    objCnt.x = possibleXs.get(Utils.intNOT(ind));
                    otRect = getOTRect(objW, tgtW, dist, objCnt, mDir);

                    if (!checkRectFit(otRect)) {
                        continue;
                    }
                }
            }

            // Location found!
            return otRect;
        }

        return otRect;

        // NEWS
//        switch (mDir) {
//            case N -> {
//                // Range of Y (based on the next_trial_dist)
//                final MinMax nextTrialY = new MinMax(
//                        (int) (mObject.getCenterY() - nextTrialD),
//                        (int) (mObject.getCenterY() + nextTrialD));
//
//                Out.d(TAG, "Y - Possible | Range", objCntPossY, nextTrialY);
//
//                // Set Y
//                if (nextTrialY.min > objCntPossY.max) {
//                    Out.d(TAG, "Couldn't position the trial");
//                    return;
//                } else {
//                    Out.d(TAG, "Compare range", max(nextTrialY.min, objCntPossY.min),
//                            min(nextTrialY.max, objCntPossY.max));
//                    mObject.y = Utils.randInt(
//                            max(nextTrialY.min, objCntPossY.min),
//                            min(nextTrialY.max, objCntPossY.max));
//                }
//
//                // X is based on the next trial dist
//                int objCX = 0, objCY = 0;
//                List<Integer> possibleXs = nextTrialRange.intersection(mObject.y);
//                if (possibleXs.isEmpty()) {
//                    Out.d(TAG, "Couldn't position the trial");
//                    return;
//                } else {
//                    final int ind = Utils.randInt(0, 2); // Flip a coin!
////                    if (objCntPossX.isBetween(possibleXs.get(ind))) objCX
//                }
//
//                mObject.x = objCX - objW / 2;
//                objCY = (int) mObject.getCenterY();
//
//                mTargetPnl.setLocation(
//                        objCX - tgtW / 2,
//                        objCY - objW / 2 - dist - tgtW);
//
//                Out.d(TAG, mObject, mTargetPnl);
//
//            }
//
//            case S -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(0, dispW - tgtW),
//                        Utils.randInt(objW + dist, dispH - tgtW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() + ((tgtW - objW) / 2),
//                        mTargetPnl.getY() - (objW + dist));
//            }
//
//            case E -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(objW + dist, dispW - tgtW),
//                        Utils.randInt(0, dispH - tgtW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() - (objW + dist),
//                        mTargetPnl.getY() + ((tgtW - objW) / 2));
//            }
//
//            case W -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(0, dispW - combW),
//                        Utils.randInt(0, dispH - tgtW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() + (tgtW + dist),
//                        mTargetPnl.getY() + ((tgtW - objW) / 2));
//            }
//        }
//
//        // Diagonal
//        combW = tgtW + sideDist + objW; // Combined rectangle side
//
//        switch (mDir) {
//            case NE -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(objW + sideDist, dispW - tgtW),
//                        Utils.randInt(0, dispH - combW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() - (objW + sideDist),
//                        mTargetPnl.getY() + (tgtW + sideDist));
//            }
//
//            case NW -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(0, dispW - combW),
//                        Utils.randInt(0, dispH - combW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() + (tgtW + sideDist),
//                        mTargetPnl.getY() + (tgtW + sideDist));
//            }
//
//            case SE -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(objW + sideDist, dispW - tgtW),
//                        Utils.randInt(objW + sideDist, dispH - tgtW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() - (objW + sideDist),
//                        mTargetPnl.getY() - (objW + sideDist));
//            }
//
//            case SW -> {
//                mTargetPnl.setLocation(
//                        Utils.randInt(0, combW),
//                        Utils.randInt(objW + sideDist, dispH - tgtW));
//
//                mObject.setLocation(
//                        mTargetPnl.getX() + (tgtW + sideDist),
//                        mTargetPnl.getY() - (objW + sideDist));
//            }
//        }
    }

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

//        AffineTransform transform = new AffineTransform();
//        transform.translate(lrMargin, tbMargin);

        mTargetPnl.translate(lrMargin, tbMargin);
        mObject.translate(lrMargin, tbMargin);
    }

    private MinMax getObjCntPossX(int objW, int tgtW, int d, DIRECTION dir, Dimension dispDim) {
        final int halfTW = tgtW / 2;
        final int halfOW = objW / 2;
        final int sideD = (int) (d / sqrt(2));

        return switch (dir) {
            case N, S -> new MinMax(halfTW, dispDim.width - halfTW);
            case NE, SE -> new MinMax(halfOW, dispDim.width - tgtW - sideD - halfOW);
            case NW, SW -> new MinMax(tgtW + sideD + halfOW, dispDim.width - halfOW);
            case E -> new MinMax(halfOW, dispDim.width - tgtW - d - halfOW);
            case W -> new MinMax(tgtW + d + halfOW, dispDim.width - halfOW);
        };
    }

    private MinMax getObjCntPossY(int objW, int tgtW, int d, DIRECTION dir, Dimension dispDim) {
        final int halfTW = tgtW / 2;
        final int halfOW = objW / 2;
        final int sideD = (int) (d / sqrt(2));

        return switch (dir) {
            case E, W -> new MinMax(halfTW, dispDim.height - halfTW);
            case N -> new MinMax(tgtW + d + halfOW, dispDim.height - halfOW);
            case S -> new MinMax(halfOW, dispDim.height - tgtW - d - halfOW);
            case NE, NW -> new MinMax(tgtW + sideD + halfOW, dispDim.height - halfOW);
            case SE, SW -> new MinMax(halfOW, dispDim.height - tgtW - sideD - halfOW);
        };
    }

    /**
     * Try to create the Obj-Tar-Rectangle around the center of the obj
     * @param objCnt Object center
     * @return Set rectangle, or (-1, -1, W, H) if not possible
     */
    private Rectangle getOTRect(int objW, int tgtW, int dist, Point objCnt, DIRECTION dir) {
        Rectangle otRect = new Rectangle(-1, -1, tgtW, tgtW + objW + dist);

        // Temp location (wo/ constrants)
        final int objHW = objW / 2;
        final int tgtHW = tgtW / 2;
        final int longD = objHW + dist + tgtW;
        final int sideD = (int) (objHW + (dist * 1.0 / sqrt(2)) + tgtW);

        switch (dir) {
            case N -> otRect.setLocation(objCnt.x - tgtHW, objCnt.y - longD);
            case S -> otRect.setLocation(objCnt.x - tgtHW, objCnt.y - objHW);

            case E -> otRect.setLocation(objCnt.x - objHW, objCnt.y - tgtHW);
            case W -> otRect.setLocation(objCnt.x - longD, objCnt.y - tgtHW);

            case NE -> otRect.setLocation(objCnt.x - objHW, objCnt.y - sideD);
            case NW -> otRect.setLocation(objCnt.x - sideD, objCnt.y - sideD);

            case SE -> otRect.setLocation(objCnt.x - objHW, objCnt.y - objHW);
            case SW -> otRect.setLocation(objCnt.x - sideD, objCnt.y - objHW);
        };

        return otRect;
    }

    private void posObjTar(Rectangle otRect, DIRECTION dir) {

//        switch (dir) {
//            case N -> {
//                mObject.setLocation(otRect.x , );
//            }
//            case S -> otRect.setLocation(objCnt.x - tgtHW, objCnt.y - objHW);
//
//            case E -> otRect.setLocation(objCnt.x - objHW, objCnt.y - tgtHW);
//            case W -> otRect.setLocation(objCnt.x - longD, objCnt.y - tgtHW);
//
//            case NE -> otRect.setLocation(objCnt.x - objHW, objCnt.y - sideD);
//            case NW -> otRect.setLocation(objCnt.x - sideD, objCnt.y - sideD);
//
//            case SE -> otRect.setLocation(objCnt.x - objHW, objCnt.y - objHW);
//            case SW -> otRect.setLocation(objCnt.x - sideD, objCnt.y - objHW);
//        };
    }

    /**
     * Check if a rectangel can be fitted in disp area
     * @param rect Rectangle
     * @return True if rectangle fitted
     */
    private boolean checkRectFit(Rectangle rect) {
        return getDispArea().contains(rect);
    }

    @Override
    public void grab() {
        if (mGroup.objectContains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    @Override
    public void release() {
        if (mGrabbed) {
            if (isHit()) {
                Consts.SOUNDS.playHit();

                mGroup.moveObjInsideTarget();

                repaint();

//                // Move the object within (if not already)
//                if (!mGroup.targetContains(mObject)) {
//                    moveObjInside();
//                    repaint();
//                }

            } else {
                Consts.SOUNDS.playMiss();
            }

            mGrabbed = false;

            // Wait a certain delay, then show the next trial
            executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);

        }
    }

    public void moveObjInside() {

        final Rectangle tgtBounds = mTargetPnl.getBounds();
        final Rectangle objBounds = mObject.getBounds();
        final Rectangle intersection = tgtBounds.intersection(objBounds);

        final int dMinX = (int) (intersection.getMinX() - objBounds.getMinX());
        final int dMaxX = (int) (intersection.getMaxX() - objBounds.getMaxX());

        final int dMinY =  (int) (intersection.getMinY() - objBounds.getMinY());
        final int dMaxY =  (int) (intersection.getMaxY() - objBounds.getMaxY());

        mObject.translate(dMinX + dMaxX, dMinY + dMaxY);
//        mObjectLbl.translate(dMinX + dMaxX, dMinY + dMaxY);
    }

    @Override
    public boolean isHit() {
        return mGroup.targetContains(getCursorPos());
    }

    // -------------------------------------------------------------------------------------------

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(COLORS.BLUE_900_ALPHA);
        g2d.fill(mGroup.mObject);

        // TEMP
        g2d.setColor(COLORS.BLUE_900);
        g2d.draw(mGroup.mCircumRect);
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
    @Override
    public void mouseDragged(MouseEvent e) {
        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
//            mObject.translate(dX, dY);
            mGroup.mObject.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseDragged(e);
    }

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

    // -------------------------------------------------------------------------------------------
    // Group of things to show!
    private class Group {
        public Rectangle mObject = new Rectangle();
        public MoPanel mTarget = new MoPanel();
        private Rectangle mCircumRect = new Rectangle();

        public DIRECTION mDir;
        public int mOTDist;

        // Helper vars
        private int mObjHalfW, mTarHalfW, mSideDist, mLongL, mSideL, mDiffHalf;

        public Group(int objW, int tgtW, DIRECTION dir, int dist) {
            mObject.setSize(objW, objW);

            mTarget.setSize(tgtW, tgtW);
            BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
            mTarget.setBorder(bord);
            mTarget.setBackground(COLORS.GRAY_200);

            mOTDist = dist;
            mDir = dir;

            // Set helper vars
            mObjHalfW = mObject.width / 2;
            mTarHalfW = mTarget.getWidth() / 2;
            mSideDist = (int) (dist * 1.0 / sqrt(2));
            mLongL = mObjHalfW + mOTDist + mTarget.getWidth();
            mSideL = (int) (mObjHalfW + (mOTDist * 1.0 / sqrt(2)) + mTarget.getWidth());
            mDiffHalf = mTarHalfW - mObjHalfW;

            // Create the cicrum rectangle
            mCircumRect.setLocation(-1, -1);
            setCircumRectSize(dir);

        }

        private void setCircumRectSize(DIRECTION dir) {

            final int tgtW = mTarget.getWidth();
            final int objW = mObject.width;

            switch (dir) {
                case N, S -> mCircumRect.setSize(tgtW, tgtW + objW + mOTDist);
                case E, W -> mCircumRect.setSize(tgtW + objW + mOTDist, tgtW);
                case NE, NW, SE, SW -> {
                    final int s = (int) (tgtW + objW + mSideDist);
                    mCircumRect.setSize(s, s); // Square
                }
            };
        }

        /**
         * Position things w/o any constraints (only fitting the display area)
         * @return Success: 0, fail: 1
         */
        public int position() {

            // If it fits the display area, position obj. and target accordingly and return 0
            while(!getDispArea().contains(mCircumRect)) {
                positionCircumRect();
            }

            positionElements();
            return 0;
        }

        public int position(Point curPoint, DIRECTION dir, int ntD) {
            final String TAG = NAME + "position";

            mDir = dir;
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
                Out.d(TAG, p);
                positionCircumRect(p.x, p.y);

                if (getDispArea().contains(mCircumRect)) {
                    positionElements();
                    return 0;
                }
            }

            return 1;
        }

        private void positionCircumRect() {
            int dispW = getDispDim().width;
            int dispH = getDispDim().height;

            mCircumRect.setLocation(
                    Utils.randInt(0, dispW - mCircumRect.width),
                    Utils.randInt(0, dispH - mCircumRect.height));
        }

        /**
         * Position circum rectangle based on object's center point
         * @param objCX Object center X
         * @param objCY Object center Y
         */
        private void positionCircumRect(int objCX, int objCY) {

            switch (mDir) {
                case N -> mCircumRect.setLocation(objCX - mTarHalfW, objCY - mLongL);
                case S -> mCircumRect.setLocation(objCX - mTarHalfW, objCY - mObjHalfW);

                case E -> mCircumRect.setLocation(objCX - mObjHalfW, objCY - mTarHalfW);
                case W -> mCircumRect.setLocation(objCX - mLongL, objCY - mTarHalfW);

                case NE -> mCircumRect.setLocation(objCX - mObjHalfW, objCY - mSideL);
                case NW -> mCircumRect.setLocation(objCX - mSideL, objCY - mSideL);

                case SE -> mCircumRect.setLocation(objCX - mObjHalfW, objCY - mObjHalfW);
                case SW -> mCircumRect.setLocation(objCX - mSideL, objCY - mObjHalfW);
            };
        }

        private void positionElements() {

            switch (mDir) {
                case N -> {
                    mObject.setLocation(
                            mCircumRect.x + mDiffHalf,
                            mCircumRect.y + mCircumRect.height - mObject.width);
                    mTarget.setLocation(mCircumRect.getLocation()); // UL point the same
                }

                case S -> {
                    mObject.setLocation(mCircumRect.x + mDiffHalf, mCircumRect.y);
                    mTarget.setLocation(mCircumRect.x, mCircumRect.y + mCircumRect.height - mTarget.getWidth());
                }

                case E -> {
                    mObject.setLocation(mCircumRect.x, mCircumRect.y + mDiffHalf);
                    mTarget.setLocation(mCircumRect.x + mCircumRect.width - mTarget.getWidth(), mCircumRect.y);
                }

                case W -> {
                    mObject.setLocation(
                            mCircumRect.x + mCircumRect.width - mObject.width,
                            mCircumRect.y + mDiffHalf);
                    mTarget.setLocation(mCircumRect.getLocation()); // UL point the same
                }

                case NE -> {
                    mObject.setLocation(mCircumRect.x, mCircumRect.y + mCircumRect.height - mObject.width);
                    mTarget.setLocation(mCircumRect.x + mCircumRect.width - mTarget.getWidth(), mCircumRect.y);
                }

                case NW -> {
                    mObject.setLocation(
                            mCircumRect.x + mCircumRect.width - mObject.width,
                            mCircumRect.y + mCircumRect.height - mObject.width);
                    mTarget.setLocation(mCircumRect.getLocation());
                }

                case SE -> {
                    mObject.setLocation(mCircumRect.getLocation());
                    mTarget.setLocation(
                            mCircumRect.x + mCircumRect.width - mTarget.getWidth(),
                            mCircumRect.y + mCircumRect.height - mTarget.getWidth());
                }

                case SW -> {
                    mObject.setLocation(mCircumRect.x + mCircumRect.width - mObject.width, mCircumRect.y);
                    mTarget.setLocation(mCircumRect.x, mCircumRect.y + mCircumRect.height - mTarget.getWidth());
                }
            };
        }

        public void translateToPanel() {
            final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
            final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

            mCircumRect.translate(lrMargin, tbMargin);
            positionElements();
        }

        public void translateObject(int dX, int dY) {
            mObject.translate(dX, dY);
        }

        /**
         * Move obj. inside only if not already in
         */
        public void moveObjInsideTarget() {

            if (mTarget.getBounds().contains(mObject)) return;

            final Rectangle tgtBounds = mTarget.getBounds();
            final Rectangle objBounds = mObject.getBounds();
            final Rectangle intersection = tgtBounds.intersection(objBounds);

            final int dMinX = (int) (intersection.getMinX() - objBounds.getMinX());
            final int dMaxX = (int) (intersection.getMaxX() - objBounds.getMaxX());

            final int dMinY =  (int) (intersection.getMinY() - objBounds.getMinY());
            final int dMaxY =  (int) (intersection.getMaxY() - objBounds.getMaxY());

            mObject.translate(dMinX + dMaxX, dMinY + dMaxY);
        }

        public boolean objectContains(Point p) {
            return mObject.contains(p);
        }

        public boolean targetContains(Point p) {
            return mTarget.getBounds().contains(p);
        }

    }

}
