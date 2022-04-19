package gui;

import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import static java.lang.Math.*;

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

    // Config
    private final boolean changeCursor = false;
    private final boolean highlightBar = true;

    // Flags
    private boolean mGrabbed = false;
    private boolean isNearBar = false;

    // Shapes
    private Rectangle mBarRect = new Rectangle();
    private Rectangle mTarRect1 = new Rectangle();
    private Rectangle mTarRect2 = new Rectangle();
    private Rectangle mTarInRect = new Rectangle();

    private Path2D.Double mBarPath = new Path2D.Double();
    private Path2D.Double mTar1Path = new Path2D.Double();
    private Path2D.Double mTar2Path = new Path2D.Double();
    private Path2D.Double mTarInPath = new Path2D.Double();

    private Line mBar = new Line();
    private Line mTargetLine1 = new Line();
    private Line mTargetLine2 = new Line();
    private Line2D.Double mLine = new Line2D.Double(100, 200, 600, 600);

    // Other
    private Point mGrabPos = new Point();
    private Experiment.DIRECTION mDir;
    private Dimension mDim;

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
        mBar.setTol(Utils.mm2px(BAR_GRAB_TOL_mm));

        mBarRect.height = Utils.mm2px(BAR_W_mm);
        mBarRect.width = Utils.mm2px(BAR_L_mm);

        mTarRect1.height = Utils.mm2px(TARGET_W_mm);
        mTarRect1.width = Utils.mm2px(TARGET_L_mm);

        mTarRect2.height = Utils.mm2px(TARGET_W_mm);
        mTarRect2.width = Utils.mm2px(TARGET_L_mm);

        mTarInRect.height = Utils.mm2px(TARGET_D_mm);
        mTarInRect.width = Utils.mm2px(TARGET_L_mm);

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = Experiment.DIRECTION.random();
//        randBarTarget();
        positionBarTargets();
        translateToPanel();
        Out.d(TAG, mDir);

        repaint();
    }

    private void positionBarTargets() {
        String TAG = NAME + "positionBarTargets";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Lengths
        final int barL = Utils.mm2px(BAR_L_mm);
        final int barW = Utils.mm2px(BAR_W_mm);
        final int tarL = Utils.mm2px(TARGET_L_mm);
        final int tarW = Utils.mm2px(TARGET_W_mm);

        final int dist = Utils.mm2px(DIST_mm);
        final int tarDist = Utils.mm2px(TARGET_D_mm);

        // Check if the longest distance fits the height of display
        final int diagDist = (int) (sqrt(pow(tarL, 2) + pow(dist + tarDist, 2)));
        if (diagDist >= dispH) {
            Out.d(TAG, "Can't fit the setup in the frame!");
            return;
        }

        // Assume a point of origin (bottom left of the envelope rectangle)
        final int oX = Utils.randInt(diagDist, dispW - diagDist);
        final int oY = Utils.randInt(diagDist, dispH - diagDist);

        // Place the lines based on the origin (as if it is N)
        mBarRect.setLocation(oX + (tarL - barL) / 2, oY - barW);
        mTarRect1.setLocation(oX, oY - barW - dist - tarDist - 2 * tarW);
        mTarInRect.setLocation(oX, oY - barW - dist - tarW - tarDist);
        mTarRect2.setLocation(oX, oY - barW - dist - tarW);

        // Rotate the lines based on the direction
        int deg = 0;
        switch (mDir) {
            case N -> deg = 0;
            case NE -> deg = 45;
            case E -> deg = 90;
            case SE -> deg = 135;
            case S -> deg = 180;
            case SW -> deg = 225;
            case W -> deg = 270;
            case NW -> deg = 325;
        }

        AffineTransform transform = new AffineTransform();
        transform.rotate(toRadians(deg), oX, oY);
        mBarPath = new Path2D.Double(mBarRect, transform);
        mTar1Path = new Path2D.Double(mTarRect1, transform);
        mTar2Path = new Path2D.Double(mTarRect2, transform);
        mTarInPath = new Path2D.Double(mTarInRect, transform);
    }

    /**
     * Get the randomly-positioned Object and Target
     */
    private void randBarTarget() {
        String TAG = NAME + "randBarTarget";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Lengths
        final int barLen = Utils.mm2px(BAR_L_mm);
        final int tarLen = Utils.mm2px(TARGET_L_mm);
        final int dist = Utils.mm2px(DIST_mm);
        final int tarDist = Utils.mm2px(TARGET_D_mm);
        Out.d(TAG, barLen, tarLen);

        // Check if the longest distance fits the height of display
        final int diagDist = (int) (sqrt(pow(tarLen, 2) + pow(dist + tarDist, 2)));
        if (diagDist >= dispH) {
            Out.d(TAG, "Can't fit the setup in the frame!");
            return;
        }

        // Assume a point of origin (bottom left of the envelope rectangle)
        Out.d(TAG, diagDist, dispW, dispH);
        final int oX = Utils.randInt(diagDist, dispW - diagDist);
        final int oY = Utils.randInt(diagDist, dispH - diagDist);

        // Place the lines based on the origin (as if it is N)
        mBar.setP1(oX + (tarLen - barLen) / 2, oY);
        mBar.setP2(mBar.x1 + barLen, oY);

        mTargetLine1.setP1(oX, oY - (dist - tarDist / 2));
        mTargetLine1.setP2(oX + tarLen, mTargetLine1.y1);

        mTargetLine2.setP1(oX, oY - (dist + tarDist / 2));
        mTargetLine2.setP2(oX + tarLen, mTargetLine2.y1);

        Out.d(TAG, mBar, mTargetLine1, mTargetLine2);

        // Rotate the lines based on the direction
        int deg = 0;
        switch (mDir) {
            case N -> deg = 0;
            case NE -> deg = 45;
            case E -> deg = 90;
            case SE -> deg = 135;
            case S -> deg = 180;
            case SW -> deg = 225;
            case W -> deg = 270;
            case NW -> deg = 325;
        }

        mBar.rotate(oX, oY, deg);
        mTargetLine1.rotate(oX, oY, deg);
        mTargetLine2.rotate(oX, oY, deg);

    }

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

//        mBar.translate(lrMargin, tbMargin);
//        mTargetLine1.translate(lrMargin, tbMargin);
//        mTargetLine2.translate(lrMargin, tbMargin);
        AffineTransform transform = new AffineTransform();
        transform.translate(lrMargin, tbMargin);

        mBarPath.transform(transform);
        mTar1Path.transform(transform);
        mTar2Path.transform(transform);
        mTarInPath.transform(transform);
    }

    public void grab() {
        if (isNearBar) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    public void release() {
        if (mGrabbed) {
            if (isSuccessful()) {
                Consts.SOUNDS.playHit();
            } else {
                Consts.SOUNDS.playMiss();
            }

            mGrabbed = false;
            showTrial();
        }
    }

    @Override
    public boolean isSuccessful() {
//        double[] coords = new double[6];
//        PathIterator pi = mTar2Path.getPathIterator(null);
//        while (!pi.isDone()) {
//            pi.currentSegment(coords);
//            pi.next();
//            Out.d(NAME, Arrays.toString(coords));
//        }
//
//        Out.d(NAME, "-----------------");

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

        final Stroke oldStroke = g2d.getStroke();
        final float newStroke = 3;
        g2d.setStroke(new BasicStroke(newStroke));

        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.fill(mTar1Path);
        g2d.fill(mTar2Path);
//        g2d.drawLine(mTargetLine1.x1, mTargetLine1.y1, mTargetLine1.x2, mTargetLine1.y2);
//        g2d.drawLine(mTargetLine2.x1, mTargetLine2.y1, mTargetLine2.x2, mTargetLine2.y2);

//        g2d.drawLine(mBar.x1, mBar.y1, mBar.x2, mBar.y2);

        if (isNearBar && highlightBar) g2d.setColor(Consts.COLORS.GREEN_A400);
        else g2d.setColor(Consts.COLORS.BLUE_900);

//        final Path2D.Double rect = mBar.getBoundRect(Utils.mm2px(BAR_GRAB_TOL_mm));
//        final Rectangle rect = mBar.getBoundRect(Utils.mm2px(BAR_GRAB_TOL_mm));
//        rect.translate(Utils.mm2px(LR_MARGIN_mm), Utils.mm2px(TB_MARGIN_mm));
        g2d.fill(mBarPath);
//        g2d.draw(rect);
//        g2d.drawRect((int) rect.getMinX(), (int) rect.getMinY(), rect.width, rect.height);
//        Out.d(TAG, rect);
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
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        grab();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        release();
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
//            mBar.translate(dX, dY);
            AffineTransform transform = new AffineTransform();
            transform.translate(dX, dY);
            mBarPath.transform(transform);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        // When the cursor gets near the bar
//        isNearBar = mBar.isNear(e.getPoint(), Utils.mm2px(BAR_GRAB_TOL_mm));
        isNearBar = mBarPath.contains(e.getPoint());
        if (isNearBar) {
            if (changeCursor) setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
        } else {
            if (changeCursor) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (mGrabbed) {
            mGrabPos = e.getPoint();

            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mBar.translate(dX, dY);
        }

        repaint();
    }
}
