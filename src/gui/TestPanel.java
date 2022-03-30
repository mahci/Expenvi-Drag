package gui;

import experiment.Block;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static tools.Consts.*;

public class TestPanel extends JLayeredPane implements MouseMotionListener, MouseListener {
    private final String NAME = "TestPanel/";

    // Block and related
    private Block mBlock;
    private int mNBlocks; // Just for show
    private int mNSuccessTrials; // Number of successful trials so far

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Layout & elements
    private Dimension mDim = new Dimension();

    private Point mLasPanePos = new Point();
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();
    private JLabel mLevelLabel;
    private JLabel mTechLabel;

    private final int TARGET_W = 100; // px
    private final int DOCK_W = 150; // px
    private final Point TARGET_INIT_POS = new Point(500, 400);
    private final Point DOCK_INIT_POS = new Point(2000, 500);

    private boolean mDragging = false;
    private boolean mGrabbed = false;
    private Rectangle mTarget = new Rectangle();
    private Rectangle mTargetGhost = new Rectangle();
    private Rectangle mDock = new Rectangle();

    private Point mGrabPos = new Point();

    private JLabel label;

    // Actions ------------------------------------------------------------------------------------

    // End a trial
    private final Action END_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String TAG = TestPanel.this.NAME +  "END_TRIAL";
            Out.d(TAG, "End trial");
        }
    };

    // Jump to the next trial (without check)
    private final Action ADVANCE_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            remove(0);
            mNSuccessTrials++;
        }
    };


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
        mTarget = new Rectangle(
                TARGET_INIT_POS.x, TARGET_INIT_POS.y,
                TARGET_W, TARGET_W);

        mDock = new Rectangle(
                DOCK_INIT_POS.x, DOCK_INIT_POS.y,
                DOCK_W, DOCK_W);

        showTrial();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getActionMap().put(KeyEvent.VK_SPACE, END_TRIAL);
        getActionMap().put(KeyEvent.VK_RIGHT, ADVANCE_TRIAL);
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

    /**
     * Check if the cursor is inside
     * @return
     */
//    private boolean isCursorInsideTarget() {
//        Point cursorPos = getCursorPos();
//
//        if (cursorPos != null) { // Somehow it can be null!
//            return (cursorPos.x >= mTargetPos.x) &&
//                    (cursorPos.y >= mTargetPos.y) &&
//                    (cursorPos.x <= mTargetPos.x + TARGET_W) &&
//                    (cursorPos.y <= mTargetPos.y + TARGET_W);
//        } else {
//            return false;
//        }
//    }

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
        if (mGrabbed) {
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
        if (mDragging) {
            final int dX = e.getX() - mGrabPos.x;
            final int dY = e.getY() - mGrabPos.y;
            mTarget.translate(dX, dY);

            mGrabPos = e.getPoint();

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
        if (mTarget.contains(getCursorPos())) {
            mGrabbed = true;
            mGrabPos = getCursorPos();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mDragging = false;
        mGrabPos = e.getPoint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
