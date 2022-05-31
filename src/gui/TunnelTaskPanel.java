package gui;

import experiment.Experiment;
import experiment.TunnelTrial;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static tools.Consts.*;
import static experiment.Experiment.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";
    private final String LOG = "Enter/";

    // Experiment
    private TunnelTrial mTrial;

    // Things to show
    private Trace mVisualTrace;
    private Trace mTrace;

    private Circle showCirc = new Circle();

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

    private int mPosCount = 0;

    private Graphix mGraphix;

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

        mVisualTrace.reset();
        mTrace.reset();

        mPosCount = 0;

        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
        Out.d(TAG, mTrial);

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

        mGrabbed = false;

        if (mTrialStarted) { // Entered the tunnel
            if (mExited) hit();
            else miss();
        } else if (mGrabbed) { // Still outside the tunnel
            startError();
        }

    }

    @Override
    public boolean isHit() {
        Out.d(NAME, mMissed, mDragging, mEntered);
        return !mMissed && mDragging && mEntered;
    }

    @Override
    protected void miss() {
        final String TAG = NAME + "miss";

        mTrialStarted = false;

        super.miss();
    }

    @Override
    protected void startError() {
        final String TAG = NAME + "startError";
        Out.e(TAG, "Trial Num", mTrialNum);
        SOUNDS.playStartError();

        mVisualTrace.reset();
        mTrace.reset();

//        repaint();

//        mTrialActive = false;

        // Respawn the trial (everything will be reset)
//        nextTrial();
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

        mGraphix = new Graphix(g2d);

        // Draw Targets
//        mTrial = (TunnelTrial) mBlock.getTrial(mTrialNum);
        if (mTrial != null) {
//            Out.d(TAG, mTrialNum, mTrial.toString());
            mGraphix.fillRectangles(COLORS.GRAY_500, mTrial.line1Rect, mTrial.line2Rect);

            // Draw Start text
            mGraphix.drawString(COLORS.GREEN_700, FONTS.DIALOG,"Start",
                    mTrial.startTextRect.x,
                    mTrial.startTextRect.y + mTrial.startTextRect.height / 2);

            // Draw block-trial num
            String stateText =
                    STRINGS.BLOCK + " " + mBlockNum + "/" + mTask.getNumBlocks() + " --- " +
                    STRINGS.TRIAL + " " + mTrialNum + "/" + mBlock.getNumTrials();
            mGraphix.drawString(COLORS.GRAY_900, FONTS.STATUS, stateText,
                    getWidth() - Utils.mm2px(70), Utils.mm2px(10));

            // Draw trace
            final int rad = Trace.TRACE_R;
            for (Point tp : mVisualTrace.getPoints()) {
                mGraphix.fillCircle(COLORS.BLUE_900, new Circle(tp, rad));
            }

            // Temp: show range circle
//            mGraphix.drawRectangle(COLORS.GRAY_400, mTrial.inRect);
//            mGraphix.drawCircle(COLORS.GREEN_700, showCirc);
//            mGraphix.drawLine(COLORS.GREEN_700, mTrial.startLine);

        }
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

            if (dragDist > Utils.mm2px(TunnelTask.DRAG_THRSH_mm)) drag();

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
