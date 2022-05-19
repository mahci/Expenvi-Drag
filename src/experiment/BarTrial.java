package experiment;

import gui.MoRectangle;
import tools.Out;

import java.awt.*;
import java.util.List;

public class BarTrial extends Trial {
    private final String NAME = "BarTrial/";

    public MoRectangle line1Rect = new MoRectangle();
    public MoRectangle line2Rect = new MoRectangle();
    public MoRectangle inRect = new MoRectangle();

    public MoRectangle objectRect = new MoRectangle();

    // Vraiables
    private Experiment.AXIS axis;

    // Cosntants and randoms
    private Experiment.DIRECTION dir;
    private int dist; // Corner-to-corner (diag) or edge-to-edge (straight)
    private int objLen, tgtLen, tgtLinesThickness;

    /**
     * Constructor
     * @param conf Configuration list
     * @param params ObjLen, TgtLen, TgtLinesThickness, Distance
     */
    public BarTrial(List<Integer> conf, int... params) {
        super(conf, params);

        if (conf == null || conf.size() < 3) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        //-- Set constants
        if (params != null && params.length == 4) {
            objLen = params[0];
            tgtLen = params[1];
            tgtLinesThickness = params[2];
            dist = params[3];
        } else {
            Out.e(NAME, "Params not passed correctly!");
        }

        //-- Set variables
        axis = Experiment.AXIS.get(conf.get(2));
        switch (axis) { // conf(0) -> objW | conf(1) -> tgtW
            case VERTICAL -> {
                objectRect.setSize(objLen, conf.get(0));
                line1Rect.setSize(tgtLen, tgtLinesThickness);
                line2Rect.setSize(line1Rect.getSize());
                inRect.setSize(tgtLen, conf.get(1));

                boundRect.setSize(
                        tgtLen,
                        dist + inRect.height + line1Rect.height);
            }
            case HORIZONTAL -> {
                objectRect.setSize(conf.get(0), objLen);
                line1Rect.setSize(tgtLinesThickness, tgtLen);
                line2Rect.setSize(line1Rect.getSize());
                inRect.setSize(conf.get(1), tgtLen);

                boundRect.setSize(
                        dist + inRect.width + line1Rect.width,
                        tgtLen);
            }
        }

        // Random direction
        dir = axis.randDir();

    }

    @Override
    public Point getEndPoint() {
        return inRect.getCenter();
    }

    @Override
    public void setElementsLocations() {
        super.setElementsLocations();

        switch (dir) {
            case N -> {
                objectRect.setLocation(
                        boundRect.getCenter().x - objectRect.width / 2,
                        boundRect.getLoLeft().y - objectRect.height);

                line1Rect.setLocation(boundRect.getLocation());
                inRect.setLocation(line1Rect.getLoLeft());
                line2Rect.setLocation(inRect.getLoLeft());
            }

            case S -> {
                objectRect.setLocation(
                        boundRect.getCenter().x - objectRect.width / 2,
                        boundRect.y);

                line1Rect.setLocation(boundRect.x, boundRect.getLoLeft().y - line1Rect.height);
                inRect.setLocation(boundRect.x, line1Rect.y - inRect.height);
                line2Rect.setLocation(boundRect.x, inRect.y - line2Rect.height);
            }

            case E -> {
                objectRect.setLocation(
                        boundRect.x,
                        boundRect.getCenter().y - objectRect.height / 2);

                line1Rect.setLocation(boundRect.getUpRight().x - line1Rect.width, boundRect.y);
                inRect.setLocation(line1Rect.x - inRect.width, boundRect.y);
                line2Rect.setLocation(inRect.x - line2Rect.width, boundRect.y);
            }

            case W -> {
                objectRect.setLocation(
                        boundRect.getUpRight().x - objectRect.width,
                        boundRect.getCenter().y - objectRect.height / 2);

                line1Rect.setLocation(boundRect.getLocation());
                inRect.setLocation(line1Rect.getUpRight());
                line2Rect.setLocation(inRect.getUpRight());
            }

        }
    }

    @Override
    public String toString() {
        return "BarTrial{" +
                "line1Rect=" + line1Rect +
                ", line2Rect=" + line2Rect +
                ", inRect=" + inRect +
                ", objectRect=" + objectRect +
                ", axis=" + axis +
                ", dir=" + dir +
                ", dist=" + dist +
                ", objLen=" + objLen +
                ", tgtLen=" + tgtLen +
                ", tgtLinesThickness=" + tgtLinesThickness +
                '}';
    }
}
