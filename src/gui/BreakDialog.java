package gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static tools.Consts.STRINGS.*;

public class BreakDialog extends JDialog implements KeyListener {
    private final static String NAME = "BreakDialog/";

    public BreakDialog() {
        setTitle("BREAK");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 400));
        setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.ORANGE);

        JLabel textLabel = new JLabel(DLG_BREAK_TITLE);
        textLabel.setAlignmentX(CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Sans", Font.PLAIN, 30));

        JLabel instLabel = new JLabel(DLG_BREAK_TEXT, JLabel.CENTER);
        instLabel.setAlignmentX(CENTER_ALIGNMENT);
        instLabel.setFont(new Font("Sans", Font.PLAIN, 20));

        panel.add(Box.createVerticalStrut(100)); // Top space
        panel.add(textLabel);
        panel.add(Box.createVerticalStrut(50)); // space
        panel.add(instLabel);

        add(panel);
        addKeyListener(this);
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        // Close on Shift + \
        if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_BACK_SLASH) {
            setVisible(false);
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
