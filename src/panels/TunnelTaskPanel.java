package panels;

import experiment.Experiment;
import experiment.TunnelTrial;
import graphic.MoCircle;
import graphic.MoGraphics;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Math.*;
import static tools.Consts.*;
import static experiment.Experiment.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";

    // Constants
    private final int DRAG_TICK = 5; // millisecs

    // Experiment
    private TunnelTrial mTrial;

    // Things to show
    private Trace mVisualTrace;
    private Trace mTrace;
    private Trace mInTunnelTrace;

    private MoCircle showCirc = new MoCircle();

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Flags
    private boolean mTrialActive = false;
    private boolean mDragOpen = true;
    private boolean isInsideObj = false;
    private boolean highlightObj = false;
    private boolean mEntered = false;
    private boolean mExited = false;
    private boolean mMissed = false;
    private boolean mDragging = false;
    private boolean mTrialStarted = false;

    // Other
    private Point mLastGrabPos = new Point();
    private DIRECTION mDir;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Timer mDragTimer;

    private long t0;
    private boolean firstMove;

    private int mPosCount = 0;

    private MoGraphics mGraphics;

    private List<Integer> tunnelXs = new ArrayList<>();
    private List<Integer> tunnelYs = new ArrayList<>();

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hit();
        }
    };

    private ActionListener mDrageListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            if (mTrialActive && mGrabbed) {

                final Point curP = getCursorPos();

                final int dX = curP.x - mLastGrabPos.x;
                final int dY = curP.y - mLastGrabPos.y;

                final double dragDist = sqrt(pow(dX, 2) + pow(dY, 2));

                if (dragDist > Utils.mm2px(TunnelTask.DRAG_THRSH_mm)) drag();

                repaint();
            }

        }
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

        // Key maps
        mapKeys();
        getActionMap().put(KeyEvent.VK_SPACE, NEXT_TRIAL);

        // Init
        mVisualTrace = new Trace();
        mTrace = new Trace();
        mInTunnelTrace = new Trace();
    }

    public TunnelTaskPanel setTask(TunnelTask tunnelTask) {
        mTask = tunnelTask;

        mDragTimer = new Timer(0, mDrageListener);
        mDragTimer.setDelay(DRAG_TICK);

        return this;
    }

    /**
     * Show the trial
     */
    protected void showTrial(int trNum) {
        String TAG = NAME + "nextTrial";
        Out.d(TAG, mTrialNum, "===============================================");
        mGrabbed = false;
        mDragging = false;
        mEntered = false;
        mExited = false;
        mMissed = false;
        mTrialStarted = false;

        // Reset traces
        mVisualTrace.reset();
        mTrace.reset();
        mInTunnelTrace.reset();

        mPosCount = 0;

        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
//        Out.d(TAG, mTrial);

        // Add all the points to the list
        tunnelXs.clear();
        tunnelYs.clear();
        if (mTrial.getDir().getAxis().equals(AXIS.VERTICAL)) {
            for (int y = mTrial.inRect.minY; y < mTrial.inRect.maxY; y++) {
                tunnelYs.add(y);
            }
        } else {
            for (int x = mTrial.inRect.minX; x < mTrial.inRect.maxX; x++) {
                tunnelXs.add(x);
            }
        }

        repaint();

        mTrialActive = true;
    }


    @Override
    public void grab() {
        Point p = getCursorPos();

        if (mDragOpen && checkGrab()) {
            mGrabbed = true;
            mLastGrabPos = p;

            mDragTimer.start();
        }
    }

    @Override
    public void drag() {
        mDragging = true;

        if (mDragOpen) {
            // Add cursor point to the traces
            mVisualTrace.addPoint(getCursorPos());

            if (!mExited) mTrace.addNewPoint(getCursorPos()); // Only add to mTrace if not exited

            if (!mTrialStarted) {
                checkTrialStart();
            } else { // Trial started
                filterPoints();
                if (checkMiss()) miss();
                else { // Dragging succesfully
                    mExited = checkExit();
                }
            }
        }
    }

    @Override
    public void release() {

        if (mTrialStarted) { // Entered the tunnel
            if (mExited) hit();
            else miss();
        } else if (mGrabbed) { // Still outside the tunnel
            startError();
        }

        mGrabbed = false;
        mDragOpen = true;
    }

    @Override
    protected void revert() {
        miss();
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
        analyzeTrace();
        super.hit();
    }

    @Override
    protected void startError() {
        final String TAG = NAME + "startError";
        Out.e(TAG, "Trial Num", mTrialNum);
        SOUNDS.playStartError();

        mVisualTrace.reset();
        mTrace.reset();

        mDragOpen = false; // Reactive when released (to avoind continuation of dragging)

        repaint();

//        mTrialActive = false;

        // Respawn the trial (everything will be reset)
//        nextTrial();
    }


    private boolean isValidGrab(Point grbP) {
        final String TAG = NAME + "isValidGrab";

        return !mTrial.isPointInside(grbP);
    }

    private boolean checkGrab() {
        if (mTrial.isPointInside(getCursorPos())) {
            SOUNDS.playStartError();
            return false;
        } else {
            return true;
        }
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

                // Add all the inside points to the list
                if (mTrial.getDir().getAxis().equals(AXIS.VERTICAL)) {
                    for (int y = mTrial.inRect.minY; y <= mTrial.inRect.maxY; y++) {

                    }
                }
            }
        }
    }

    private boolean checkMiss() {
        return mTrace.intersects(mTrial.startLine);
    }

    private boolean checkExit() {
        final Point lastP = mTrace.getLastPoint();
        if (!mTrial.inRect.contains(lastP) && mTrace.intersects(mTrial.endLine)) {
            return true;
        } else {
            return false;
        }
    }

    private void filterPoints() {
        final String TAG = NAME + "filterPoints";
        Point p = getCursorPos();

        if (mTrial.inRect.contains(p)) {
//            Out.d(TAG, p);
//            tunnelXs.remove((Integer) p.x);
//            tunnelYs.remove((Integer) p.y);
            mInTunnelTrace.addPoint(p);
        }
    }

    private void analyzeTrace() {
        final String TAG = NAME + "analyzeTrace";
//        HashMap<Integer, List<Point>> map = new HashMap<>();
        int inPointsCount = mInTunnelTrace.getNumPoints();
        int totalNumInPoints = mTrace.getNumPoints();



//        if (mTrial.getDir().getAxis().equals(AXIS.VERTICAL)) {
//            totalNumInPoints = mTrial.inRect.height;
//            inPointsCount = tunnelYs.size();
//            for (int y = mTrial.inRect.minY; y <= mTrial.inRect.maxY; y++) {
////                map.put(y, new ArrayList<>());
//                List<Point> yPoints = mTrace.getYPoints(y);
//
//                boolean toCount = true;
//                for (Point p : yPoints) {
//                    if (!mTrial.inRect.contains(p)) toCount = false;
//                }
//
//                if (toCount) inPointsCount++;
//            }
//
//        } else {
//            totalNumInPoints = mTrial.inRect.width;
//            inPointsCount = tunnelXs.size();
//            for (int x = mTrial.inRect.minX; x <= mTrial.inRect.maxX; x++) {
////                map.put(y, new ArrayList<>());
//                List<Point> xPoints = mTrace.getXPoints(x);
//
//                boolean toCount = true;
//                for (Point p : xPoints) {
//                    if (!mTrial.inRect.contains(p)) toCount = false;
//                }
//
//                if (toCount) inPointsCount++;
//            }
//        }

        // Ratio of inside/total points
        Out.d(TAG,"Accuracy", inPointsCount, totalNumInPoints, inPointsCount * 100.0 / totalNumInPoints);
    }

    private boolean isValidDrag() {
        final String TAG = NAME + "isValidDrag";

//        final int traceSize = mTrace.size();

        // Did the trace interesect the lines?
        if (mTrace.intersects(mTrial.line1Rect) || mTrace.intersects(mTrial.line2Rect)) return false;

        // Not touching lines
        final Point lastP = mTrace.getLastPoint();

        if (!mTrialStarted) { // Not yet entered the tunnel

            // There is a point inside (and NOT on the start line) => Truly entered!
            if (lastP != null && mTrial.inRect.contains(lastP) && mTrial.startLine.ptLineDist(lastP) > 0) {

                // Has it entered through the start?
                if (mTrace.intersects(mTrial.startLine)) {
                    // TODO: Set the start of trial
                    mTrialStarted = true;
                    mTrace.reset(); // Start again from inside

                    return true;
                } else {
                    return false;
                }

            }

        } else { // Trial started

            // Touched start again!
            if (mTrace.intersects(mTrial.startLine)) {
                return false;
            }

            // Exited successfully!
            if (!mTrial.inRect.contains(lastP)) {
                mExited = true;
            }
        }


        return true;
    }

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

        mGraphics = new MoGraphics(g2d);

        // Draw Targets
//        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
        if (mTrial != null) {
//            Out.d(TAG, mTrialNum, mTrial.toString());
            mGraphics.fillRectangles(COLORS.GRAY_500, mTrial.line1Rect, mTrial.line2Rect);

            // Draw Start text
            mGraphics.drawString(COLORS.GREEN_700, FONTS.DIALOG,"Start",
                    mTrial.startTextRect.x,
                    mTrial.startTextRect.y + mTrial.startTextRect.height / 2);

            // Draw block-trial num
            String stateText =
                    STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                    STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mGraphics.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // Draw trace
            final int rad = Trace.TRACE_R;
            for (Point tp : mVisualTrace.getPoints()) {
                mGraphics.fillCircle(COLORS.BLUE_900, new MoCircle(tp, rad));
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
        final String TAG = NAME + "mouseDragged";

//        if (mTrialActive && mGrabbed) {
//
//            final Point curP = e.getPoint();
//
//            final int dX = curP.x - mLastGrabPos.x;
//            final int dY = curP.y - mLastGrabPos.y;
//
//            final double dragDist = sqrt(pow(dX, 2) + pow(dY, 2));
//
//            if (dragDist > Utils.mm2px(TunnelTask.DRAG_THRSH_mm)) drag();
//
//            repaint();
//        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!firstMove) t0 = Utils.nowMillis();

        mouseDragged(e);
    }

    // -------------------------------------------------------------------------------------------
    private class Trace {
        private ArrayList<Point> points = new ArrayList<>();
        private ArrayList<Line2D.Double> segments = new ArrayList<>();

        public static final int TRACE_R = 1;

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

        public int getNumPoints() {
            return points.size();
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
