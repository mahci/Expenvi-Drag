package gui;

import experiment.Experiment;
import tools.Logs;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final static String NAME = "MainFrame/";
    // -------------------------------------------------------------------------------------------
    private static MainFrame self; // Singelton instance

    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;

    private static DemoPanel mDemoPanel;
    private static ExperimentPanel mExperimentPanel;

    private Experiment mExperiment;

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();

        setBackground(Color.WHITE);

        // Create and show an experiment
        final int pid = 126;
        mExperiment = new Experiment(pid);
    }

    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    public void start() {
        showDemo();
    }

    public void showDemo() {
        mDemoPanel = new DemoPanel(mExperiment);
        add(mDemoPanel);
        pack();

        setVisible(true);
    }

    public void showExperiment() {
        getContentPane().removeAll();

        mExperimentPanel = new ExperimentPanel(mExperiment);
        add(mExperimentPanel);
        mExperimentPanel.requestFocusInWindow();

        revalidate();
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

        // Set mm size in pixels
//        Logs.info("TAG", Toolkit.getDefaultToolkit().getScreenResolution());
    }

    public static void scroll(int vtScrollAmt, int hzScrollAmt) {
        if (mExperimentPanel != null) mExperimentPanel.scroll(vtScrollAmt, hzScrollAmt);
        else if (mDemoPanel != null) mDemoPanel.scroll(vtScrollAmt, hzScrollAmt);
    }

}
