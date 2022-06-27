package experiment;

import graphic.MoRectangle;
import jdk.jshell.execution.Util;
import tools.Out;
import tools.Utils;

import java.awt.*;
import java.util.List;

import static tools.Consts.*;
import static tools.Consts.STRINGS.SP;

public class PeekTrial extends Trial {
    private final String NAME = "PeekTrial/";

    public MoRectangle objectRect = new MoRectangle();
    public MoRectangle curtainRect = new MoRectangle();
    public MoRectangle tempRect = new MoRectangle();
    public MoRectangle targetRect = new MoRectangle();

    // Factors
    private int fObjectWidth, fTargettWidth;
    private AXIS fAxis;

    // Init positions for reverting
    private Point initObjPosition = new Point();
    private MoRectangle initCurtainRect = new MoRectangle();

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

        // Set factors
        fObjectWidth = conf.get(0);
        fTargettWidth = conf.get(1);
        fAxis = AXIS.get(conf.get(2));

        // Set params
        if (params != null && params.length == 3) {
            len = Utils.mm2px(params[0]);
            dist = Utils.mm2px(params[1]);
            tempW = Utils.mm2px(params[2]);
        } else {
            Out.e(NAME, "Params not passed correctly!");
        }

        // Random direction
        dir = fAxis.randDir();

        //-- Set sizes
        switch (fAxis) {
            case VERTICAL -> { // N-S
                objectRect.setSize(len, Utils.mm2px(fObjectWidth));
                targetRect.setSize(len, Utils.mm2px(fTargettWidth));
                tempRect.setSize(len, tempW);

                boundRect.setSize(
                        len,
                        targetRect.height + dist + tempRect.height);

                // Curtain's size depends on the object movement range
                curtainRect.setSize(len, boundRect.height - objectRect.height);
            }

            case HORIZONTAL -> { // E-W
                objectRect.setSize(Utils.mm2px(fObjectWidth), len);
                targetRect.setSize(Utils.mm2px(fTargettWidth), len);
                tempRect.setSize(tempW, len);

                boundRect.setSize(
                        targetRect.width + dist + tempRect.width,
                        len);

                // Curtain's size depends on the object movement range
                curtainRect.setSize(boundRect.width - objectRect.width, len);
            }
        }

    }

    public AXIS getAxis() {
        return fAxis;
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
        initCurtainRect = (MoRectangle) curtainRect.clone();
    }

    /**
     * Move the object to be under the point p (relative to the top-left corner of the object)
     * (with conditions)
     * @param relGrabP Point relative to the top-left corner of the object (= grab point)
     * @param newLoc The new location of the relGrabP
     */
    public void moveObject(Point relGrabP, Point newLoc) {
        final String TAG = NAME + "moveObject";

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
        objectRect.setLocation(initObjPosition);
        curtainRect = initCurtainRect;
    }

    @Override
    public String toLogString() {
        return boundRect.x + SP +
                boundRect.y + SP +
                fObjectWidth + SP +
                fTargettWidth + SP +
                dir.getAxis() + SP +
                dir;
    }

    @Override
    public String toString() {
        return toLogString();
    }
}
