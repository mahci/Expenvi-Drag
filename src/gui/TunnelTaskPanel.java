package gui;

import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final double DIST_mm = 5; // Distance from the center of the object to the side (l/R) of target lines

    private final double START_AREA_L_mm = 17; // Lneght of the Start Area
    private final double TARGET_L_mm = 100; // Lneght of the Target lines (> bar L)
    private final double TARGET_W_mm = 1; // Targets width
    private final double TARGET_D_mm = 15; // Perpendicular distance betw. the target lines (> bar L)

    private final double DRAG_THRSH_mm = 5; // Movement > threshold => Dragging starts
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial

    // Flags
    private boolean mGrabbed = false;
    private boolean isInsideObj = false;
    private boolean highlightObj = false;
    private boolean mEntered = false;
    private boolean mMissed = false;
    private boolean mDragBegan = false;

    // Shapes
    private Rectangle mStartAreaRect = new Rectangle();
    private Rectangle mTar1Rect = new Rectangle();
    private Rectangle mTar2Rect = new Rectangle();
    private Rectangle mTarInRect = new Rectangle();
    private Circle mObject = new Circle();

    private Path2D.Double mStartAreaPath = new Path2D.Double();
    private Path2D.Double mTar1Path = new Path2D.Double();
    private Path2D.Double mTar2Path = new Path2D.Double();
    private Path2D.Double mTarInPath = new Path2D.Double();

    // Measurements in px
    private final int DIST = Utils.mm2px(DIST_mm);
    private final int TGT_L = Utils.mm2px(TARGET_L_mm);
    private final int TGT_W = Utils.mm2px(TARGET_W_mm);
    private final int TGT_D = Utils.mm2px(TARGET_D_mm);
    private final int SA_L = Utils.mm2px(START_AREA_L_mm);
    private final int DRAG_THSH = Utils.mm2px(DRAG_THRSH_mm);
    private final int SA_W = TGT_D + 2 * TGT_W;
    private final int TRACE_R = 1;

    // Other
    private Point mLastGrabPos = new Point();
    private Experiment.DIRECTION mDir;
    private ArrayList<Point> trace = new ArrayList<>();

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
    public TunnelTaskPanel(Dimension dim) {
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
        super.start();

        mStartAreaRect.setSize(SA_L, SA_W);
        mTar1Rect.setSize(TGT_L, TGT_W);
        mTar2Rect.setSize(TGT_L, TGT_W);
        mTarInRect.setSize(TGT_L, TGT_D);

        showTrial();
    }


    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mMissed = false;
        mDragBegan = false;

        mDir = Experiment.DIRECTION.random();
        positionRandomly();
        translateToPanel();
        Out.d(TAG, mDir);

        repaint();
    }

    private void positionRandomly() {
        String TAG = NAME + "randPosition";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

//        final int objDiam = 2 * objR;
//        final int halfTarDist = tarD / 2;

        // Check if the longest distance fits the height of display
        final int maxDist = SA_L + TGT_L;
        Out.d(TAG, "Fitting...", maxDist, dispH);
        if (maxDist >= dispH) {
            Out.d(TAG, "Can't fit the setup in the frame!");
            return;
        }

        // Assume a point of origin (for rotation and ref.)
        final int oX = Utils.randInt(maxDist, dispW - maxDist);
        final int oY = Utils.randInt(maxDist, dispH - maxDist);

        // Place the lines based on the origin (as if it is N)
        mStartAreaRect.setLocation(oX, oY - SA_W / 2);
//        mObject.setCenter(oX, oY);
        mTar1Rect.setLocation(oX + SA_L + 10, oY - SA_W / 2);
        mTar2Rect.setLocation(oX + SA_L + 10, oY + TGT_D / 2);
        mTarInRect.setLocation(oX + SA_L + 10, oY - TGT_D / 2);
        Out.d(TAG, oX, oY, mObject, mTar1Rect);
        // Rotate the lines based on the direction
        int deg = 0;
        switch (mDir) {
            case E -> deg = 0;
            case NE -> deg = 45;
            case N -> deg = 90;
            case NW -> deg = 135;
            case W -> deg = 180;
            case SW -> deg = 225;
            case S -> deg = 270;
            case SE -> deg = 315;
        }

        AffineTransform transform = new AffineTransform();
        transform.rotate(toRadians(deg), oX, oY);
        mStartAreaPath = new Path2D.Double(mStartAreaRect, transform);
        mTar1Path = new Path2D.Double(mTar1Rect, transform);
        mTar2Path = new Path2D.Double(mTar2Rect, transform);
        mTarInPath = new Path2D.Double(mTarInRect, transform);
    }

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

        AffineTransform transform = new AffineTransform();
        transform.translate(lrMargin, tbMargin);

        mStartAreaPath.transform(transform);
        mTar1Path.transform(transform);
        mTar2Path.transform(transform);
        mTarInPath.transform(transform);

//        mObject.translate(lrMargin, tbMargin);
    }

    @Override
    public void grab() {
        if (mStartAreaPath.contains(getCursorPos())) {
            mGrabbed = true;
            mLastGrabPos = getCursorPos();
        }
    }

    @Override
    public void release() {
        if (mDragBegan) {
            if (isHit()) {
                hit();
            } else {
                miss();
            }
        }
    }

    @Override
    public boolean isHit() {
        return !mMissed && mGrabbed && mEntered && !mTarInPath.contains(getCursorPos());
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
        Consts.SOUNDS.playHit();
        mGrabbed = false;
        mEntered = false;
        mMissed = true;

        trace.clear();

        // Wait a certain delay, then show the next trial
        executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);
    }

    private void miss() {
        Consts.SOUNDS.playMiss();
        mGrabbed = false;
        mEntered = false;
        mMissed = true;
        mDragBegan = false;

        trace.clear();

        // Wait a certain delay, then show the next trial
        executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);
    }

    private boolean traceIntersect() {
        Line2D seg;
        for (int i = 1; i < trace.size(); i++) {
            seg = new Line2D.Double(trace.get(i - 1), trace.get(i));
            if (Utils.intersects(mTar1Path, seg) || Utils.intersects(mTar2Path, seg)) return true;
        }

        return false;
    }

    // -------------------------------------------------------------------------------------------
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

        // Draw trace
        g2d.setColor(Consts.COLORS.BLUE_900);
        for (Point tp : trace) {
            g2d.fillOval(tp.x - TRACE_R, tp.y - TRACE_R, TRACE_R * 2, TRACE_R * 2);
        }

        // Draw Targets
        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.fill(mTar1Path);
        g2d.fill(mTar2Path);

        // Draw Start Area
        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.draw(mStartAreaPath);
        g2d.setColor(Consts.COLORS.BLUE_900);
        g2d.fill(mStartAreaPath);
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

            final Point curP = e.getPoint();

            final int dX = curP.x - mLastGrabPos.x;
            final int dY = curP.y - mLastGrabPos.y;

//            mLastGrabPos = curP;

            final double dragDist = sqrt(pow(dX, 2) + pow(dY, 2));
            mDragBegan = dragDist > DRAG_THSH;

            if (mDragBegan) {
                // Add cursor point to the trace
                trace.add(curP);

                if (mTar1Path.contains(curP) || mTar2Path.contains(curP) || traceIntersect()) {
                    miss();
                } else {
                    if (mEntered) {
                        if (mStartAreaPath.contains(curP)) miss();
                    } else {
                        if (mTarInPath.contains(curP)) mEntered = true;
                    }

                    repaint();
                }
            }

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseDragged(e);
    }
}
