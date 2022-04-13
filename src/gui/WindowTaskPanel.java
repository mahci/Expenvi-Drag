package gui;

import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static experiment.Experiment.*;

import static tools.Consts.COLORS;

public class WindowTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "TestPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final int OBJECT_W_mm = 20; // Object width (always square)
    private final int TARGET_W_mm = 100; // Window width (always squeate)
    private final int DIST_mm = 150; // Distance from center of object to the center of the window

    // Flags
    private boolean mGrabbed = false;

    // Shapes
    private Rectangle mObject = new Rectangle();
    private Rectangle mTarget = new Rectangle();

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
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
    public WindowTaskPanel(Dimension dim) {
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

        mDir = DIRECTION.random();
        randObjTgt();

        repaint();
    }

    /**
     * Get the randomly-positioned Object and Target
     */
    private void randObjTgt() {
        String TAG = NAME + "randObjTgt";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Distance in px
        final int dist = Utils.mm2px(DIST_mm);
        final int sideDist = (int) (dist / Math.sqrt(2));

        // Rectangles (squares) in the display frame (after margins)
        Rectangle objDispRect = new Rectangle(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));
        Rectangle tgtDispRect = new Rectangle(Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_W_mm));

        Out.d(TAG, dispH,  -tgtDispRect.height / 2, - dist, - tgtDispRect.height / 2);
        // Random positions based on the direction
        switch (mDir) {
            case N -> {
                tgtDispRect.x = Utils.randInt(0, dispW - tgtDispRect.width);
                tgtDispRect.y = Utils.randInt(0,
                        dispH - tgtDispRect.width / 2 - dist - tgtDispRect.width / 2);

                Out.d(TAG, tgtDispRect);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX - objDispRect.width / 2;
                objDispRect.y = tgtCentY + dist - objDispRect.width / 2;
            }

            case S -> {
                tgtDispRect.x = Utils.randInt(0, dispW - tgtDispRect.width); // Same as N
                tgtDispRect.y = Utils.randInt(objDispRect.width / 2 + dist - tgtDispRect.width / 2,
                        dispH - tgtDispRect.width);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX - objDispRect.width / 2;
                objDispRect.y = tgtCentY - dist - objDispRect.width / 2; // Only diff. than N
            }

            case W -> {
                tgtDispRect.x = Utils.randInt(0,
                        dispW - objDispRect.width / 2 - dist - tgtDispRect.width / 2);
                tgtDispRect.y = Utils.randInt(0, dispH - tgtDispRect.width);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX + dist - objDispRect.width / 2;
                objDispRect.y = tgtCentY - objDispRect.width / 2;
            }

            case E -> {
                tgtDispRect.x = Utils.randInt(objDispRect.width / 2 + dist - tgtDispRect.width / 2,
                        dispW - tgtDispRect.width);
                tgtDispRect.y = Utils.randInt(0, dispH - tgtDispRect.width); // Same as W

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX - dist - objDispRect.width / 2;
                objDispRect.y = tgtCentY - objDispRect.width / 2;
            }

            // From here on, dist is replaced with sideDist
            case NE -> {
                tgtDispRect.x = Utils.randInt(objDispRect.width / 2 + sideDist - tgtDispRect.width / 2,
                        dispW - tgtDispRect.width);
                tgtDispRect.y = Utils.randInt(0,
                        dispH - objDispRect.width / 2 - sideDist - tgtDispRect.width / 2);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX - sideDist - objDispRect.width / 2;
                objDispRect.y = tgtCentY - objDispRect.width / 2;
            }

            case NW -> {
                tgtDispRect.x = Utils.randInt(0,
                        dispW - objDispRect.width / 2 - sideDist - tgtDispRect.width / 2);
                tgtDispRect.y = Utils.randInt(0,
                        dispH - objDispRect.width / 2 - sideDist - tgtDispRect.width / 2);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX + sideDist - objDispRect.width / 2;
                objDispRect.y = tgtCentY + sideDist - objDispRect.width / 2;
            }

            case SE -> {
                tgtDispRect.x = Utils.randInt(objDispRect.width / 2 + sideDist - tgtDispRect.width / 2,
                        dispW - tgtDispRect.width);
                tgtDispRect.y = Utils.randInt(objDispRect.width / 2 + sideDist - tgtDispRect.width / 2,
                        dispH - tgtDispRect.width);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX - sideDist - objDispRect.width / 2;
                objDispRect.y = tgtCentY - sideDist - objDispRect.width / 2;
            }

            case SW -> {
                tgtDispRect.x = Utils.randInt(0,
                        dispW - objDispRect.width / 2 - sideDist - tgtDispRect.width / 2);
                tgtDispRect.y = Utils.randInt(objDispRect.width / 2 + sideDist - tgtDispRect.width / 2,
                        dispH - tgtDispRect.width);

                final int tgtCentX = (int) tgtDispRect.getCenterX();
                final int tgtCentY = (int) tgtDispRect.getCenterY();

                objDispRect.x = tgtCentX + sideDist - objDispRect.width / 2;
                objDispRect.y = tgtCentY - sideDist - objDispRect.width / 2;
            }
        }

        // Translate to whole panel coordinates
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);
        objDispRect.translate(lrMargin, tbMargin);
        tgtDispRect.translate(lrMargin, tbMargin);
        Out.d(TAG, tgtDispRect);
        mObject = objDispRect;
        mTarget = tgtDispRect;
    }

    public void grab() {
        if (mObject.contains(getCursorPos())) {
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
        return mTarget.contains(getCursorPos());
    }

    // -------------------------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final float newThickness = 3;

        // Draw Window (should be drawn behind the object)
        g2d.setColor(COLORS.GRAY_500);
//        g2d.setStroke(new BasicStroke(newThickness));
        g2d.fill(mTarget);
//        g2d.setStroke(oldStroke);

        // Draw target
        g2d.setColor(COLORS.BLUE_900_ALPHA);
        g2d.fill(mObject);
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
    public void mouseDragged(MouseEvent e) {
        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mObject.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mGrabbed) {
            mGrabPos = e.getPoint();

            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mObject.translate(dX, dY);

            repaint();
        }
    }

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
}
