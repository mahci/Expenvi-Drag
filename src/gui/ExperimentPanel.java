package gui;

import control.Controller;
import control.Logger;
import control.Server;
import experiment.Block;
import experiment.Experiment;
import experiment.Trial;
import tools.*;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static experiment.Experiment.*;
import static tools.Consts.*;
import static tools.Consts.SOUNDS.*;
import static tools.Consts.STRINGS.END_EXPERIMENT_MESSAGE;
import static control.Logger.*;

public class ExperimentPanel extends JLayeredPane implements MouseMotionListener {

    private final static String NAME = "ExperimentPanel/";
    // -------------------------------------------------------------------------------------------

    // Margins
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin
    private double TB_MARGIN_mm = 20; // (mm) Left-right margin

    // Keystrokes
    private KeyStroke KS_SPACE;
    private KeyStroke KS_SLASH;
    private KeyStroke KS_ENTER;
    private KeyStroke KS_SHIFT;
    private KeyStroke KS_Q;
    private KeyStroke KS_A;
    private KeyStroke KS_W;
    private KeyStroke KS_S;
    private KeyStroke KS_E;
    private KeyStroke KS_D;
    private KeyStroke KS_RA; // Right arrow
    private KeyStroke KS_DA; // Down arrow

    // Experiment and trial
    private Experiment mExperiment;
    private List<Block> mBlocks; // Blocks in a TechTask
    private Block mBlock;
    private Trial mTrial;

    // Elements
    private JLabel mLabel;
    private JLabel mLevelLabel;
    private JLabel mTechLabel;
    private JLabel mShortBreakLabel;
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();
    private AudioClip mHitSound, mMissSound, mTechEndSound;

    // Logging
    private GeneralInfo mGenInfo;
    private TimeInfo mTimeInfo = new Logger.TimeInfo();
    private long mExpStTime;

    // ACTIONS ---------------------------------------------------------------------------------------
    // Shart the experiment
    private final Action START_EXP_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {

            // Show trial
            remove(0);
            showTrial();
        }
    };

    private ConfigAction mNextTechnique = new ConfigAction(STRINGS.TECH, true);


    // -------------------------------------------------------------------------------------------
    /**
     * Create the panel
     * @param exp Experiment to show
     */
    public ExperimentPanel(Experiment exp) {
        String TAG = NAME;
        setLayout(null);

        // Set mouse listener and key bindings
        addMouseMotionListener(this);
        mapKeys();
        getActionMap().put("SPACE", START_EXP_ACTION);

        // Set the experiment and log
        mExperiment = exp;
        Logger.get().logParticipant(exp.getPId());


        // Set up the sounds
        loadSounds();
    }

    /**
     * Create one instance of each pane
     */
    private void createPanes() {
        String TAG = NAME + "createPanes";

    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        // Start logging
        final Logger.InstantInfo instantInfo = new Logger.InstantInfo();
        instantInfo.trialShow = Utils.nowInMillis();
//        mTrialStTime = Utils.nowInMillis();

        // If panes aren't created, create them (only once)
//        if (mVTScrollPane == null || mTDScrollPane == null) createPanes();

        // Show the trial


        // Show labels
//        mLevelLabel.setText("Block: " + (mBlockInd + 1) +
//                " / " + mBlocks.size() + " --- " +
//                "Successful Trials: " + mNSuccessTrials +
//                "  / " +  mBlock.getTargetNTrials());
//        mTechLabel.setText("Technique: " + mTechs.get(mTechInd));
//        add(mLevelLabel, 1);
//        add(mTechLabel, 1);

        revalidate();
        repaint();
    }

    /**
     * Check if each axes of a trial was a hit (1) or a miss (0)
     * Vertical -> hzResult = 1
     * @return Pair of int for each dimension
     */
    private Pair checkHit() {
        return new Pair();
    }

    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point getRandPosition(Dimension paneDim) {
        String TAG = NAME + "randPosition";

        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);

//        final int minX = lrMargin;
//        final int maxX = getWidth() - (lrMargin + paneDim.width);
//
//        final int midY = (getHeight() - paneDim.height) / 2;
//
//        if (minX >= maxX) return new Point(); // Invalid dimensions
//        else {
//            int randX = 0;
//
//            do {
//                randX = Utils.randInt(minX, maxX);
//            } while (Math.abs(randX - mLasPanePos.x) <= paneDim.width); // New position shuold be further than W
//
//            return new Point(randX, midY);
//        }

        return new Point();
    }

    /**
     * Get a random line index
     * NOTE: Indexes start from 1
     * @return A random line index
     */
    private int randVtLineInd() {
        String TAG = NAME + "randVtLineInd";

        return 0;
    }

    /**
     * Scroll the scrollPanes for a certain amount
     * @param vtScrollAmt Vertical scroll amount
     * @param hzScrollAmt Horizontal scroll amount
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        String TAG = NAME + "scroll";

        Logs.d(TAG, "Scrolling", vtScrollAmt, hzScrollAmt);
    }

    /**
     * Go to the next trial
     */
    private void nextTrial() {
        final String TAG = NAME + "nextTrial";

        // Inc nums, ...

        showTrial();
    }

    /**
     * Go to the next block
     */
    private void nextBlock() {
        final String TAG = NAME + "nextBlock";

        // Inc block, ...


        showTrial();
    }

    /**
     * The current technique is ended
     */
    private void endExp() {
        removeAll();
        showExpEndDialog();

        // Send the end message to the Moose
        Server.get().send(new Memo(STRINGS.LOG, STRINGS.END, 0, 0));
    }

    /**
     * Show the short break in in each task
     */
    private void showShortBreak() {
        removeAll();

        add(mShortBreakLabel, 0);

        repaint();
    }

    /**
     * Show the long break between TechTasks
     */
    private void showLongBreak() {

        removeAll();
        repaint();
        MainFrame.get().showDialog(new BreakDialog());

        // ... back from the break
//        mInLongBreak = false;
    }

    /**
     * Show the technique end dialog
     */
    public void showExpEndDialog() {
        JDialog dialog = new JDialog((JFrame)null, "Child", true);
        dialog.setSize(1000, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(800, 500);
        panel.setBackground(Color.decode("#7986CB"));
        panel.add(Box.createRigidArea(new Dimension(800, 100)));

        JLabel label = new JLabel(END_EXPERIMENT_MESSAGE);
        label.setFont(new Font("Sans", Font.BOLD, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton button = new JButton("Close");
        button.setMaximumSize(new Dimension(300, 50));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                System.exit(0);
            }
        });
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        panel.add(button);

        dialog.add(panel);
        dialog.setUndecorated(true);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        // Do the displaying

    }

    /**
     * Load all the sounds to play later
     */
    private void loadSounds() {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();

            final URL hitURL = new File(Objects.requireNonNull(classLoader.getResource(HIT_SOUND))
                    .getFile()).toURI().toURL();
            final URL missURL = new File(Objects.requireNonNull(classLoader.getResource(MISS_SOUND))
                    .getFile()).toURI().toURL();
            final URL techEndURL = new File(Objects.requireNonNull(classLoader.getResource(TECH_END_SOUND))
                    .getFile()).toURI().toURL();

            mHitSound = Applet.newAudioClip(hitURL);
            mMissSound = Applet.newAudioClip(missURL);
            mTechEndSound = Applet.newAudioClip(techEndURL);

        } catch ( NullPointerException
                | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Map the keys
     */
    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_SLASH = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true);
        KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);

        getInputMap().put(KS_SPACE, "SPACE");
        getInputMap().put(KS_SLASH, "SLASH");
        getInputMap().put(KS_ENTER, "ENTER");
        getInputMap().put(KS_SHIFT, "SHIFT");

    }

    // Mouse Listeners -------------------------------------------------------------------------------------
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    // ConfigAction ----------------------------------------------------------------------------------------
    private class ConfigAction extends AbstractAction {
        private final String TAG = "ExperimentPanel/" + "AdjustSensitivtyAction";
        private String mAction = "";
        private boolean mInc = false;

        public ConfigAction(String action, boolean inc) {
            mAction = action;
            mInc = inc;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }


}
