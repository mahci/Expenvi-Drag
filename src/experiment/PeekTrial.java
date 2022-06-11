package experiment;

import graphic.MoRectangle;
import tools.Out;

import java.awt.*;
import java.util.List;

import static tools.Consts.*;

public class PeekTrial extends Trial {
    private final String NAME = "PeekTrial/";

    public MoRectangle objectRect = new MoRectangle();
    public MoRectangle curtainRect = new MoRectangle();
    public MoRectangle tempRect = new MoRectangle();
    public MoRectangle targetRect = new MoRectangle();

    // Init positions for reverting
    private Point initObjPosition = new Point();
//    private Point initCurtainPosition = new Point();

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
        return targetRect.center;
    }

    public boolean isPointInRange(Point p) {
        switch (dir) {
            case N -> {
                return p.y > tempRect.minY;
            }

            case S -> {
                return p.y < tempRect.maxY;
            }

            case E -> {
                return p.x < tempRect.maxX;
            }

            case W -> {
                return p.x > tempRect.minX;
            }
        }

        return false;
    }

    @Override
    protected void positionElements() {
        super.positionElements();

        switch (dir) {
            case N -> {
                tempRect.setLocation(boundRect.topLeft);
                targetRect.setLocationBottomLeft(boundRect.bottomLeft);
                objectRect.setLocationBottomLeft(boundRect.bottomLeft);

                curtainRect.setLocation(boundRect.topLeft);
            }

            case S -> {
                objectRect.setLocation(boundRect.topLeft);
                targetRect.setLocation(boundRect.topLeft);
                tempRect.setLocationBottomLeft(boundRect.bottomLeft);

                curtainRect.setLocation(objectRect.bottomLeft);
            }

            case E -> {
                objectRect.setLocation(boundRect.topLeft);
                targetRect.setLocation(boundRect.topLeft);
                tempRect.setLocationTopRight(boundRect.topRight);

                curtainRect.setLocation(objectRect.topRight);
            }

            case W -> {
                tempRect.setLocation(boundRect.topLeft);
                objectRect.setLocationTopRight(boundRect.topRight);
                targetRect.setLocationTopRight(boundRect.topRight);

                curtainRect.setLocation(boundRect.topLeft);
            }
        }

        // Set the initial positions
        initObjPosition = objectRect.getLocation();
//        initCurtainPosition = curtainRect.getLocation();
    }

    /**
     * Move the object while holding the curtain constraints
     * @param dX Movement in X
     * @param dY Movement in Y
     */
//    public void moveObject(int dX, int dY) {
//
//        switch (dir) {
//            case N -> {
//                final int yTopLimit = boundRect.y;
//                final int newY = objectRect.y + dY;
//
//                if (newY > yTopLimit) {
//                    objectRect.y = newY;
//                    curtainRect.resize(DIRECTION.S, dY, dX);
//                }
//            }
//
//            case S -> {
//                final int yBottomLimit = boundRect.bottomLeft.y - objectRect.height;
//                final int newY = objectRect.y + dY;
//
//                if (newY < yBottomLimit) {
//                    objectRect.y = newY;
//                    curtainRect.resize(DIRECTION.N, dY, dX);
//                }
//            }
//
//            case E -> {
//                final int xRightLimit = boundRect.topRight.x - objectRect.width;
//                final int newX = objectRect.x + dX;
//
//                if (newX < xRightLimit) {
//                    objectRect.x = newX;
//                    curtainRect.resize(DIRECTION.W, dY, dX);
//                }
//            }
//
//            case W -> {
//                final int xLeftLimit = boundRect.x;
//                final int newX = objectRect.x + dX;
//
//                if (newX > xLeftLimit) {
//                    objectRect.x = newX;
//                    curtainRect.resize(DIRECTION.E, dY, dX);
//                }
//            }
//        }
//    }

    /**
     * Move the object to be under the point p (relative to the top-left corner of the object)
     * (with conditions)
     * @param relGrabP Point relative to the top-left corner of the object (= grab point)
     * @param newLoc The new location of the relGrabP
     */
    public void moveObject(Point relGrabP, Point newLoc) {
        final String TAG = NAME + "moveObject";

        Out.d(TAG, relGrabP, newLoc);
        switch (dir) {
            case N -> {
                Point objNewTL = new Point();
                objNewTL.x = objectRect.x; // X doesn't change
                if (isPointInRange(newLoc)) {
                    objNewTL.y = newLoc.y - relGrabP.y;
                } else { // Past the tempRect => just stick the object to the end!
                    objNewTL = tempRect.topLeft;
                }

                objectRect.setLocationTopLeft(objNewTL);
                curtainRect.resizeXY(DIRECTION.S, -1, objectRect.topLeft.y); // Stick to the object
            }

            case S -> {
                Point objNewBL = new Point();
                objNewBL.x = objectRect.x; // X doesn't change
                if (isPointInRange(newLoc)) {
                    objNewBL.y = (newLoc.y - relGrabP.y) + objectRect.height; // relGrabP is relative to *TL*
                } else { // Past the tempRect => just stick the object to the end!
                    objNewBL = tempRect.bottomLeft;
                }

                objectRect.setLocationBottomLeft(objNewBL);
                curtainRect.resizeXY(DIRECTION.N, -1, objectRect.bottomLeft.y); // Stick to the object
            }

            case E -> {
                Point objNewTR = new Point();
                objNewTR.y = objectRect.y; // Y doesn't change
                if (isPointInRange(newLoc)) {
                    objNewTR.x = (newLoc.x - relGrabP.x) + objectRect.width; // relGrabP is relative to *TL*
                } else { // Past the tempRect => just stick the object to the end!
                    objNewTR = tempRect.topRight;
                }

                objectRect.setLocationTopRight(objNewTR);
                curtainRect.resizeXY(DIRECTION.W, objectRect.topRight.x, -1); // Stick to the object
            }

            case W -> {
                Point objNewTL = new Point();
                objNewTL.y = objectRect.y; // Y doesn't change
                if (isPointInRange(newLoc)) {
                    objNewTL.x = newLoc.x - relGrabP.x;
                } else { // Past the tempRect => just stick the object to the end!
                    objNewTL = tempRect.topLeft;
                }

                objectRect.setLocationTopLeft(objNewTL);
                curtainRect.resizeXY(DIRECTION.E, objectRect.topLeft.x, -1); // Stick to the object
            }
        }
    }

    // TODO: change to relative moveObject()
    public void revertObject() {
//        moveObject(initObjPosition.x - objectRect.x, initObjPosition.y - objectRect.y);
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
