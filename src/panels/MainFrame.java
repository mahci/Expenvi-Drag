package panels;

import control.Logger;
import control.Server;
import experiment.Experiment;
import tools.Memo;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class MainFrame extends JFrame implements MouseListener {
    private final static String NAME = "MainFrame/";

    private static MainFrame self; // Singelton instance

    // Keys
    private KeyStroke KS_1, KS_2, KS_3, KS_4;
    private KeyStroke KS_SLASH;

    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;

    private TaskPanel mActivePanel;

    private long mHomingStartTime;

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public final int PID = 300;

    public MODE mMode = MODE.TEST;
    public TECHNIQUE mActiveTechnique = TECHNIQUE.TAP_PRESS_HOLD;
    public TASK mActiveTask = TASK.BAR;

    public final int NUM_BLOCKS = 5;
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();
        setBackground(Color.WHITE);

//        addMouseListener(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Server.get().close();
            }
        });

        mapKeys();
    }

    /**
     * Get the instance
     * @return MainFram instance
     */
    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    /**
     * Start the frame
     */
    public void start() {

        switch (mMode) {
            case DEMO -> {
                // Create log files
                Logger.get().initLog("DEMO-" + PID);

                startDemo();
            }

            case PRACTICE -> {
                // Create log files
                Logger.get().initLog("P" + PID + "-PRACTICE");

                startPractice();
            }

            case TEST -> {
                // Create log files
                Logger.get().initLog("P" + PID);

                startTask();
            }
        }

    }

    /**
     * Start the demo
     */
    private void startDemo() {
        Server.get().start();
        Server.get().send(new Memo(STRINGS.CONFIG, STRINGS.TECH, mActiveTechnique));

        // Map actions
        getRootPane().getActionMap().put(KeyEvent.VK_1, new TaskSwitchAction(1));
        getRootPane().getActionMap().put(KeyEvent.VK_2, new TaskSwitchAction(2));
        getRootPane().getActionMap().put(KeyEvent.VK_3, new TaskSwitchAction(3));
        getRootPane().getActionMap().put(KeyEvent.VK_4, new TaskSwitchAction(4));

        getRootPane().getActionMap().put(KeyEvent.VK_SLASH, new TechSwitchAction());

        // Show the Intro panel
        IntroPanel stPanel = new IntroPanel(
                "Welcome to DRAG experiment!",
                mActiveTechnique,
                mActiveTask,
                showPracticeAction,
                true);
        add(stPanel);
        setVisible(true);
    }

    /**
     * Start the practice
     */
    private void startPractice() {
        Server.get().start();
        Server.get().send(new Memo(STRINGS.CONFIG, STRINGS.TECH, mActiveTechnique));


        // Show the practice Intro panel
        IntroPanel stPanel = new IntroPanel(
                "Press SPACE to start the practice",
                mActiveTechnique,
                mActiveTask,
                showPracticeAction,
                false);
        add(stPanel);
        setVisible(true);
    }

    /**
     * Start the main task
     */
    private void startTask() {
        // Start the Server if working with MOOSE
        if (!mActiveTechnique.equals(TECHNIQUE.MOUSE)) {
            Server.get().start();

            // Sync the active technique and pcId to the Moose
            Server.get().send(new Memo(STRINGS.CONFIG, STRINGS.TECH, mActiveTechnique));
        }

        // Show the Intro panel
        IntroPanel stPanel = new IntroPanel(
                "When ready, press SPACE to start the task",
                mActiveTechnique,
                mActiveTask,
                showTaskAction,
                false);
        add(stPanel);
        setVisible(true);
    }

    public void setHomeingStartTime() {
        mHomingStartTime = Utils.nowMillis();
    }

    public long getmHomingStartTime() {
        return mHomingStartTime;
    }

    public void resetHomingStartTime() {
        mHomingStartTime = 0;
    }

    /**
     * Show a dialog
     * @param dialog JDialog
     */
    public void showDialog(JDialog dialog) {
        Out.d(NAME, "Showing dialog");
        dialog.pack();
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        int frW = dialog.getSize().width;
        int frH = dialog.getSize().height;

        dialog.setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );
        dialog.setVisible(true);
    }

    /**
     * Show a message (in the form of a dialog box)
     * @param mssg String message
     */
    public void showMessage(String mssg) {
        JOptionPane.showMessageDialog(this, mssg);
    }

    /**
     * Show the end panel (dependant on task, ...)
     */
    public void showEndPanel() {
        getContentPane().removeAll();

        EndPanel endPanel = new EndPanel(
                mActiveTask,
                mActiveTechnique,
                mMode);
        add(endPanel);
        setVisible(true);
    }

    /**
     * Set the config for showing panels
     */
    private void setDisplayConfig() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximized frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close on exit

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();

        scrBound = gd[1].getDefaultConfiguration().getBounds();
        scrW = scrBound.width;
        scrH = scrBound.height;

        frW = getSize().width;
        frH = getSize().height;

        setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );
    }

    // Moose actions (called from outside) ----------------------------------------------------------------
    public void grab() {
        if (mActivePanel != null) mActivePanel.grab();
    }

    public void release() {
        if (mActivePanel != null) mActivePanel.release();
    }

    public void revert() {
        if (mActivePanel != null) mActivePanel.revert();
    }


    // Listeners ------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    // Actions --------------------------------------------------------------------------------------------
    final AbstractAction showTaskAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final Dimension panelDim = getContentPane().getSize();

            getContentPane().removeAll();

            switch (mActiveTask) {
                case BOX -> {
                    mActivePanel = new BoxTaskPanel(panelDim).setTask(new Experiment.BoxTask(NUM_BLOCKS));
                }
                case BAR -> {
                    mActivePanel = new BarTaskPanel(panelDim).setTask(new Experiment.BarTask(NUM_BLOCKS));
                }
                case PEEK -> {
                    mActivePanel = new PeekTaskPanel(panelDim).setTask(new Experiment.PeekTask(NUM_BLOCKS));
                }
                case TUNNEL -> {
                    mActivePanel = new TunnelTaskPanel(panelDim).setTask(new Experiment.TunnelTask(NUM_BLOCKS));
                }
            }

            mActivePanel.setOpaque(true);
            mActivePanel.setBackground(Color.WHITE);
            if (mActiveTechnique.equals(TECHNIQUE.MOUSE)) mActivePanel.setMouseEnabled(true);

            getContentPane().add(mActivePanel);
            mActivePanel.requestFocusInWindow();
            mActivePanel.start();
        }
    };

    final AbstractAction showPracticeAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final Dimension panelDim = getContentPane().getSize();
            final int nPrBlocks = 20;

            switch (mActiveTask) {
                case BOX -> {
                    mActivePanel = new BoxTaskPanel(panelDim)
                                    .setTask(new Experiment.BoxTask(nPrBlocks));
                }
                case BAR -> {
                    mActivePanel = new BarTaskPanel(panelDim)
                                    .setTask(new Experiment.BarTask(nPrBlocks));
                }
                case PEEK -> {
                    mActivePanel = new PeekTaskPanel(panelDim)
                                    .setTask(new Experiment.PeekTask(nPrBlocks));
                }
                case TUNNEL -> {
                    mActivePanel = new TunnelTaskPanel(panelDim)
                                    .setTask(new Experiment.TunnelTask(nPrBlocks));
                }

            }

            mActivePanel.setOpaque(true);
            mActivePanel.setBackground(Color.WHITE);
            mActivePanel.setMouseEnabled(true);
            mActivePanel.setPracticeMode(true);

            getContentPane().add(mActivePanel);
            mActivePanel.requestFocusInWindow();
            mActivePanel.start();
        }
    };

    private class TaskSwitchAction extends AbstractAction {
        private int taskNum;

        public TaskSwitchAction(int taskNum) {
            this.taskNum = taskNum;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Dimension panelDim = getContentPane().getSize();
            final int nPrBlocks = 20;

            getContentPane().removeAll();

            switch (taskNum) {
                case 1 -> {
                    mActivePanel =
                            new BoxTaskPanel(panelDim)
                                    .setTask(new Experiment.BoxTask(nPrBlocks));
                }
                case 2 -> {
                    mActivePanel =
                            new BarTaskPanel(panelDim)
                                    .setTask(new Experiment.BarTask(nPrBlocks));
                }
                case 3 -> {
                    mActivePanel =
                            new PeekTaskPanel(panelDim)
                                    .setTask(new Experiment.PeekTask(nPrBlocks));
                }
                case 4 -> {
                    mActivePanel =
                            new TunnelTaskPanel(panelDim)
                                    .setTask(new Experiment.TunnelTask(nPrBlocks));
                }
            }

            mActivePanel.setOpaque(true);
            mActivePanel.setBackground(Color.WHITE);
            mActivePanel.setMouseEnabled(true);
            mActivePanel.setPracticeMode(true);

            getContentPane().add(mActivePanel);
            mActivePanel.requestFocusInWindow();
            mActivePanel.start();
        }
    }

    private class TechSwitchAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            switch (mActiveTechnique) {
                case TAP_PRESS_HOLD -> mActiveTechnique = TECHNIQUE.TWO_FINGER_SWIPE;
                case TWO_FINGER_SWIPE -> mActiveTechnique = TECHNIQUE.MOUSE;
                case MOUSE -> mActiveTechnique = TECHNIQUE.TAP_PRESS_HOLD;
            }

            // Send the active technique to Moose
            Server.get().send(new Memo(STRINGS.CONFIG, STRINGS.TECH, mActiveTechnique));

            // Refresh the practice panel
            if (mActivePanel != null) mActivePanel.repaint();
        }
    }

    //-----------------------------------------------------------------------------------------------------

    private void mapKeys() {
        KS_1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0, true);
        KS_2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, 0, true);
        KS_3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, 0, true);
        KS_4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, 0, true);
        KS_SLASH = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true);

        final InputMap framInputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        framInputMap.put(KS_1, KeyEvent.VK_1);
        framInputMap.put(KS_2, KeyEvent.VK_2);
        framInputMap.put(KS_3, KeyEvent.VK_3);
        framInputMap.put(KS_4, KeyEvent.VK_4);
        framInputMap.put(KS_SLASH, KeyEvent.VK_SLASH);
    }
}
