package gui;

import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static experiment.Experiment.*;

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

    // Config
    private final boolean mChangeCursor = false;
    private final boolean highlightBar = true;

    // Flags
    private boolean mGrabbed = false;
    private boolean mIsNearBar = false;

    // Shapes
    private Group mGroup;
    private Rectangle mBarRect = new Rectangle();
    private Rectangle mTarRect1 = new Rectangle();
    private Rectangle mTarRect2 = new Rectangle();
    private Rectangle mTarInRect = new Rectangle();

    private Path2D.Double mBarPath = new Path2D.Double();
    private Path2D.Double mTar1Path = new Path2D.Double();
    private Path2D.Double mTar2Path = new Path2D.Double();
    private Path2D.Double mTarInPath = new Path2D.Double();

    // Other
    private Point mGrabPos = new Point();
    private Experiment.DIRECTION mDir;
    private Dimension mDim;

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

    @Override
    public void start() {

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        firstMove = false;

        mDir = Experiment.DIRECTION.random();

        mGroup = new Group(
                Utils.mm2px(BAR_W_mm), Utils.mm2px(BAR_L_mm),
                Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_L_mm),
                Utils.mm2px(TARGET_D_mm), mDir, Utils.mm2px(DIST_mm));

        Out.d(TAG, mDir);

        if (mGroup.position() == 0) {
            mGroup.translateToPanel();

            repaint();
        } else {
            Out.e(NAME, "Couldn't find suitable position!");
        }

    }

    @Override
    public void grab() {
        if (mIsNearBar) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    @Override
    public void release() {
        if (mGrabbed) {
            if (isHit()) {
                Consts.SOUNDS.playHit();
            } else {
                Consts.SOUNDS.playMiss();
            }

            mGrabbed = false;

            // Wait a certain delay, then show the next trial
            executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);

            Out.d(NAME, (Utils.nowMillis() - t0) / 1000.0);
        }
    }

    @Override
    public boolean isHit() {
        boolean result = true;
        double[] coords = new double[2];
        PathIterator pi = mBarPath.getPathIterator(null);
        while (!pi.isDone()) {
            pi.currentSegment(coords);
            result &= mTarInPath.contains(coords[0], coords[1]);
            pi.next();
        }

        return result;
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

//        final Stroke oldStroke = g2d.getStroke();
//        final float newStroke = 3;
//        g2d.setStroke(new BasicStroke(newStroke));

        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.fill(mGroup.tgt1Path);
        g2d.fill(mGroup.tgt2Path);

        if (mIsNearBar && highlightBar) g2d.setColor(Consts.COLORS.GREEN_A400);
        else g2d.setColor(Consts.COLORS.BLUE_900);

        g2d.fill(mGroup.barPath);
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

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }

    // -------------------------------------------------------------------------------------------
    // Group of things to show!
    private class Group {
        public Rectangle mBarRect = new Rectangle();
        public Rectangle mTgt1Rect = new Rectangle();
        public Rectangle mTgt2Rect = new Rectangle();

        public Rectangle mTgtRoomRect = new Rectangle();

        private Rectangle mCircumRect = new Rectangle();

        public Path2D.Double barPath = new Path2D.Double();
        public Path2D.Double tgt1Path = new Path2D.Double();
        public Path2D.Double tgt2Path = new Path2D.Double();
        public Path2D.Double tgtRoomPath = new Path2D.Double();

        public DIRECTION mDir;
        public int mBTDist;

        /**
         * Constructor
         * (All units in px)
         * @param barW Bar Width
         * @param barL Bar Length
         * @param tgtW Target Width
         * @param tgtL Target Length
         * @param tgtD Target Dist
         * @param dir DIRECTION
         * @param barTgtD Distance bet. center of the bar and the middle of targets
         */
        public Group(int barW, int barL, int tgtW, int tgtL, int tgtD, DIRECTION dir, int barTgtD) {
            mBarRect.setSize(barL, barW);
            mTgt1Rect.setSize(tgtL, tgtW);
            mTgt2Rect.setSize(tgtL, tgtW);

            mTgtRoomRect.setSize(tgtL, tgtD);

            mCircumRect.setSize(tgtL, barW + barTgtD + (2 * tgtW) + tgtD);

            mDir = dir;
            mBTDist = barTgtD;
        }

        public int position() {

            // Dimension of the display frame (in px)
            final int dispW = getDispDim().width;
            final int dispH = getDispDim().height;

            // Check if the longest distance fits the height of display
            final int diagDist = (int) sqrt(pow(mCircumRect.width, 2) + pow(mCircumRect.height, 2));
            if (diagDist >= getDispDim().height) {
                return 1;
            }

            // Assume a point of origin (lower left corner of the envelope rectangle), then position accord.
            final int oX = Utils.randInt(diagDist, dispW - diagDist);
            final int oY = Utils.randInt(diagDist, dispH - diagDist);

            mCircumRect.setLocation(oX, oY - mCircumRect.height);

            positionElements(oX, oY);

            // Rotate based on the Direction
            int deg = 0;
            switch (mDir) {
                case N -> deg = 0;
                case NE -> deg = 45;
                case E -> deg = 90;
                case SE -> deg = 135;
                case S -> deg = 180;
                case SW -> deg = 225;
                case W -> deg = 270;
                case NW -> deg = 315;
            }

            AffineTransform transform = new AffineTransform();
            transform.rotate(toRadians(deg), oX, oY);

            barPath = new Path2D.Double(mBarRect, transform);
            tgt1Path = new Path2D.Double(mTgt1Rect, transform);
            tgt2Path = new Path2D.Double(mTgt2Rect, transform);
            tgtRoomPath = new Path2D.Double(mTgtRoomRect, transform);

            return 0;
        }

        /**
         * Position elements based on the origing point (LL of circumRect)
         * @param oX Origin point X
         * @param oY Origing point Y
         */
        private void positionElements(int oX, int oY) {

            int diffDist = (mTgt1Rect.width - mBarRect.width) / 2;

            mBarRect.setLocation(oX + diffDist, oY - mBarRect.height);
            mTgt1Rect.setLocation(oX, oY - mCircumRect.height);
            mTgtRoomRect.setLocation(oX, mTgt1Rect.y + mTgt1Rect.height);
            mTgt2Rect.setLocation(oX, mTgtRoomRect.y + mTgtRoomRect.height);
        }

        public void translateToPanel() {
            final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
            final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

            AffineTransform transform = new AffineTransform();
            transform.translate(lrMargin, tbMargin);

            barPath.transform(transform);
            tgt1Path.transform(transform);
            tgt2Path.transform(transform);
            tgtRoomPath.transform(transform);


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
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;

            AffineTransform transform = new AffineTransform();
            transform.translate(dX, dY);

            mGroup.barPath.transform(transform);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (!firstMove) t0 = Utils.nowMillis();

        // When the cursor gets near the bar
        mIsNearBar = mGroup.barPath.contains(e.getPoint());
        if (mIsNearBar) {
            if (mChangeCursor) setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            if (mChangeCursor) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;

            AffineTransform transform = new AffineTransform();
            transform.translate(dX, dY);

            mGroup.barPath.transform(transform);

            mGrabPos = e.getPoint();
//            mouseDragged(e);
        }

        repaint();

    }


}
