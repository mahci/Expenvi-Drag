package gui;

import javax.swing.*;
import java.awt.*;

public class MoLabel extends JLabel {

    public void translate(int dX, int dY) {
        Point location = getLocation();
        location.translate(dX, dY);
        setLocation(location);
    }
}
