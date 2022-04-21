package gui;

import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import static java.lang.Math.*;

public class TunnelTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TunnelTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final double OBJECT_R_mm = 3; // Radius of the object
    private final double DIST_mm = 5; // Distance from the center of the object to the side (l/R) of target lines
    private final double TARGET_L_mm = 50; // Lneght of the target lines (> bar L)
    private final double TARGET_W_mm = 1; // Targets width
    private final double TARGET_D_mm = 15; // Perpendicular distance betw. the target lines (> bar L)

    // Flags
    private boolean mGrabbed = false;
    private boolean isInsideObj = false;
    private boolean highlightObj = false;
    private boolean objEntered = false;

    // Shapes
    private Rectangle mTar1Rect = new Rectangle();
    private Rectangle mTar2Rect = new Rectangle();
    private Rectangle mTarInRect = new Rectangle();
    private Circle mObject = new Circle();

    private Path2D.Double mTar1Path = new Path2D.Double();
    private Path2D.Double mTar2Path = new Path2D.Double();
    private Path2D.Double mTarInPath = new Path2D.Double();
    private Shape mObjShape;

    // Other
    private Point mGrabPos = new Point();
    private Experiment.DIRECTION mDir;

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

        mObject.setRadius(Utils.mm2px(OBJECT_R_mm));

        mTar1Rect.setSize(Utils.mm2px(TARGET_L_mm), Utils.mm2px(TARGET_W_mm));
        mTar2Rect.setSize(Utils.mm2px(TARGET_L_mm), Utils.mm2px(TARGET_W_mm));
        mTarInRect.setSize(Utils.mm2px(TARGET_L_mm), Utils.mm2px(TARGET_D_mm));

        showTrial();
    }


    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = Experiment.DIRECTION.random();
        positionObjectTargets();
//        translateToPanel();
        Out.d(TAG, mDir);

        repaint();
    }

    private void positionObjectTargets() {
        String TAG = NAME + "positionObjectTargets";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Lengths
        final int objR = Utils.mm2px(OBJECT_R_mm);
        final int dist = Utils.mm2px(DIST_mm);
        final int tarL = Utils.mm2px(TARGET_L_mm);
        final int tarW = Utils.mm2px(TARGET_W_mm);
        final int tarDist = Utils.mm2px(TARGET_D_mm);

        final int objDiam = 2 * objR;
        final int halfTarDist = tarDist / 2;

        // Check if the longest distance fits the height of display
        final int maxDist = (int) ((tarL + 2 * dist + 2 * objR) * sqrt(2)); // consider obj on both sides
        Out.d(TAG, "Fitting...", maxDist, dispH);
        if (maxDist >= dispH) {
            Out.d(TAG, "Can't fit the setup in the frame!");
            return;
        }

        // Assume a point of origin (bottom left of the envelope rectangle)
        final int oX = Utils.randInt(maxDist, dispW - maxDist);
        final int oY = Utils.randInt(maxDist, dispH - maxDist);

        // Place the lines based on the origin (as if it is N)
        mObject.setCenter(oX, oY);
        mTar1Rect.setLocation(oX + dist, oY - halfTarDist - tarW);
        mTarInRect.setLocation(oX + dist, oY - halfTarDist);
        mTar2Rect.setLocation(oX + dist, oY + halfTarDist);
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
        mTar1Path = new Path2D.Double(mTar1Rect, transform);
        mTar2Path = new Path2D.Double(mTar2Rect, transform);
        mTarInPath = new Path2D.Double(mTarInRect, transform);
    }

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

        AffineTransform transform = new AffineTransform();
        transform.translate(lrMargin, tbMargin);

        mTar1Path.transform(transform);
        mTar2Path.transform(transform);
        mTarInPath.transform(transform);

        mObject.translate(lrMargin, tbMargin);
    }

    public void grab() {
        if (mObject.contains(getCursorPos())) {
            Out.d(NAME, "inside");
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
        return !mTarInPath.contains(mObject.getBounds());
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Consts.COLORS.GRAY_500);
        g2d.fill(mTar1Path);
        g2d.fill(mTar2Path);

        if (isInsideObj && highlightObj) g2d.setColor(Consts.COLORS.GREEN_A400);
        else g2d.setColor(Consts.COLORS.BLUE_900);

        g2d.fillOval(mObject.getX(), mObject.getY(), mObject.getR(), mObject.getR());
    }

    private void printPath(Path2D.Double path) {
        final String TAG = NAME + "printPath";
        Out.d(TAG, "Printing Path...");
        double[] coords = new double[4];
        PathIterator pi = path.getPathIterator(null);
        Out.d(TAG, pi.isDone());
        while(!pi.isDone()) {
            pi.currentSegment(coords);
            Out.d(TAG, Arrays.toString(coords));
            pi.next();
        }
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

            mObject.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();

            if (mTar1Path.intersects(mObject.getBounds()) || mTar2Path.intersects(mObject.getBounds())) {
                Consts.SOUNDS.playMiss();
                mGrabbed = false;
                objEntered = false;

                showTrial();
            }

            if (!objEntered) {
                if (mTarInPath.contains(mObject.getBounds())) {
                    objEntered = true;
                }
            }

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
