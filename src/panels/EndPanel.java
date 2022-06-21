package panels;

import javax.swing.*;
import java.awt.*;

import static experiment.Experiment.*;

import static tools.Consts.*;

public class EndPanel extends JPanel {

    MODE mMode;

    /**
     * Constructor
     */
    public EndPanel(TASK task, TECHNIQUE technique, MODE mode) {

        mMode = mode;

        if (mode.equals(MODE.TEST)) setBackground(task.getBgColor());
        else setBackground(Color.WHITE);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel textLabel = new JLabel();
        if (mode.equals(MODE.PRACTICE)) textLabel.setText("You have got it now!");
        else textLabel.setText("Thank You!");
        textLabel.setAlignmentX(CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Sans", Font.PLAIN, 70));

        String text = "";
        if (mode.equals(MODE.PRACTICE)) text = task.getTitle() + " with " + technique.getTitle();
        else text = task.getTitle() + " with " + technique.getTitle() + " is finished";
        JLabel explanLabel = new JLabel(text, JLabel.CENTER);
        explanLabel.setForeground(task.getFgColor());
        explanLabel.setAlignmentX(CENTER_ALIGNMENT);
        explanLabel.setFont(new Font("Sans", Font.PLAIN, 25));

        add(Box.createVerticalStrut(500)); // Top space
        add(textLabel);
        add(Box.createVerticalStrut(50)); // space
        add(explanLabel);
    }

}
