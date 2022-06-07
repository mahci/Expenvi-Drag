package experiment;

import graphic.MoRectangle;
import tools.Out;

import java.awt.*;
import java.util.List;

import static experiment.Experiment.*;

public class PeekTrial extends Trial {
    private final String NAME = "PeekTrial/";

    public MoRectangle objectRect = new MoRectangle();
    public MoRectangle curtainRect = new MoRectangle();
    public MoRectangle tempRect = new MoRectangle();
    public MoRectangle targetRect = new MoRectangle();

    // Vraiables
    public AXIS axis;

    // Cosntants and randoms
    private DIRECTION dir;
    private int dist;
    private int len;
    private int tempW;

    /**
     * Constructor
     * @param conf [0] Obj W, [1] Target W
     * @param params [0] len = ObjLen = TgtLen, [1] Distance, [2] Temp W
     */
    public PeekTrial(List<Integer> conf, int... params) {
        super(conf, params);

        if (conf == null || conf.size() < 2) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        //-- Set constants
        if (params != null && params.length == 3) {
            len = params[0];
            dist = params[1];
            tempW = params[2];
        } else {
            Out.e(NAME, "Params not passed correctly!");
        }

        //-- Set variables
        axis = AXIS.get(conf.get(2));

        switch (axis) {
            case VERTICAL -> { // N-S
                objectRect.setSize(len, conf.get(0));
                targetRect.setSize(len, conf.get(1));
                tempRect.setSize(len, tempW);

                boundRect.setSize(
                        len,
                        targetRect.height + dist + tempRect.height);

                // Curtain's size depends on the object movement range
                curtainRect.setSize(len, boundRect.height - objectRect.height);
            }

            case HORIZONTAL -> { // E-W
                objectRect.setSize(conf.get(0), len);
                targetRect.setSize(conf.get(1), len);
                tempRect.setSize(tempW, len);

                boundRect.setSize(
                        targetRect.width + dist + tempRect.width,
                        len);

                // Curtain's size depends on the object movement range
                curtainRect.setSize(boundRect.width - objectRect.width, len);
            }
        }

        // Random direction
        dir = axis.randDir();

    }

    @Override
    public Point getEndPoint() {
        return targetRect.center();
    }

    public boolean isPointInRange(Point p) {
        switch (dir) {
            case N -> {
                return p.y > tempRect.minY();
            }

            case S -> {
                return p.y < tempRect.maxY();
            }

            case E -> {
                return p.x < tempRect.maxX();
            }

            case W -> {
                return p.x > tempRect.minX();
            }
        }

        return false;
    }

    @Override
    protected void positionElements() {
        super.positionElements();

        switch (dir) {
            case N -> {
                tempRect.setLocation(boundRect.topLeft());
                targetRect.setLocationLoLeft(boundRect.loLeft());
                objectRect.setLocationLoLeft(boundRect.loLeft());

                curtainRect.setLocation(boundRect.topLeft());
            }

            case S -> {
                objectRect.setLocation(boundRect.topLeft());
                targetRect.setLocation(boundRect.topLeft());
                tempRect.setLocationLoLeft(boundRect.loLeft());

                curtainRect.setLocation(objectRect.loLeft());
            }

            case E -> {
                objectRect.setLocation(boundRect.topLeft());
                targetRect.setLocation(boundRect.topLeft());
                tempRect.setLocationTopRight(boundRect.topRight());

                curtainRect.setLocation(objectRect.topRight());
            }

            case W -> {
                tempRect.setLocation(boundRect.topLeft());
                objectRect.setLocationTopRight(boundRect.topRight());
                targetRect.setLocationTopRight(boundRect.topRight());

                curtainRect.setLocation(boundRect.topLeft());
            }
        }
    }

    public void moveObject(int dX, int dY) {

        switch (dir) {
            case N -> {
                final int yTopLimit = boundRect.y;
                final int newY = objectRect.y + dY;

                if (newY > yTopLimit) {
                    objectRect.y = newY;
                    curtainRect.resize(DIRECTION.S, dY, dX);
                }
            }

            case S -> {
                final int yBottomLimit = boundRect.loLeft().y - objectRect.height;
                final int newY = objectRect.y + dY;

                if (newY < yBottomLimit) {
                    objectRect.y = newY;
                    curtainRect.resize(DIRECTION.N, dY, dX);
                }
            }

            case E -> {
                final int xRightLimit = boundRect.topRight().x - objectRect.width;
                final int newX = objectRect.x + dX;

                if (newX < xRightLimit) {
                    objectRect.x = newX;
                    curtainRect.resize(DIRECTION.W, dY, dX);
                }
            }

            case W -> {
                final int xLeftLimit = boundRect.x;
                final int newX = objectRect.x + dX;

                if (newX > xLeftLimit) {
                    objectRect.x = newX;
                    curtainRect.resize(DIRECTION.E, dY, dX);
                }
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
