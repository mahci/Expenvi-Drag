package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static tools.Consts.*;
import static experiment.Experiment.*;

public class IntroPanel extends JPanel {
    private KeyStroke KS_SPACE;

    private boolean mDemoMode = false;
    private TECHNIQUE mTech;
    private TASK mTask;

    public IntroPanel(String text, TECHNIQUE technique, TASK task, AbstractAction spaceAction, boolean dMode) {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);

        mTech = technique;
        mTask = task;

        mDemoMode = dMode;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);

        if (mDemoMode) setBackground(Color.WHITE);
        else setBackground(task.getBgColor());

        add(Box.createRigidArea(new Dimension(0, 600)));

        // Instruction label
        JLabel instructLabel = new JLabel(text, JLabel.CENTER);
        instructLabel.setAlignmentX(CENTER_ALIGNMENT);
        instructLabel.setFont(new Font("Sans", Font.BOLD, 50));
        instructLabel.setForeground(COLORS.GRAY_900);
        add(instructLabel);

        add(Box.createRigidArea(new Dimension(0, 200)));

        // Device-task panel
        if (!mDemoMode) {
            JPanel deviceTaskPnl = new JPanel();
            deviceTaskPnl.setLayout(new BoxLayout(deviceTaskPnl, BoxLayout.X_AXIS));
            deviceTaskPnl.setAlignmentX(CENTER_ALIGNMENT);
            deviceTaskPnl.setMaximumSize(new Dimension(700, 60));
            deviceTaskPnl.setOpaque(true);
            deviceTaskPnl.setBackground(task.getBgColor());
            deviceTaskPnl.add(Box.createHorizontalGlue());

            JLabel taskLbl = new JLabel(task.getTitle(), SwingConstants.CENTER);
            taskLbl.setFont(new Font("Sans", Font.BOLD, 30));
            taskLbl.setForeground(mTask.getFgColor());
            taskLbl.setPreferredSize(new Dimension(200, 0));
            deviceTaskPnl.add(taskLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel dashLbl = new JLabel("—", SwingConstants.CENTER);
            dashLbl.setFont(new Font("Sans", Font.BOLD, 50));
            dashLbl.setForeground(COLORS.GRAY_900);
            dashLbl.setPreferredSize(new Dimension(50, 0));
            deviceTaskPnl.add(dashLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel techLbl = new JLabel(technique.getTitle(), SwingConstants.CENTER);
            techLbl.setFont(new Font("Sans", Font.BOLD, 30));
            techLbl.setPreferredSize(new Dimension(400, 0));
            techLbl.setForeground(COLORS.GRAY_900);
            deviceTaskPnl.add(techLbl);

            deviceTaskPnl.add(Box.createHorizontalGlue());

            add(deviceTaskPnl);
        }

        // Set action
        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getActionMap().put(KeyEvent.VK_SPACE, spaceAction);

    }
}
