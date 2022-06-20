package panels;

import javax.swing.*;
import java.awt.*;

import static experiment.Experiment.*;

import static tools.Consts.*;

public class EndPanel extends JPanel {

    boolean mPracticeMode = false;

    /**
     * Constructor
     */
    public EndPanel(TASK task, TECHNIQUE technique, boolean prMode) {

        mPracticeMode = prMode;

        if (!mPracticeMode) setBackground(task.getBgColor());
        else setBackground(Color.WHITE);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel textLabel = new JLabel();
        if (!mPracticeMode) textLabel.setText("Thank You!");
        else textLabel.setText("You have got it now!");
        textLabel.setAlignmentX(CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Sans", Font.PLAIN, 70));

        String text = "";
        if (!mPracticeMode) text = task.getTitle() + " with " + technique.getTitle() + " is finished";
        else text = task.getTitle() + " with " + technique.getTitle();
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
