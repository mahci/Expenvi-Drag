package dialogs;

import panels.MainFrame;
import tools.Consts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static tools.Consts.STRINGS.DLG_BREAK_TEXT;
import static tools.Consts.STRINGS.DLG_BREAK_TITLE;

public class PracticeBreakDialog extends JDialog implements KeyListener {

    public PracticeBreakDialog() {
        setTitle("BREAK");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 300));
        setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainFrame.get().mActiveTask.getBgColor());

        JLabel textLabel = new JLabel("It was easy, wasn't it?");
        textLabel.setAlignmentX(CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Sans", Font.PLAIN, 30));

        panel.add(Box.createVerticalStrut(120)); // Top space
        panel.add(textLabel);

        add(panel);
        addKeyListener(this);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) setVisible(false);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
