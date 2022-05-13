package gui;

import experiment.Block;
import experiment.Experiment;
import experiment.TunnelTrial;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static tools.Consts.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";
    private final String LOG = "Enter/";

    private int MAX_CEHCK_POS = 100;

    // Things to show
    private Experiment.TunnelTask mTask;
    private Block mBlock;
    private TunnelTrial mTrial;

    private Trace mVisualTrace;
    private Trace mTrace;

    private Circle showCirc = new Circle();

//    private List<Point> trialPositions = new ArrayList<>();

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Flags
    private boolean mTrialActive = false;
    private boolean mGrabbed = false;
    private boolean isInsideObj = false;
    private boolean highlightObj = false;
    private boolean mEntered = false;
    private boolean mExited = false;
    private boolean mMissed = false;
    private boolean mDragging = false;
    private boolean mTrialStarted = false;

    // Other
    private Point mLastGrabPos = new Point();
    private Experiment.DIRECTION mDir;


    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private long t0;
    private boolean firstMove;

    private int mTrialNum = 0;

    private int mPosCount = 0;

    private Graphix mGraphix;

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mTrialNum++;
            showTrial();
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
    }

    public TunnelTaskPanel setTask(Experiment.TunnelTask tunnelTask) {
        mTask = tunnelTask;
        mBlock = mTask.getBlock(0);

        return this;
    }

    @Override
    public void start() {
        super.start();

        int positioningSuccess = findTrialListPosition(0);
        if (positioningSuccess == 0) {
//            setTrialPositions();
            mBlock.setTrialElements();

            mTrialNum = 0;
            showTrial();
        }

    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        Out.d(TAG, mTrialNum, "===============================================");
        mGrabbed = false;
        mDragging = false;
        mEntered = false;
        mExited = false;
        mMissed = false;
        mTrialStarted = false;

        mVisualTrace.reset();
        mTrace.reset();

        mPosCount = 0;

        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
//        Out.d(TAG, mTrial);

        repaint();

        mTrialActive = true;
    }


    @Override
    public void grab() {
        Point p = getCursorPos();
        if (isValidGrab(p)) {
            mGrabbed = true;
            mLastGrabPos = p;
        } else {
            startError();
        }
    }

    @Override
    public void drag() {
        mDragging = true;

        if (mTrialStarted) {
            if (!isValidDrag()) miss();
        } else {
            if (!isValidDrag()) startError();
        }
    }

    @Override
    public void release() {

        if (mGrabbed && !mMissed) {
            if (mExited) hit();
            else if (mTrialStarted) {
                miss();
            } else {
                startError();
            }
        }

        mGrabbed = false;
        mDragging = false;

    }

    @Override
    public boolean isHit() {
        Out.d(NAME, mMissed, mDragging, mEntered);
        return !mMissed && mDragging && mEntered;
    }

    /**
     * Get the cursor position relative to the panel
     * @return Point
     */
    private Point getCursorPos() {
        Point result = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(result, this);

        return  result;
    }

    private void hit() {
        SOUNDS.playHit();

        mTrialActive = false;
        // Wait a certain delay, then show the next trial
        mTrialNum++;
        executorService.schedule(this::showTrial, mTask.NT_DELAY_ms, TimeUnit.MILLISECONDS);
    }

    /**
     * What to do when the trial is missed?
     */
    private void miss() {
        final String TAG = NAME + "miss";

        SOUNDS.playMiss();
        mMissed = true;
        Out.d(TAG, "Missed on trial", mTrialNum);
        // Shuffle back and reposition the next ones
        final int trNewInd = mBlock.dupeShuffleTrial(mTrialNum);
        Out.e(TAG, "TrialNum | Insert Ind | Total", mTrialNum, trNewInd, mBlock.getNTrials());
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

    private void startError() {
        final String TAG = NAME + "startError";
        Out.e(TAG, "Trial Num", mTrialNum);
        SOUNDS.playStartError();

        mTrialActive = false;
        // Respawn the trial (everything will be reset)
        showTrial();
    }


    private boolean isValidGrab(Point grbP) {
        final String TAG = NAME + "isValidGrab";

        return !mTrial.isPointInside(grbP);
    }

    private boolean isValidDrag() {
        final String TAG = LOG + "isValidDrag";

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
                    Out.d(TAG, lastP, mTrial.inRect.printCorners());

                    return true;
                } else {
                    Out.e(TAG, "Not from start!");
                    return false;
                }

            }

        } else { // Trial started

            // Touched start again!
            Out.d(TAG, mTrace);
            if (mTrace.intersects(mTrial.startLine)) {
                Out.e(TAG, "Touched start again!");
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

        mGraphix = new Graphix(g2d);

        // Draw trace
//        g2d.setColor(Consts.COLORS.BLUE_900);
        final int rad = Trace.TRACE_R;
        for (Point tp : mVisualTrace.getPoints()) {
//            g2d.fillOval(tp.x - rad, tp.y - rad, rad * 2, rad * 2);
            final Circle circ = new Circle(tp, rad);
            mGraphix.fillCircle(COLORS.BLUE_900, circ);
        }

        // Draw Targets
//        g2d.setColor(COLORS.GRAY_500);
//        g2d.fill(mTrial.line1Rect);
//        g2d.fill(mTrial.line2Rect);
        if (mTrial != null) {
            mGraphix.fillRectangles(COLORS.GRAY_500, mTrial.line1Rect, mTrial.line2Rect);

            // Draw text
            g2d.setColor(COLORS.GREEN_700);
            g2d.drawString("Start", mTrial.startTextRect.x,
                    mTrial.startTextRect.y + mTrial.startTextRect.height / 2);

            // Draw bounding box
            mGraphix.drawRectangle(COLORS.GRAY_400, mTrial.inRect);

            // Draw start line
            mGraphix.drawLine(COLORS.GREEN_700, mTrial.startLine);

            // Temp: show range circle
            mGraphix.drawCircle(COLORS.GREEN_700, showCirc);
//        g2d.drawRect(mTunnelTrial.startTextRect.x, mTunnelTrial.startTextRect.y,
//                mTunnelTrial.startTextRect.width, mTunnelTrial.startTextRect.height);

        }
    }





    /**
     * Recursively find suitable positions for a list of trials, from (incl.) trInd
     * @param trInd Index of the first trial. If > 0 => prev. Trial restricts, otherwise, free
     * @return Success (0) Fail (1)
     */
    public int findTrialListPosition(int trInd) {
        final String TAG = NAME + "position";
        Out.d(TAG, "-----------------------------------------------");
        Out.d(TAG, "trInd | nTrials", trInd, mBlock.getNTrials());
        final int minNtDist = Utils.mm2px(mTask.NT_DIST_mm);
        int maxNtDist = minNtDist;

        Point foundPosition = null;
        Point refP = null;

//        if (trInd == mBlock.getNTrials() - 1 && trInd > 0) { // Last trial
//            refP = mBlock.getTrial(trInd - 1).getEndPoint();
//            // Loops the points aroung the refP to find suitable position
//            foundPosition = findTrialPosition(mBlock.getTrial(trInd).getBoundRect(), refP, ntDist);
//
//            if (foundPosition != null) { // Found!
//                Out.d(TAG, "foundPosition", foundPosition);
//                mBlock.setTrialLocation(trInd, foundPosition);
//                return 0;
//            } else { // No position found
//                Out.d(TAG, "foundPosition", foundPosition);
//                // TODO: Chnage distance?
//                return 1;
//            }
//        } else { // Recursive
//
//            if (trInd - 1 > 0) refP = mBlock.getTrial(trInd - 1).getEndPoint();
//
//            foundPosition = findTrialPosition(mBlock.getTrial(trInd).getBoundRect(), refP, ntDist);
//
//            if (foundPosition != null) { // Found!
//                Out.d(TAG, "foundPosition - rec", foundPosition);
//                mBlock.setTrialLocation(trInd, foundPosition);
//                return findTrialListPosition(trInd + 1);
//            } else { // No position found
//                // TODO: Chnage distance?
//                Out.d(TAG, "foundPosition - rec", foundPosition);
//                return 1;
//            }
//        }

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
        for (int ti = trInd + 1; ti < mBlock.getNTrials(); ti++) {
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

    /**
     * Find a position for a trial
     * @param trBoundRect Boudning box of the trial
     * @param ptP Previous trial position (null if not reference)
     * @return The found point or null (if nothing found)
     */
    public Point findTrialPosition(Rectangle trBoundRect, Point ptP, int minNtDist, int maxNtDist) {
        final String TAG = NAME + "findTrialPosition";
        Out.d(TAG, "BoundRect", trBoundRect.toString());
        Out.d(TAG, "W | H", getWidthMinMax(), getHeightMinMax());
        Circle rangeCircle = new Circle();
        int ntDist = minNtDist;
        Out.d(TAG, "ptP | ntDist", ptP, ntDist);
        if (ptP != null) { // Contrained by the previous trial

            while (ntDist <= maxNtDist) {

                rangeCircle = new Circle(ptP, ntDist);
                final List<Point> rangePoints = rangeCircle.getPoints();
                Collections.shuffle(rangePoints); // Shuffle for random iteration

                Rectangle rect = trBoundRect;
                for (Point candP : rangePoints) {
                    rect.setLocation(candP);
                    if (getPanelBounds().contains(rect)) { // Fits the window?
                        return candP;
                    }
                }

                Out.d(TAG, "Distance checked", ntDist);
                ntDist += 5; // Increase by 10 px
            }

        } else {
            return getPanelBounds().fitRect(trBoundRect);
        }

        showCirc = rangeCircle;
        repaint();
        return null;
    }


    // -------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mTrialActive && e.getButton() == MouseEvent.BUTTON1) { // Do nothing on the other button press
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
        final String TAG = LOG + "mouseDragged";

        if (mTrialActive && mGrabbed) {

            final Point curP = e.getPoint();

            final int dX = curP.x - mLastGrabPos.x;
            final int dY = curP.y - mLastGrabPos.y;

            // Add cursor point to the traces
            mVisualTrace.addPoint(curP);
            mTrace.addNewPoint(curP);

            final double dragDist = sqrt(pow(dX, 2) + pow(dY, 2));

            if (dragDist > Utils.mm2px(mTask.DRAG_THRSH_mm)) drag();

            repaint();

        }
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
