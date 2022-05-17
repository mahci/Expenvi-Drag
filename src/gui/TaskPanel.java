package gui;

import tools.MinMax;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.util.Collections;
import java.util.List;

public class TaskPanel extends JLayeredPane {
    private final String NAME = "TaskPanel/";

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


    /**
     * Find a position for a trial
     * @param trBoundRect Boudning box of the trial
     * @param ptP Previous trial position (null if not reference)
     * @return The found point or null (if nothing found)
     */
    public Point findTrialPosition(Rectangle trBoundRect, Point ptP, int minNtDist, int maxNtDist) {
        final String TAG = NAME + "findTrialPosition";
        Out.d(TAG, "BoundRect", trBoundRect.toString());
        Out.d(TAG, "W | H", getWidthMinMax(), getHeightMinMax());
        Circle rangeCircle = new Circle();
        int ntDist = minNtDist;
        Out.d(TAG, "ptP | ntDist", ptP, ntDist);
        if (ptP != null) { // Contrained by the previous trial

            while (ntDist <= maxNtDist) {

                rangeCircle = new Circle(ptP, ntDist);
                final List<Point> rangePoints = rangeCircle.getPoints();
                Collections.shuffle(rangePoints); // Shuffle for random iteration

                Rectangle rect = trBoundRect;
                for (Point candP : rangePoints) {
                    rect.setLocation(candP);
                    if (getPanelBounds().contains(rect)) { // Fits the window?
                        return candP;
                    }
                }

                Out.d(TAG, "Distance checked", ntDist);
                ntDist += 5; // Increase by 10 px
            }

        } else {
            return getPanelBounds().fitRect(trBoundRect);
        }

        repaint();
        return null;
    }

//    protected Point toPanel(Point inP) {
//        return new Point(inP.x + Utils.mm2px(LR_MARGIN_mm), inP.y + Utils.mm2px(TB_MARGIN_mm));
//    }

}
