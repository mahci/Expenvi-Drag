package gui;

import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;

public class TaskPanel extends JLayeredPane {
    protected double TB_MARGIN_mm = 10;
    protected double LR_MARGIN_mm = 10;

    public void start() {
    }

    public boolean isHit() {
        return false;
    }

    public void grab() {

    }

    public void release() {

    }

    public void cancel() {

    }

    protected Dimension getDispDim() {
        Dimension result = new Dimension();
        result.width = getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
        result.height = getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));

        return result;
    }

    protected int getDispW() {
        return getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
    }

    protected int getDispH() {
        return getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));
    }

    protected Area getDispArea() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);
        return new Area(new Rectangle(
                0, 0,
                getWidth() - 2 * lrMargin, getHeight() - 2 * tbMargin));
    }

    protected boolean isXInRange(int x) {
        Dimension dispDim = getDispDim();
        return x > 0 && x < dispDim.width;
    }

}
