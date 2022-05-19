package gui;

import control.Server;
import experiment.Experiment;
import tools.Consts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame implements MouseListener {
    private final static String NAME = "MainFrame/";
    // -------------------------------------------------------------------------------------------
    private static MainFrame self; // Singelton instance

    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;

    private Experiment mExperiment;

    private TaskPanel mActivePanel;

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();
        setBackground(Color.WHITE);

        addMouseListener(this);
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
    }

    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    public void start() {
//        Server.get().start();

        // Show the Intro panel
        final AbstractAction showTaskAA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTaskPanel();
            }
        };
        IntroPanel stPanel = new IntroPanel("M", "Window", showTaskAA);
        add(stPanel);
        setVisible(true);
    }

    private void showTaskPanel() {
        final Dimension panelDim = getContentPane().getSize();

        getContentPane().removeAll();

        mActivePanel = new BoxTaskPanel(panelDim).setTask(new Experiment.BoxTask(1));
//        mActivePanel = new BarTaskPanel(panelDim).setTask(new Experiment.BarTask(1));
//        mActivePanel = new TunnelTaskPanel(panelDim).setTask(new Experiment.TunnelTask(1));

        mActivePanel.setOpaque(true);
        mActivePanel.setBackground(Color.WHITE);

        getContentPane().add(mActivePanel);
        mActivePanel.requestFocusInWindow();
        mActivePanel.start();

        repaint();
    }


    public void showDialog(JDialog dialog) {
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

    public void cancel() {
        if (mActivePanel != null) mActivePanel.cancel();
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

    // Panels --------------------------------------------------------------------------------------------
    private static class IntroPanel extends JPanel {
        private KeyStroke KS_SPACE;

        public IntroPanel(String device, String task, AbstractAction spaceAction) {
            KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(true);
            setBackground(Color.WHITE);

            add(Box.createRigidArea(new Dimension(0, 600)));

            // Instruction label
            JLabel instructLabel = new JLabel("When ready, press SPACE to start", JLabel.CENTER);
            instructLabel.setAlignmentX(CENTER_ALIGNMENT);
            instructLabel.setFont(new Font("Sans", Font.BOLD, 50));
            instructLabel.setForeground(Consts.COLORS.GRAY_900);
            add(instructLabel);

            add(Box.createRigidArea(new Dimension(0, 200)));

            // Device-task panel
            JPanel deviceTaskPnl = new JPanel();
            deviceTaskPnl.setLayout(new BoxLayout(deviceTaskPnl, BoxLayout.X_AXIS));
            deviceTaskPnl.setAlignmentX(CENTER_ALIGNMENT);
            deviceTaskPnl.setMaximumSize(new Dimension(500, 60));
            deviceTaskPnl.setOpaque(true);
            deviceTaskPnl.setBackground(Color.WHITE);
            deviceTaskPnl.add(Box.createHorizontalGlue());

            JLabel deviceLbl = new JLabel(device, SwingConstants.CENTER);
            deviceLbl.setFont(new Font("Sans", Font.BOLD, 30));
            deviceLbl.setPreferredSize(new Dimension(200, 0));
            deviceLbl.setForeground(Consts.COLORS.BLUE_900);
            deviceTaskPnl.add(deviceLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel dashLbl = new JLabel("-", SwingConstants.CENTER);
            dashLbl.setFont(new Font("Sans", Font.BOLD, 50));
            dashLbl.setForeground(Consts.COLORS.GRAY_900);
            dashLbl.setPreferredSize(new Dimension(50, 0));
            deviceTaskPnl.add(dashLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel taskLbl = new JLabel(task, SwingConstants.CENTER);
            taskLbl.setFont(new Font("Sans", Font.BOLD, 30));
            taskLbl.setForeground(Consts.COLORS.GRAY_900);
            taskLbl.setPreferredSize(new Dimension(200, 0));
            deviceTaskPnl.add(taskLbl);

            deviceTaskPnl.add(Box.createHorizontalGlue());

            add(deviceTaskPnl);

            // Set action
            getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
            getActionMap().put(KeyEvent.VK_SPACE, spaceAction);

        }
    }
}
