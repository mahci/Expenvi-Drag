package gui;

import experiment.Experiment;
import tools.Consts;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BarTaskPanel extends TaskPanel implements MouseMotionListener, MouseListener {
    private final String NAME = "BarTaskPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Constants
    private final double BAR_L_mm = 20; // Bar length
    private final double BAR_GRAB_TOL_mm = 5; // tolearnce from each side (to grab)
    private final double TARGET_L_mm = 100; // Lneght of the target lines (> bar L)
    private final double TARGET_D_mm = 50; // Perpendicular distance betw. the target lines (> bar L)
    private final double DIST_mm = 150; // Distance from center of bar to the middle of the target lines (= rect cent)

    // Flags
    private boolean mGrabbed = false;
    private boolean isNearBar = false;

    // Shapes
    private Line mBar = new Line(600, 800, 1000, 400);
    private Line mTargetLine1 = new Line();
    private Line mTargetLine2 = new Line();

    // Other
    private Point mGrabPos = new Point();
    private Experiment.DIRECTION mDir;
    private Dimension mDim;

    // Actions ------------------------------------------------------------------------------------
    private final Action NEXT_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };


    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public BarTaskPanel(Dimension dim) {
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
        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mDir = Experiment.DIRECTION.random();

        repaint();
    }

    /**
     * Get the randomly-positioned Object and Target
     */
    private void randBarTarget() {
        String TAG = NAME + "randBarTarget";

        //-- Create a polygon from bar + target lines
//        mTargetLine1.set(0, 0);
        Polygon barTar = new Polygon();
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
        return false;
    }


    // -------------------------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final float newStroke = 3;
        g2d.setStroke(new BasicStroke(newStroke));

//        g2d.setColor(Consts.COLORS.GRAY_500);
//        g2d.drawLine(mTargetLine1.x1, mTargetLine1.y1, mTargetLine1.x2, mTargetLine1.y2);
//        g2d.drawLine(mTargetLine2.x1, mTargetLine2.y1, mTargetLine2.x2, mTargetLine2.y2);

        g2d.setColor(Consts.COLORS.BLUE_900);
        g2d.drawLine(mBar.x1, mBar.y1, mBar.x2, mBar.y2);

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
            mBar.translate(dX, dY);

            mGrabPos = e.getPoint();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        isNearBar = mBar.isNear(e.getPoint());
        if (isNearBar) {
            setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (mGrabbed) {
            mGrabPos = e.getPoint();

            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mBar.translate(dX, dY);

            repaint();
        }
    }
}
