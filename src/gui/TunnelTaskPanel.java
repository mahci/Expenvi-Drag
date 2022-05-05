package gui;

import experiment.Experiment;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static experiment.Experiment.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
//    private final double START_AREA_L_mm = 17; // Lneght of the Start Area
    private final double DIST_mm = 150; // Lneght of the Target lines (> bar L)

    private final double LINES_W_mm = 1; // Targets width
    private final double TUNNEL_W_mm = 5; // Perpendicular distance betw. the target lines (> bar L)

    private final double TEXT_RECT_W_mm = 8; // Width of the start text rectangle
    private final double TEXT_RECT_H_mm = 8; // Height of the start text rectangle

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

    private Group mGroup;

    // Measurements in px
    private final int DIST = Utils.mm2px(DIST_mm);
    private final int TGT_L = Utils.mm2px(DIST_mm);
    private final int TGT_W = Utils.mm2px(LINES_W_mm);
    private final int TGT_D = Utils.mm2px(TUNNEL_W_mm);
    private final int DRAG_THSH = Utils.mm2px(DRAG_THRSH_mm);
    private final int SA_W = TGT_D + 2 * TGT_W;
    private final int TRACE_R = 1;

    // Other
    private Point mLastGrabPos = new Point();
    private Experiment.DIRECTION mDir;
    private ArrayList<Point> mTrace = new ArrayList<>();

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

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mMissed = false;
        mDragBegan = false;

        mDir = DIRECTION.randStraight();
        Out.d(TAG, mDir);

        mGroup = new Group(
                Utils.mm2px(LINES_W_mm), Utils.mm2px(TUNNEL_W_mm),
                Utils.mm2px(DIST_mm),
                Utils.mm2px(TEXT_RECT_W_mm), Utils.mm2px(TEXT_RECT_H_mm), mDir);

        if (mGroup.position(getCursorPos(), 500, 1000) == 0) {
            mGroup.translateToPanel();
            repaint();
        } else {
            Out.d(TAG, "No suitable position found!");
        }




    }

    private void positionRandomly() {
        String TAG = NAME + "randPosition";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

//        final int objDiam = 2 * objR;
//        final int halfTarDist = tarD / 2;

        // Check if the longest distance fits the height of display
//        final int maxDist = SA_L + TGT_L;
//        Out.d(TAG, "Fitting...", maxDist, dispH);
//        if (maxDist >= dispH) {
//            Out.d(TAG, "Can't fit the setup in the frame!");
//            return;
//        }

        // Assume a point of origin (for rotation and ref.)
//        final int oX = Utils.randInt(maxDist, dispW - maxDist);
//        final int oY = Utils.randInt(maxDist, dispH - maxDist);
//
//        // Place the lines based on the origin (as if it is N)
//        mStartAreaRect.setLocation(oX, oY - SA_W / 2);
////        mObject.setCenter(oX, oY);
//        mTar1Rect.setLocation(oX + SA_L + 10, oY - SA_W / 2);
//        mTar2Rect.setLocation(oX + SA_L + 10, oY + TGT_D / 2);
//        mTarInRect.setLocation(oX + SA_L + 10, oY - TGT_D / 2);
//        Out.d(TAG, oX, oY, mObject, mTar1Rect);
        // Rotate the lines based on the direction
//        int deg = 0;
//        switch (mDir) {
//            case E -> deg = 0;
//            case NE -> deg = 45;
//            case N -> deg = 90;
//            case NW -> deg = 135;
//            case W -> deg = 180;
//            case SW -> deg = 225;
//            case S -> deg = 270;
//            case SE -> deg = 315;
//        }
//
//        AffineTransform transform = new AffineTransform();
//        transform.rotate(toRadians(deg), oX, oY);
//        mStartAreaPath = new Path2D.Double(mStartAreaRect, transform);
//        mTar1Path = new Path2D.Double(mTar1Rect, transform);
//        mTar2Path = new Path2D.Double(mTar2Rect, transform);
//        mTarInPath = new Path2D.Double(mTarInRect, transform);
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
        mGrabbed = true;
        mLastGrabPos = getCursorPos();
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

        mGrabbed = false;

        Out.d(NAME, (Utils.nowMillis() - t0) / 1000.0);
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

        mTrace.clear();

        // Wait a certain delay, then show the next trial
        executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);
    }

    private void miss() {
        Consts.SOUNDS.playMiss();
        mGrabbed = false;
        mEntered = false;
        mMissed = true;
        mDragBegan = false;

        mTrace.clear();

        // Wait a certain delay, then show the next trial
        executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);
    }

    private boolean traceIntersect() {
        Line2D seg;
        for (int i = 1; i < mTrace.size(); i++) {
            seg = new Line2D.Double(mTrace.get(i - 1), mTrace.get(i));
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
        for (Point tp : mTrace) {
            g2d.fillOval(tp.x - TRACE_R, tp.y - TRACE_R, TRACE_R * 2, TRACE_R * 2);
        }

        // Draw Targets
        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.fill(mGroup.line1Rect);
        g2d.fill(mGroup.line2Rect);

        // Draw text
        g2d.setColor(Consts.COLORS.GREEN_700);
        g2d.drawString("Start", mGroup.startTextRect.x,
                mGroup.startTextRect.y + mGroup.startTextRect.height /2);
//        g2d.drawRect(mGroup.startTextRect.x, mGroup.startTextRect.y,
//                mGroup.startTextRect.width, mGroup.startTextRect.height);

        // Draw Start Area
//        g2d.setColor(Consts.COLORS.GRAY_500);
//        g2d.draw(mStartAreaPath);
//        g2d.setColor(Consts.COLORS.BLUE_900);
//        g2d.fill(mStartAreaPath);
    }

    // -------------------------------------------------------------------------------------------
    // Group of things to show!
    private class Group {
        private Rectangle line1Rect = new Rectangle();
        private Rectangle line2Rect = new Rectangle();
        private Rectangle inRect = new Rectangle();
        private Rectangle startTextRect = new Rectangle();

        private static Rectangle circumRectWE = new Rectangle();
        private static Rectangle circumRectNS = new Rectangle();

        private Rectangle circumRect = new Rectangle();

        private Path2D.Double line1Path = new Path2D.Double();
        private Path2D.Double line2Path = new Path2D.Double();
        private Path2D.Double inPath = new Path2D.Double();

        private int dist;
        private DIRECTION dir;

        private int cbNRows, cbNCols; // Num of rows and cols in the chekerboard
        private int cbSide;

        private static List<Point> circumRectListWE = new ArrayList<>();
        private static List<Point> circumRectListNS = new ArrayList<>();

        private List<Point> circumRectList = new ArrayList<>();

        private static boolean arranged = false;

        /**
         * Create the Group (default is W/E)
         * @param linesW Width of the lines
         * @param tunnelW Width of the tunnel
         * @param dist Distance (= length of the tunnel)
         * @param textW Width of the start text rectangle
         * @param textH Height of the start text rectangle
         */
        public Group(int linesW, int tunnelW, int dist, int textW, int textH, DIRECTION dir) {

            if (!arranged) {
                circumRectWE.setSize(dist, 2 * linesW + tunnelW + textH);
                circumRectNS.setSize(2 * linesW + tunnelW + textH,  dist);
                arrange();
            }

            switch (dir) {
                case W, E -> {
                    line1Rect.setSize(dist, linesW);
                    inRect.setSize(dist, tunnelW);
                    circumRect = circumRectWE;
                    circumRectList = new ArrayList<>(circumRectListWE);
                }

                case N, S -> {
                    line1Rect.setSize(linesW, dist);
                    inRect.setSize(tunnelW, dist);
                    circumRect = circumRectNS;
                    circumRectList = new ArrayList<>(circumRectListNS);
                }
            }

            startTextRect.setSize(textW, textH);
            line2Rect.setSize(line1Rect.getSize());

            this.dist = dist;
            this.dir = dir;

            // Create the checkerboard
//            cbSide = circumRect.width;
//            cbNRows = getDispDim().height / cbSide;
//            cbNCols = getDispDim().width / cbSide;
        }

        private void arrange() {
            // Arrange WE rectangle
            for (int x = 0; x < getDispW() - circumRectWE.width; x += 5) {
                for (int y = 0; y < getDispH() - circumRectWE.height; y += 5) {
                    circumRectListWE.add(new Point(x, y));
                }
            }

            // Arrange NS rectangle
            for (int x = 0; x < getDispW() - circumRectNS.width; x += 5) {
                for (int y = 0; y < getDispH() - circumRectNS.height; y += 5) {
                    circumRectListNS.add(new Point(x, y));
                }
            }

            arranged = true;
        }

        private Pair getRandCBCell(Point refP) {
            final String TAG = NAME + "checkerboard";

            // Which cell refP resides in
            int col = refP.x / cbSide;
            int row = refP.y / cbSide;
            Out.d(TAG, row, col);

            List<Pair> candidCells = new ArrayList<>();
            if (col + 1 <= cbNCols) candidCells.add(new Pair(row, col + 1));
            if (col - 1 >= 0) candidCells.add(new Pair(row, col - 1));
            if (row + 1 <= cbNRows) candidCells.add(new Pair(row + 1, col));
            if (row - 1 >= 0) candidCells.add(new Pair(row - 1, col));

            return candidCells.get(Utils.randInt(0, candidCells.size()));
        }
//
//        private Point getRandPointInCell(Pair cellRC) {
//            Point cellUL = new Point(cellRC.second * cbSide, cellRC.first * cbSide);
//
//            int x, y;
////            x = cellUL.x + Utils.randInt(0, cbSide - circumRect.width);
////            y = cellUL.y + Utils.randInt(0, cbSide - circumRect.height);
//
////            switch (dir) {
////                case W, E -> {
////
////                }
////
////                case N, S -> {
////                    x = cellUL.x;
////                    y = cellUL.y + Utils.randInt(0, circumRect.height);
////                }
////            }
//
//            return new Point(x, y);
//        }

        /**
         * Position the group based on a ref. point (can be last trial's cursor, home, etc.), distance and direction
         * @param refP Reference point (if no ref => null)
         * @return Success (0) or fail (1)
         */
        public int position(Point refP, int minD, int maxD) {
            final String TAG = NAME + "position";

            Point position = new Point();
            for (int d = minD; d < maxD; d += 50) {
                Out.d(TAG, "Distance= " + d);
//                List<Point> tempList = new ArrayList<>(circumRectList);
                Collections.shuffle(circumRectList);
                for (int i = 0; i < circumRectList.size(); i++) {
                    position = circumRectList.get(i);
                    final double pDist = position.distance(refP);
                    circumRect.setLocation(position);
                    Out.d(TAG, position, pDist);
                    if (pDist <= d && pDist >= minD && !circumRect.contains(refP)) {
                        positionElements();
                        return 0;
                    }
                }
            }

//            Pair cbRC = getRandCBCell(refP);
//            Out.d(TAG, cbRC);
//
//            circumRect.setLocation(getRandPointInCell(cbRC));
//            positionElements();

            // Dimension of the display frame (in px)
//            final int dispW = getDispDim().width;
//            final int dispH = getDispDim().height;
//
//            // Assume a point of origin, find MinMax based on refP, rot. based on DIR
//            MinMax xMinMax, yMinMax;
//            Point oP = new Point(-1, -1);
//            switch (dir) {
//                case W, E -> {
//                    xMinMax = new MinMax(0, dispW - circumRect.width);
//                    yMinMax = new MinMax(0, dispH - circumRect.height);
//
//                    if (refP != null) {
//                        final Circle refCircle = new Circle(refP, refD);
//                        final List<Point> rangePoints = refCircle.getPoints();
//                        Out.d(TAG, rangePoints);
//                        while (oP.x == -1 && !rangePoints.isEmpty()) {
//                            Point candP = rangePoints.remove(Utils.randInt(0, rangePoints.size()));
//
//                            if (xMinMax.containsIncl(candP.x) && yMinMax.containsIncl(candP.y)) { // Fits the display?
//                                circumRect.setLocation(candP); // Set the origin point
//
//                                // Set elemnents
//                                positionElements(0);
//                                return 0;
//                            }
//                        }
//                    } else {
//                        circumRect.setLocation(Utils.randInt(xMinMax), Utils.randInt(yMinMax));
//                        // Set elemnents
//                        positionElements(0);
//                        return 0;
//                    }
//
//                    return 1;
//                }
//
//            }

            return 1;
        }

        /**
         * Position elements based on the circumference rectangle and the degree to rotate
         */
        private void positionElements() {

            switch (dir) {
                case W -> {
                    line1Rect.setLocation(circumRect.x, circumRect.y);
                    inRect.setLocation(circumRect.x, circumRect.y + line1Rect.height);
                    line2Rect.setLocation(circumRect.x, inRect.y + inRect.height);
                    startTextRect.setLocation(circumRect.x, line2Rect.y + line2Rect.height);
                }

                case E -> {
                    line1Rect.setLocation(circumRect.x, circumRect.y);
                    inRect.setLocation(circumRect.x, circumRect.y + line1Rect.height);
                    line2Rect.setLocation(circumRect.x, inRect.y + inRect.height);
                    startTextRect.setLocation(
                            circumRect.x + circumRect.width - startTextRect.width,
                            line2Rect.y + line2Rect.height);
                }

                case N -> {
                    startTextRect.setLocation(
                            circumRect.x,
                            circumRect.y + circumRect.height - startTextRect.height);
                    line2Rect.setLocation(
                            startTextRect.x + startTextRect.width,
                            circumRect.y);
                    inRect.setLocation(line2Rect.x + line2Rect.width, circumRect.y);
                    line1Rect.setLocation(inRect.x + inRect.width, circumRect.y);
                }

                case S -> {
                    startTextRect.setLocation(circumRect.x, circumRect.y);
                    line2Rect.setLocation(
                            startTextRect.x + startTextRect.width,
                            circumRect.y);
                    inRect.setLocation(line2Rect.x + line2Rect.width, circumRect.y);
                    line1Rect.setLocation(inRect.x + inRect.width, circumRect.y);

                }
            }

//            line1Rect.setLocation(circumRect.x, circumRect.y);
//            inRect.setLocation(circumRect.x, circumRect.y + line1Rect.height);
//            line2Rect.setLocation(circumRect.x, inRect.y + inRect.height);
//            startTextRect.setLocation(circumRect.x, line2Rect.y + line2Rect.height);
//
//            final AffineTransform transform = new AffineTransform();
//            transform.rotate(toRadians(rotDeg),
//                    circumRect.getCenterX(), circumRect.getCenterY());
//
//            line1Path = new Path2D.Double(line1Rect, transform);
//            line2Path = new Path2D.Double(line2Rect, transform);
//            inPath = new Path2D.Double(inRect, transform);
        }

        /**
         * Translate the group to the panel
         */
        public void translateToPanel() {
            final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
            final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

            circumRect.translate(lrMargin, tbMargin);
            positionElements();
        }

        public boolean linesTouchPoint(Point p) {
            return line1Rect.contains(p) || line2Rect.contains(p);
        }

        public boolean enteredTunnel(Point p) {
            return inRect.contains(p);
        }

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
            mDragBegan = dragDist > Utils.mm2px(DRAG_THRSH_mm);

            if (mDragBegan) {
                // Add cursor point to the trace
                mTrace.add(curP);

                mEntered = mGroup.enteredTunnel(curP);

                if (mGroup.linesTouchPoint(curP) || traceIntersect()) {
                    miss();
                } else {
//                    if (mEntered) {
//                        if (mStartAreaPath.contains(curP)) miss();
//                    } else {
//                        if (mTarInPath.contains(curP)) mEntered = true;
//                    }

                    repaint();
                }
            }

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!firstMove) t0 = Utils.nowMillis();

        mouseDragged(e);
    }
}
