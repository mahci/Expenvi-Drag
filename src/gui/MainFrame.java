package gui;

import experiment.Experiment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainFrame extends JFrame implements MouseListener {
    private final static String NAME = "MainFrame/";
    // -------------------------------------------------------------------------------------------
    private static MainFrame self; // Singelton instance

    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;

//    private static ExperimentPanel mExperimentPanel;

    private Experiment mExperiment;

    private TestPanel mTestPanel;

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();

        setBackground(Color.WHITE);

        addMouseListener(this);
    }

    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    public void start() {
        final Dimension panelDim = getContentPane().getSize();

        mTestPanel = new TestPanel(panelDim);

        getContentPane().add(mTestPanel);
        mTestPanel.requestFocusInWindow();
        mTestPanel.start();
        setVisible(true);

//        repaint();
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
        if (mTestPanel != null) mTestPanel.grab();
    }

    public void release() {
        if (mTestPanel != null) mTestPanel.release();
    }

    public void cancel() {
        if (mTestPanel != null) mTestPanel.cancel();
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
}
