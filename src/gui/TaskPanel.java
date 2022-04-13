package gui;

import tools.Utils;

import javax.swing.*;
import java.awt.*;

public class TaskPanel extends JLayeredPane {
    protected double TB_MARGIN_mm = 20;
    protected double LR_MARGIN_mm = 20;

    public void start() {

    }

    public boolean isSuccessful() {
        return false;
    }

    protected Dimension getDispDim() {
        Dimension result = new Dimension();
        result.width = getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
        result.height = getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));

        return result;
    }
}
