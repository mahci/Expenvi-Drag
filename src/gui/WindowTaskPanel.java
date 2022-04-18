package gui;

import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static experiment.Experiment.*;

import static tools.Consts.COLORS;

public class WindowTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "WindowTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final double OBJECT_W_mm = 20; // Object width (always square)
    private final double TARGET_W_mm = 60; // Window width (always squeate)
    private final double DIST_mm = 150; // Distance from center of object to the center of the window
    private final long DROP_DELAY_ms = 700; // Delay before showing the next trial

    // Flags
    private boolean mGrabbed = false;

    // Shapes
    private Rectangle mObject = new Rectangle();
    private Rectangle mTarget = new Rectangle();
    private final MoPanel mTargetPnl = new MoPanel();
    private final MoLabel mObjectLbl = new MoLabel();
    private AlphaContainer alphaContain;

    // Other
    private Point mGrabPos = new Point();
    private DIRECTION mDir;
    private Dimension mDim;

    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

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
        mTargetPnl.setSize(Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_W_mm));
        BevelBorder bord = new BevelBorder(BevelBorder.LOWERED);
        mTargetPnl.setBorder(bord);

//        mObjectLbl.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));
////        mObjectLbl.setOpaque(true);
//        mObjectLbl.setBackground(COLORS.BLUE_900_ALPHA);
//        alphaContain = new AlphaContainer(mObjectLbl);

        mObject.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = DIRECTION.random();
//        randObjectTarget();
//        randPos();
        // Rectangles (squares) in the display frame (after margins)

        randCompPos();

        Out.d(TAG, mDir, mTargetPnl.getLocation(), mObject.getLocation());
        translateToPanel();

        Out.d(TAG, mDir, mTargetPnl.getLocation(), mObject.getLocation());

        removeAll();
        add(mTargetPnl, DEFAULT_LAYER);
//        add(alphaContain, PALETTE_LAYER);

        repaint();
    }

    private void randCompPos() {
        String TAG = NAME + "randPos";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Distance in px
        final int dist = Utils.mm2px(DIST_mm);
        final int sideDist = (int) (dist / Math.sqrt(2));

        final int tgtW = mTargetPnl.getWidth();
        final int objW = mObject.width;
        
        // NEWS
        int combW = tgtW + dist + objW; // Combined rectangle side

        switch (mDir) {
            case N -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - tgtW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() + ((tgtW - objW) / 2),
                        mTargetPnl.getY() + (tgtW + dist));
            }

            case S -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - tgtW),
                        Utils.randInt(objW + dist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + ((tgtW - objW) / 2),
                        mTargetPnl.getY() - (objW + dist));
            }

            case E -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + dist, dispW - tgtW),
                        Utils.randInt(0, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + dist),
                        mTargetPnl.getY() + ((tgtW - objW) / 2));
            }

            case W -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - combW),
                        Utils.randInt(0, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + dist),
                        mTargetPnl.getY() + ((tgtW - objW) / 2));
            }
        }

        // Diagonal
        combW = tgtW + sideDist + objW; // Combined rectangle side

        switch (mDir) {
            case NE -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + sideDist, dispW - tgtW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + sideDist),
                        mTargetPnl.getY() + (tgtW + sideDist));
            }

            case NW -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, dispW - combW),
                        Utils.randInt(0, dispH - combW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + sideDist),
                        mTargetPnl.getY() + (tgtW + sideDist));
            }

            case SE -> {
                mTargetPnl.setLocation(
                        Utils.randInt(objW + sideDist, dispW - tgtW),
                        Utils.randInt(objW + sideDist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() - (objW + sideDist),
                        mTargetPnl.getY() - (objW + sideDist));
            }

            case SW -> {
                mTargetPnl.setLocation(
                        Utils.randInt(0, combW),
                        Utils.randInt(objW + sideDist, dispH - tgtW));

                mObject.setLocation(
                        mTargetPnl.getX() + (tgtW + sideDist),
                        mTargetPnl.getY() - (objW + sideDist));
            }
        }
    }
    
    private void randPos() {
        String TAG = NAME + "randPos";

        // Dimension of the display frame (in px)
        final int dispW = getDispDim().width;
        final int dispH = getDispDim().height;

        // Distance in px
        final int dist = Utils.mm2px(DIST_mm);
        final int sideDist = (int) (dist / Math.sqrt(2));

        // Rectangles (squares) in the display frame (after margins)
        mObject.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));
        mTarget.setSize(Utils.mm2px(TARGET_W_mm), Utils.mm2px(TARGET_W_mm));

        // NEWS
        int combW = mTarget.width + dist + mObject.width; // Combined rectangle side

        switch (mDir) {
            case N -> {
                mTarget.x = Utils.randInt(0, dispW - mTarget.width);
                mTarget.y = Utils.randInt(0, dispH - combW);

                mObject.x = mTarget.x + ((mTarget.width - mObject.width) / 2);
                mObject.y = mTarget.y + (mTarget.width + dist);
            }

            case S -> {
                mTarget.x = Utils.randInt(0, dispW - mTarget.width);
                mTarget.y = Utils.randInt(mObject.width + dist, dispH - mTarget.width);

                mObject.x = mTarget.x + ((mTarget.width - mObject.width) / 2);
                mObject.y = mTarget.y - (mObject.width + dist);
            }

            case E -> {
                mTarget.x = Utils.randInt(mObject.width + dist, dispW - mTarget.width);
                mTarget.y = Utils.randInt(0, dispH - mTarget.width);

                mObject.x = mTarget.x - (mObject.width + dist);
                mObject.y = mTarget.y + ((mTarget.width - mObject.width) / 2);
            }

            case W -> {
                mTarget.x = Utils.randInt(0, dispW - combW);
                mTarget.y = Utils.randInt(0, dispH - mTarget.width);

                mObject.x = mTarget.x + (mTarget.width + dist);
                mObject.y = mTarget.y + ((mTarget.width - mObject.width) / 2);
            }
        }

        // Diagonal
        combW = mTarget.width + sideDist + mObject.width; // Combined rectangle side

        switch (mDir) {
            case NE -> {
                mTarget.x = Utils.randInt(mObject.width + sideDist, dispW - mTarget.width);
                mTarget.y = Utils.randInt(0, dispH - combW);

                mObject.x = mTarget.x - (mObject.width + sideDist);
                mObject.y = mTarget.y + (mTarget.width + sideDist);
            }

            case NW -> {
                mTarget.x = Utils.randInt(0, dispW - combW);
                mTarget.y = Utils.randInt(0, dispH - combW);

                mObject.x = mTarget.x + (mTarget.width + sideDist);
                mObject.y = mTarget.y + (mTarget.width + sideDist);
            }

            case SE -> {
                mTarget.x = Utils.randInt(mObject.width + sideDist, dispW - mTarget.width);
                mTarget.y = Utils.randInt(mObject.width + sideDist, dispH - mTarget.width);

                mObject.x = mTarget.x - (mObject.width + sideDist);
                mObject.y = mTarget.y - (mObject.width + sideDist);
            }

            case SW -> {
                mTarget.x = Utils.randInt(0, combW);
                mTarget.y = Utils.randInt(mObject.width + sideDist, dispH - mTarget.width);

                mObject.x = mTarget.x + (mTarget.width + sideDist);
                mObject.y = mTarget.y - (mObject.width + sideDist);
            }
        }

    }

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);
//        mTarget.translate(lrMargin, tbMargin);

        mTargetPnl.translate(lrMargin, tbMargin);
        mObject.translate(lrMargin, tbMargin);
//        mObjectLbl.translate(lrMargin, tbMargin);
    }

    /**
     * Get the randomly-positioned Object and Target
     */
    private void randObjectTarget() {
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

//        Out.d(TAG, dispH,  -tgtDispRect.height / 2, - dist, - tgtDispRect.height / 2);
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
//        Out.d(NAME, mObjectLbl.getLocation(), mObjectLbl.getSize(), getCursorPos());
//        if (mObjectLbl.getBounds().contains(getCursorPos())) {
//            mGrabbed = true;
//            mGrabPos = getCursorPos();
//        }

        if (mObject.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    public void release() {
        if (mGrabbed) {
            if (isSuccessful()) {
                Consts.SOUNDS.playHit();

                // Move the object within (if not already)
                if (!mTargetPnl.getBounds().contains(mObject)) {
                    moveObjInside();
                    repaint();
                }

            } else {
                Consts.SOUNDS.playMiss();
            }

            mGrabbed = false;

            // Wait a certain delay, then show the next trial
            executorService.schedule(this::showTrial, DROP_DELAY_ms, TimeUnit.MILLISECONDS);

        }
    }

    public void moveObjInside() {

        final Rectangle tgtBounds = mTargetPnl.getBounds();
        final Rectangle objBounds = mObject.getBounds();
        final Rectangle intersection = tgtBounds.intersection(objBounds);

        final int dMinX = (int) (intersection.getMinX() - objBounds.getMinX());
        final int dMaxX = (int) (intersection.getMaxX() - objBounds.getMaxX());

        final int dMinY =  (int) (intersection.getMinY() - objBounds.getMinY());
        final int dMaxY =  (int) (intersection.getMaxY() - objBounds.getMaxY());

        mObject.translate(dMinX + dMaxX, dMinY + dMaxY);
//        mObjectLbl.translate(dMinX + dMaxX, dMinY + dMaxY);
    }

    @Override
    public boolean isSuccessful() {
        return mTargetPnl.getBounds().contains(getCursorPos());
    }

    // -------------------------------------------------------------------------------------------

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        final String TAG = NAME + "paintComponent";
//
//        Graphics2D g2d = (Graphics2D) g;
//
////        final Stroke oldStroke = g2d.getStroke();
////        final float newThickness = 3;
////
////        // Draw Target (should be drawn behind the object)
////        g2d.setColor(COLORS.GRAY_900);
////        g2d.fill(mTarget);
////
////        g2d.setColor(COLORS.BLUE_100);
////        g2d.setStroke(new BasicStroke(newThickness));
////        g2d.draw(mTarget);
////        g2d.setStroke(oldStroke);
////
////        // Draw Object
////        mObject.setBounds(400, 600, 500, 500);
////        g2d.setColor(COLORS.BLUE_900_ALPHA);
////        g2d.fill(mObject);
//    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

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
//            mObjectLbl.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
//        if (mGrabbed) {
//            mGrabPos = e.getPoint();
//
//            final int dX = e.getX() - mGrabPos.x;
//            final int dY = e.getY() - mGrabPos.y;
//            mObject.translate(dX, dY);
////            mObjectLbl.translate(dX, dY);
//
//            repaint();
//        }
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
