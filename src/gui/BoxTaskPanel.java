package gui;

import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static experiment.Experiment.*;

import static tools.Consts.COLORS;

public class BoxTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BoxTaskPanel/";

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
    public BoxTaskPanel(Dimension dim) {
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

        mObject.setSize(Utils.mm2px(OBJECT_W_mm), Utils.mm2px(OBJECT_W_mm));

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = DIRECTION.random();

        randPosition();
        translateToPanel();

        removeAll();
        add(mTargetPnl, DEFAULT_LAYER);

        repaint();
    }

    private void randPosition() {
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

    private void translateToPanel() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);

        AffineTransform transform = new AffineTransform();
        transform.translate(lrMargin, tbMargin);

        mTargetPnl.translate(lrMargin, tbMargin);
        mObject.translate(lrMargin, tbMargin);
    }


    @Override
    public void grab() {
        if (mObject.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    @Override
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
}
