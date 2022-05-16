package gui;

import tools.MinMax;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;

public class TaskPanel extends JLayeredPane {
    protected double TB_MARGIN_mm = 20;
    protected double LR_MARGIN_mm = 10;

    public void start() {
    }

    public boolean isHit() {
        return false;
    }

    public void grab() {

    }

    public void drag() {

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

    protected MinMax getWidthMinMax() {
        final int hzMargin = Utils.mm2px(LR_MARGIN_mm);
        return new MinMax(hzMargin, getWidth() - hzMargin);
    }

    protected MinMax getHeightMinMax() {
        final int vtMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MinMax(vtMargin, getHeight() - vtMargin);
    }

    protected MoRectangle getPanelBounds() {
        final int wMargin = Utils.mm2px(LR_MARGIN_mm);
        final int hMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MoRectangle(
                wMargin, hMargin,
                getWidth() - wMargin, getHeight() - hMargin);
    }

    protected boolean isXInRange(int x) {
        Dimension dispDim = getDispDim();
        return x > 0 && x < dispDim.width;
    }

//    protected Point toPanel(Point inP) {
//        return new Point(inP.x + Utils.mm2px(LR_MARGIN_mm), inP.y + Utils.mm2px(TB_MARGIN_mm));
//    }

}
