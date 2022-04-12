package gui;

import experiment.Block;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static tools.Consts.*;

public class TestPanel extends JLayeredPane implements MouseMotionListener, MouseListener {
    private final String NAME = "TestPanel/";

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Layout & elements
    private Dimension mDim = new Dimension();

    private final int TARGET_W_mm = 20; // mm
    private final int DOCK_W_mm = 30; // mm
    private final Point TARGET_INIT_POS = new Point(400, 200);
    private final Point DOCK_INIT_POS = new Point(1000, 300);

    private boolean mGrabbed = false;
    private final boolean mGhosting = false;

    private Rectangle mTarget = new Rectangle();
    private Rectangle mTargetGhost = new Rectangle();
    private Rectangle mDock = new Rectangle();

    private Point mGrabPos = new Point();

    // Actions ------------------------------------------------------------------------------------



    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     */
    public TestPanel(Dimension dim) {
        setSize(dim);
        setLayout(null);

        addMouseMotionListener(this);
        addMouseListener(this);

        // Key maps
        mapKeys();
    }

    public void start() {
        final int targetW = Utils.mm2px(TARGET_W_mm);
        final int dockW = Utils.mm2px(DOCK_W_mm);

        mTarget = new Rectangle(
                TARGET_INIT_POS.x, TARGET_INIT_POS.y,
                targetW, targetW);

        mDock = new Rectangle(
                DOCK_INIT_POS.x, DOCK_INIT_POS.y,
                dockW, dockW);

        showTrial();
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

//        label = new JLabel();
//        label.setOpaque(true);
//        label.setBackground(Color.BLUE);
//        label.setBounds(mRectPos.x, mRectPos.y, TARGET_W, TARGET_W);
//        add(label, 0);

        repaint();
    }

    public void grab() {
        if (mTarget.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
            mTargetGhost = (Rectangle) mTarget.clone();
        }
    }

    public void release() {
        if (mGrabbed) {
            mGrabbed = false;
            repaint();
        }
    }

    public void cancel() {
        mTarget.setLocation(mTargetGhost.getLocation());
        mGrabbed = false;
        repaint();
    }

    // -------------------------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final float newThickness = 3;

        // Draw dock
        g2d.setColor(COLORS.GRAY_500);
        g2d.setStroke(new BasicStroke(newThickness));
        g2d.fill(mDock);
        g2d.setStroke(oldStroke);

        // Draw ghost (if exists)
        if (mGhosting && mGrabbed) {
            g2d.setColor(COLORS.BLUE_100);
            g2d.fill(mTargetGhost);
        }

        // Draw target
        g2d.setColor(COLORS.BLUE_900);
        g2d.fill(mTarget);
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
            mTarget.translate(dX, dY);

            mGrabPos = e.getPoint();
//            mTargetGhost = (Rectangle) mTarget.clone();

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mGrabbed) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mTarget.translate(dX, dY);

            mGrabPos = e.getPoint();

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
