package panels;

import control.Logger;
import control.Server;
import experiment.Experiment;
import tools.Memo;
import tools.Out;

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
    private TaskPanel mPracticePanel;

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public final int PID = 100;

    public final boolean DEMO_MODE = false;
    public final boolean PRACTICE_MODE = true;

    public TECHNIQUE mActiveTechnique = TECHNIQUE.MOUSE;
    public TASK mActiveTask = TASK.BOX;
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

    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    public void start() {

        if (DEMO_MODE) {
            // Create log files
            Logger.get().logParticipant(123);

            startDemo();

        } else if (PRACTICE_MODE) {
            // Create log files
            Logger.get().logParticipant(-PID);

            startPractice();
        } else { // Experiment mode
            // Create log files
            Logger.get().logParticipant(PID);

            startTask();
        }

    }

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

    private void startPractice() {
        Server.get().start();
        Server.get().send(new Memo(STRINGS.CONFIG, STRINGS.TECH, mActiveTechnique));

        // Map actions
//        getRootPane().getActionMap().put(KeyEvent.VK_1, new TaskSwitchAction(1));
//        getRootPane().getActionMap().put(KeyEvent.VK_2, new TaskSwitchAction(2));
//        getRootPane().getActionMap().put(KeyEvent.VK_3, new TaskSwitchAction(3));
//        getRootPane().getActionMap().put(KeyEvent.VK_4, new TaskSwitchAction(4));
//
//        getRootPane().getActionMap().put(KeyEvent.VK_SLASH, new TechSwitchAction());
//
//        // Show the Intro panel
        IntroPanel stPanel = new IntroPanel(
                "Press SPACE to start the practice",
                mActiveTechnique,
                mActiveTask,
                showPracticeAction,
                false);
        add(stPanel);
        setVisible(true);
    }

    private void startTask() {
        // Start the Server if working with MOOSE
        if (!mActiveTechnique.equals(TECHNIQUE.MOUSE)) {
            Server.get().start();
            // Send the active technique to Moose
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

    public void showMessage(String mssg) {
        JOptionPane.showMessageDialog(this, mssg);
    }

    public void showEndPanel() {
        getContentPane().removeAll();

        EndPanel endPanel = new EndPanel(
                mActiveTask,
                mActiveTechnique,
                PRACTICE_MODE);
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

    public void grab() {
        if (mActivePanel != null) mActivePanel.grab();
    }

    public void release() {
        if (mActivePanel != null) mActivePanel.release();
    }

    public void revert() {
        if (mActivePanel != null) mActivePanel.revert();
    }

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
                    mPracticePanel = new BoxTaskPanel(panelDim)
                                    .setTask(new Experiment.BoxTask(nPrBlocks));
                }
                case BAR -> {
                    mPracticePanel = new BarTaskPanel(panelDim)
                                    .setTask(new Experiment.BarTask(nPrBlocks));
                }
                case PEEK -> {
                    mPracticePanel = new PeekTaskPanel(panelDim)
                                    .setTask(new Experiment.PeekTask(nPrBlocks));
                }
                case TUNNEL -> {
                    mPracticePanel = new TunnelTaskPanel(panelDim)
                                    .setTask(new Experiment.TunnelTask(nPrBlocks));
                }

            }

            mPracticePanel.setOpaque(true);
            mPracticePanel.setBackground(Color.WHITE);
            mPracticePanel.setMouseEnabled(true);
            mPracticePanel.setPracticeMode(true);

            getContentPane().add(mPracticePanel);
            mPracticePanel.requestFocusInWindow();
            mPracticePanel.start();
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
                    mPracticePanel =
                            new BoxTaskPanel(panelDim)
                                    .setTask(new Experiment.BoxTask(nPrBlocks));
                }
                case 2 -> {
                    mPracticePanel =
                            new BarTaskPanel(panelDim)
                                    .setTask(new Experiment.BarTask(nPrBlocks));
                }
                case 3 -> {
                    mPracticePanel =
                            new PeekTaskPanel(panelDim)
                                    .setTask(new Experiment.PeekTask(nPrBlocks));
                }
                case 4 -> {
                    mPracticePanel =
                            new TunnelTaskPanel(panelDim)
                                    .setTask(new Experiment.TunnelTask(nPrBlocks));
                }
            }

            mPracticePanel.setOpaque(true);
            mPracticePanel.setBackground(Color.WHITE);
            mPracticePanel.setMouseEnabled(true);
            mPracticePanel.setPracticeMode(true);

            getContentPane().add(mPracticePanel);
            mPracticePanel.requestFocusInWindow();
            mPracticePanel.start();
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
            if (mPracticePanel != null) mPracticePanel.repaint();
        }
    }
}
