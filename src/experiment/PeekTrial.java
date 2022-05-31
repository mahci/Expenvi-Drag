package experiment;

import gui.MoRectangle;
import tools.Out;

import java.awt.*;
import java.util.List;

import static experiment.Experiment.*;

public class PeekTrial extends Trial {
    private final String NAME = "PeekTrial/";

    public MoRectangle targetRect = new MoRectangle();
    public MoRectangle curtainRect = new MoRectangle();
    public MoRectangle objectRect = new MoRectangle();

    // Vraiables
    public AXIS axis;

    // Cosntants and randoms
    private DIRECTION dir;
    private int dist; // Corner-to-corner (diag) or edge-to-edge (straight)
    private int len;


    /**
     * Constructor
     * @param conf [0] Obj W, [1] Target W
     * @param params [0] len = ObjLen = TgtLen, [1] Distance
     */
    public PeekTrial(List<Integer> conf, int... params) {
        super(conf, params);

        if (conf == null || conf.size() < 2) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        //-- Set constants
        if (params != null && params.length == 2) {
            len = params[0];
            dist = params[1];
        } else {
            Out.e(NAME, "Params not passed correctly!");
        }


        //-- Set variables
        axis = AXIS.get(conf.get(2));
        switch (axis) {

            case VERTICAL -> { // N-S
                objectRect.setSize(len, conf.get(0));
                curtainRect.setSize(len, dist);
                targetRect.setSize(len, conf.get(1));

                boundRect.setSize(
                        len,
                        conf.get(0) + dist + conf.get(1));
            }

            case HORIZONTAL -> { // E-W
                objectRect.setSize(conf.get(0), len);
                curtainRect.setSize(dist, len);
                targetRect.setSize(conf.get(1), len);

                boundRect.setSize(
                        conf.get(0) + dist + conf.get(1),
                        len);
            }
        }

        // Random direction
        dir = axis.randDir();

    }

    @Override
    public Point getEndPoint() {
        return targetRect.getCenter();
    }

    @Override
    protected void positionElements() {
        super.positionElements();

        switch (dir) {
            case N -> {
                targetRect.setLocation(boundRect.getUpLeft());
                curtainRect.setLocation(targetRect.getLoLeft());
                objectRect.setLocation(curtainRect.getLoLeft());
            }

            case S -> {
                objectRect.setLocation(boundRect.getUpLeft());
                curtainRect.setLocation(objectRect.getLoLeft());
                targetRect.setLocation(curtainRect.getLoLeft());
            }

            case E -> {
                objectRect.setLocation(boundRect.getUpLeft());
                curtainRect.setLocation(objectRect.getUpRight());
                targetRect.setLocation(curtainRect.getUpRight());
            }

            case W -> {
                targetRect.setLocation(boundRect.getUpLeft());
                curtainRect.setLocation(targetRect.getUpRight());
                objectRect.setLocation(curtainRect.getUpRight());
            }
        }
    }

    public void moveObject(int dX, int dY) {

        switch (dir) {
            case N -> {
                curtainRect.resize(DIRECTION.S, dY, dX);
                boundRect.resize(DIRECTION.S, dY, dX);

                objectRect.setLocation(curtainRect.getLoLeft());
            }

            case S -> {
                curtainRect.resize(DIRECTION.N, dY, dX);
                boundRect.resize(DIRECTION.N, dY, dX);

                objectRect.setLocation(boundRect.getUpLeft());
            }

            case E -> {
                curtainRect.resize(DIRECTION.W, dY, dX);
                boundRect.resize(DIRECTION.W, dY, dX);

                objectRect.setLocation(boundRect.getUpLeft());
            }

            case W -> {
                curtainRect.resize(DIRECTION.E, dY, dX);
                boundRect.resize(DIRECTION.E, dY, dX);

                objectRect.setLocation(curtainRect.getUpRight());
            }
        }
    }

    @Override
    public String toString() {
        return "PeekTrial{" +
                "targetRect=" + targetRect +
                ", curtainRect=" + curtainRect +
                ", objectRect=" + objectRect +
                ", axis=" + axis +
                ", dir=" + dir +
                ", dist=" + dist +
                ", len=" + len +
                '}';
    }
}
