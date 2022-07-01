package panels;

import control.Logger;
import experiment.TunnelTrial;
import graphic.MoCircle;
import graphic.MoGraphics;
import log.ActionLog;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static tools.Consts.*;
import static experiment.Experiment.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";

    // Constants
    private final int DRAG_TICK = 5; // millisecs
    private final int TRACE_RAD = 1;

    // Experiment
    private TunnelTrial mTrial;

    // Things to show
    private Trace mVisualTrace;
    private Trace mTrace;
    private Trace mTrialTrace;
    private Trace mInTunnelTrace;

    private MoCircle showCirc = new MoCircle();

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Flags
    private boolean mTrialActive = false;
    private boolean mIdle = true;
    private boolean mEntered = false;
    private boolean mExited = false;
    private boolean mMissed = false;
    private boolean mDragging = false;
    private boolean mTrialStarted = false;

    // Other
    private Point mLastGrabPos = new Point();
    private DIRECTION mDir;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Timer mDragTimer, mDragStartTimer;

    // Variables
    private double mAccuracy = 0;

    private MoGraphics mMoGraphics;

    private List<Integer> tunnelXs = new ArrayList<>();
    private List<Integer> tunnelYs = new ArrayList<>();

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hit();
        }
    };

//    private ActionListener mDragStartListner = e -> {
//        final Point curP = getCursorPos();
//
//        final int dX = curP.x - mLastGrabPos.x;
//        final int dY = curP.y - mLastGrabPos.y;
//
//        final double dragDist = sqrt(pow(dX, 2) + pow(dY, 2));
//
//        // If passed the treshold, start dragging
//        if (dragDist > Utils.mm2px(TunnelTask.DRAG_THRSH_mm)) {
//            mInstantLog.drag_start = Utils.nowMillis(); // LOG
//            mDragTimer.start();
//            mDragStartTimer.stop();
//        }
//    };

    private ActionListener mDragSampler = e -> {
        drag();
    };

    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public TunnelTaskPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);

        addMouseMotionListener(this);
        addMouseListener(this);

        mTaskType = TASK.TUNNEL;

        // Key maps
//        mapKeys();

        // Init
        mVisualTrace = new Trace();
        mTrace = new Trace();
        mTrialTrace = new Trace();
        mInTunnelTrace = new Trace();
    }

    public TunnelTaskPanel setTask(TunnelTask tunnelTask) {
        mTask = tunnelTask;

//        mDragStartTimer = new Timer(0, mDragStartListner);
//        mDragStartTimer.setDelay(DRAG_TICK);

        mDragTimer = new Timer(0, mDragSampler);
        mDragTimer.setDelay(DRAG_TICK);

        return this;
    }

    /**
     * Show the trial
     */
    protected void showTrial(int trNum) {
        String TAG = NAME + "nextTrial";
        super.showTrial(trNum);

        // Reset flags
        mIdle = true;
        mGrabbed = false;
        mDragging = false;
        mEntered = false;
        mExited = false;
        mMissed = false;
        mTrialStarted = false;

        // Reset traces
        mVisualTrace.reset();
        mTrace.reset();
        mTrialTrace.reset();
        mInTunnelTrace.reset();

        mPosCount = 0;

        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
//        Out.d(TAG, mTrial);

//        mTrialLog.trial = mTrial.clone(); // LOG
        mGenLog.trialStr = mTrial.toString(); // LOG
        sendGenLog(); // LOG

        repaint();

        mTrialActive = true;
    }

    @Override
    protected void move() {
        super.move();

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.MOVE, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion
    }

    @Override
    public void grab() {
        super.grab();

        Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.GRAB, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mIdle) { // Shouldn't be kept dragging if error occured

            if (mTrial.isPointOutside(curP)) {
                mGrabbed = true;
                mDragging = true;

                mLastGrabPos = curP;

                //region LOG
                mTrialLog.grab_time = mInstantLog.getGrabTime();
                mTrialLog.logGrabPoint(curP);
                //endregion

//                mDragStartTimer.start();
                mDragTimer.start();
            } else {
                startError();
            }
        }

    }

    @Override
    public void drag() {
        final String TAG = NAME + "darg";
        super.drag();

        mDragging = true;

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.DRAG, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        // Add cursor point to the traces
        mVisualTrace.addPoint(curP);
        if (!mExited) mTrace.addNewPoint(curP); // Only add to mTrace if not exited

        // Check the status
        if (!mTrialStarted) {
            checkTrialStart();
        } else { // Trial started (entered into the tunnel from start)
            if (checkMiss()) miss();
            else { // Dragging succesfully
                checkExit();
            }

            if (!mExited) sortPointsInOut(curP);
        }

        repaint();
    }

    @Override
    public void release() {
        super.release();

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.RELEASE, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        mDragTimer.stop();
        mDragging = false;
        mGrabbed = false;

        if (mTrialStarted) { // Entered the tunnel
            //region LOG
            mTrialLog.logReleasePoint(curP);
            mTrialLog.accuracy = mAccuracy;
            //endregion

            if (mExited) hit();
            else miss();

        } else if (mGrabbed) { // Still outside the tunnel
            startError();
        }

        mIdle = true;
    }

    @Override
    protected void revert() {
        super.revert();

        final Point curP = getCursorPos();

        //region LOG
        final ActionLog actionLog = new ActionLog(ACTION.REVERT, curP);
        Logger.get().logAction(mGenLog, actionLog);
        //endregion

        if (mDragging) {
            //region LOG
            mTrialLog.logRevertPoint(curP);
            //endregion

            miss();
        } else {
            startError();
        }
    }

    @Override
    public boolean checkHit() {
        return !mMissed && mDragging && mEntered;
    }

    @Override
    protected void miss() {
        final String TAG = NAME + "miss";

        mTrialStarted = false;

        super.miss();
    }

    @Override
    protected void hit() {
        super.hit();
    }

    @Override
    protected void startError() {
        final String TAG = NAME + "startError";
        Out.e(TAG, "Trial Num", mTrialNum);
        super.startError();

        mVisualTrace.reset();
        mTrace.reset();

        mDragTimer.stop();
        mDragging = false;
        mIdle = false; // Reactive when released (to avoind continuation of dragging)
        mGrabbed = false;

        repaint();
    }

    private void checkTrialStart() {
        final String TAG = NAME + "checkTrialStart";

        // Interesected the lines or the end line?
        if (
                mTrace.intersects(mTrial.line1Rect) ||
                mTrace.intersects(mTrial.line2Rect) ||
                mTrace.intersects(mTrial.endLine)) {
            Out.d(TAG, "Touched the lines");
            startError();
        } else {
            final Point lastP = mTrace.getLastPoint();

            // Entered from the start
            if (lastP != null && mTrial.inRect.contains(lastP) && mTrial.startLine.ptLineDist(lastP) > 0) {
                mTrialStarted = true;
                mTrace.reset(); // Reset the trace (to avoid start check again, to get points only after starting)

                //region LOG
                mInstantLog.logTunnelEntry();
                mTrialLog.tunnel_entry_time = mInstantLog.getTunnelEntryTime();
                //endregion
            }
        }
    }

    private boolean checkMiss() {
        return mTrace.intersects(mTrial.startLine);
    }

    private void checkExit() {
        final Point lastP = mTrace.getLastPoint();

        if (!mTrial.inRect.contains(lastP) && mTrace.intersects(mTrial.endLine)) { // Exited
            // Calculate accuracy
            mAccuracy = mInTunnelTrace.size() * 100.0 / mTrialTrace.size();
            Out.d(NAME, "Accuracy= ", mAccuracy);
            if (!mExited) {
                mExited = true;

                //region LOG
                mInstantLog.logCurTgtEntry();
                mTrialLog.drag_time = mInstantLog.getDragTime(mTaskType);
                //endregion
            }

        }
    }

    private void sortPointsInOut(Point p) {
        final String TAG = NAME + "filterPoints";
        Out.d(TAG, p);
        // All dragging points
        mTrialTrace.addNewPoint(p);

        // Points inside the tunnel
        if (mTrial.inRect.contains(p)) {
            Out.d(TAG, "Inside");
            mInTunnelTrace.addNewPoint(p);
        }
    }

//    private void calculateAccuracy() {
//        final String TAG = NAME + "analyzeTrace";
////        HashMap<Integer, List<Point>> map = new HashMap<>();
////        int inPointsCount = mInTunnelTrace.getNumPoints();
////        int totalPointsCount = mTrialTrace.getNumPoints();
//
//        // Ratio of inside/total points
//        Out.d(TAG,"Accuracy (%) = ", mAccuracy);
//
//
////        if (mTrial.getDir().getAxis().equals(AXIS.VERTICAL)) {
////            totalNumInPoints = mTrial.inRect.height;
////            inPointsCount = tunnelYs.size();
////            for (int y = mTrial.inRect.minY; y <= mTrial.inRect.maxY; y++) {
//////                map.put(y, new ArrayList<>());
////                List<Point> yPoints = mTrace.getYPoints(y);
////
////                boolean toCount = true;
////                for (Point p : yPoints) {
////                    if (!mTrial.inRect.contains(p)) toCount = false;
////                }
////
////                if (toCount) inPointsCount++;
////            }
////
////        } else {
////            totalNumInPoints = mTrial.inRect.width;
////            inPointsCount = tunnelXs.size();
////            for (int x = mTrial.inRect.minX; x <= mTrial.inRect.maxX; x++) {
//////                map.put(y, new ArrayList<>());
////                List<Point> xPoints = mTrace.getXPoints(x);
////
////                boolean toCount = true;
////                for (Point p : xPoints) {
////                    if (!mTrial.inRect.contains(p)) toCount = false;
////                }
////
////                if (toCount) inPointsCount++;
////            }
////        }
//
//
//    }

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        mMoGraphics = new MoGraphics(g2d);

        // Draw Targets
        if (mTrial != null) {
            mMoGraphics.fillRectangles(COLORS.GRAY_500, mTrial.line1Rect, mTrial.line2Rect);

            // Draw Start text
            mMoGraphics.drawString(COLORS.GREEN_700, FONTS.DIALOG,"Start",
                    mTrial.startTextRect.x,
                    mTrial.startTextRect.y + mTrial.startTextRect.height / 2);

            // Draw block-trial num (on practice -> technique)
            if (!mPracticeMode) {
                String stateText =
                        STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " — " +
                                STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
                mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                        getWidth() - Utils.mm2px(60), Utils.mm2px(12));
            } else {
                String stateText = MainFrame.get().mActiveTechnique.getTitle();
                mMoGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                        getWidth() - Utils.mm2px(60), Utils.mm2px(12));
            }

            // Draw trace
//            for (Point tp : mVisualTrace.getPoints()) {
//                mMoGraphics.fillCircle(COLORS.BLUE_900, new MoCircle(tp, rad));
//            }
            g2d.setColor(COLORS.BLUE_900); // Just once instead of every time in the loop
            for (int i = mVisualTrace.size() - 1; i >= 0; i--) {
                mMoGraphics.fillCircle(mVisualTrace.getPoint(i), TRACE_RAD);
            }

            // Temp draws
//            mGraphics.drawLines(COLORS.GRAY_400, mTrial.endLine);
//            mMoGraphics.drawRectangle(COLORS.GRAY_400, mTrial.inRect);
//            mMoGraphics.drawCircle(COLORS.GREEN_700, showCirc);
//            mMoGraphics.drawLine(COLORS.GREEN_700, mTrial.startLine);

        }
    }


    // -------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mMouseEnabled) {
            if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) { // Do nothing on the other button press
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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mTrialActive) {
            move();
        }
    }

    // -------------------------------------------------------------------------------------------
    private class Trace {
        private ArrayList<Point> points = new ArrayList<>();
        private ArrayList<Line2D.Double> segments = new ArrayList<>();

//        public static final int TRACE_R = 1;

        public void addPoint(Point p) {
            // Add point
            points.add(p);

            // Add segment
            final int nPoints = points.size();
            if (nPoints > 1) segments.add(new Line2D.Double(points.get(nPoints - 1), points.get(nPoints - 2)));
        }

        /**
         * Add the point to the list only if it is different than the prev. one
         * @param p Point
         */
        public void addNewPoint(Point p) {
            if (points.size() == 0) addPoint(p);
            else if (!Utils.last(points).equals(p)) addPoint(p);
        }

        public Line2D.Double getLastSeg() {
            if (segments.size() > 1) return Utils.last(segments);
            else return null;
        }

        public int intersectNum(Line2D line) {
            int count = 0;
            for (Line2D.Double seg : segments) {
                if (seg.intersectsLine(line)) {
                    count++;
                }
            }

            return count;
        }

        public boolean intersects(Line2D line) {
            if (segments.size() < 1) return false;

            boolean result = false;
            for (Line2D.Double seg : segments) {
//                Out.d(NAME, Utils.str(seg));
                result = result || seg.intersectsLine(line);
            }

            return result;
        }

        public boolean intersects(Rectangle rect) {
            if (segments.size() < 1) return false;

            boolean result = false;
            for (Line2D.Double seg : segments) {
                result = result || seg.intersects(rect);
            }

            return result;
        }

        public ArrayList<Point> getPoints() {
            return points;
        }

        public Point getPoint(int i) {
            if (i > 0 && i < points.size()) return points.get(i);
            else return null;
        }

        public List<Point> getXPoints(int x) {
            List<Point> result = new ArrayList<>();

            for (Point p : points) {
                if (p.x == x) result.add(p);
            }

            return result;
        }

        public List<Point> getYPoints(int y) {
            List<Point> result = new ArrayList<>();

            for (Point p : points) {
                if (p.y == y) result.add(p);
            }

            return result;
        }

        public Point getLastPoint() {
            return Utils.last(points);
        }

        public int size() {
            return points.size();
        }

        public void reset() {
            points.clear();
            segments.clear();
        }

        @Override
        public String toString() {
            return "Trace{" +
                    "points=" + points +
                    '}';
        }
    }

}
