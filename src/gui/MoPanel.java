package gui;

import javax.swing.*;
import java.awt.*;

public class MoPanel extends JPanel {

    protected void translate(int dX, int dY) {
        Point location = getLocation();
        location.translate(dX, dY);
        setLocation(location);
    }
}
