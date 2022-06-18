package experiment;

import graphic.MoRectangle;
import tools.Out;
import tools.Utils;

import java.awt.*;
import java.util.List;

import static tools.Consts.*;
import static tools.Consts.STRINGS.SP;

public class BarTrial extends Trial {
    private final String NAME = "BarTrial/";

    public MoRectangle targetRect = new MoRectangle();
    public MoRectangle objectRect = new MoRectangle();

    // Factors
    private int fObjectWidth, fTargettWidth;
    private AXIS fAxis;

    // Cosntants and randoms
    private DIRECTION dir;
    private int dist; // Corner-to-corner (diag) or edge-to-edge (straight)
    private int objLen, tgtLen;

    /**
     * Constructor
     * @param conf [0] Obj W, [1] Target W
     * @param params [0] ObjLen, [1] TgtLen, [2] TgtLinesThickness, [3] Distance
     */
    public BarTrial(List<Integer> conf, int... params) {
        super(conf, params);

        if (conf == null || conf.size() < 3) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        // Set factors
        fObjectWidth = conf.get(0);
        fTargettWidth = conf.get(1);
        fAxis = AXIS.get(conf.get(2));

        // Set params
        if (params != null && params.length > 2) {
            objLen = Utils.mm2px(params[0]);
            tgtLen = Utils.mm2px(params[1]);
            dist = Utils.mm2px(params[2]);
        } else {
            Out.e(NAME, "Params not passed correctly!");
        }

        // Random direction
        dir = fAxis.randDir();

        //-- Set variables
        switch (fAxis) {

            case VERTICAL -> { // N-S
                objectRect.setSize(objLen, Utils.mm2px(fObjectWidth));
                targetRect.setSize(tgtLen, Utils.mm2px(fTargettWidth));

                boundRect.setSize(
                        targetRect.width,
                        objectRect.height + dist + targetRect.height);
            }

            case HORIZONTAL -> { // E-W
                objectRect.setSize(Utils.mm2px(fObjectWidth), objLen);
                targetRect.setSize(Utils.mm2px(fTargettWidth), tgtLen);

                boundRect.setSize(
                        objectRect.height + dist + targetRect.height,
                        targetRect.height);
            }
        }

    }

    @Override
    public Point getEndPoint() {
        return targetRect.center;
    }

    @Override
    protected void positionElements() {
        super.positionElements();

        switch (dir) {
            case N -> {
                objectRect.setLocation(
                        boundRect.center.x - objectRect.width / 2,
                        boundRect.bottomLeft.y - objectRect.height);

                targetRect.setLocation(boundRect.getLocation());
            }

            case S -> {
                objectRect.setLocation(
                        boundRect.center.x - objectRect.width / 2,
                        boundRect.y);

                targetRect.setLocation(
                        boundRect.x,
                        boundRect.bottomLeft.y - targetRect.height);
            }

            case E -> {
                objectRect.setLocation(
                        boundRect.x,
                        boundRect.center.y - objectRect.height / 2);

                targetRect.setLocation(
                        boundRect.topRight.x - targetRect.width,
                        boundRect.y);
            }

            case W -> {
                objectRect.setLocation(
                        boundRect.topRight.x - objectRect.width,
                        boundRect.center.y - objectRect.height / 2);

                targetRect.setLocation(boundRect.getLocation());
            }

        }
    }

    @Override
    public String toLogString() {
        return boundRect.x + SP +
                boundRect.y + SP +
                fObjectWidth + SP +
                fTargettWidth + SP +
                dir.toString();
    }

    @Override
    public String toString() {
        return toLogString();
    }
}
