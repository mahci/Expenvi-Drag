package experiment;

import graphic.MoRectangle;
import tools.Out;

import java.awt.*;
import java.util.List;

import static experiment.Experiment.*;
import static java.lang.Math.*;

public class BoxTrial extends Trial {
    private final String NAME = "BoxTrial/";

    public MoRectangle objectRect = new MoRectangle();
//    public MoPanel targetPanel = new MoPanel();
    public MoRectangle targetRect = new MoRectangle();

    // Vraiables
    private AXIS axis;

    // Cosntants and randoms
    private DIRECTION dir;
    private int dist; // Corner-to-corner (diag) or edge-to-edge (straight)

    public BoxTrial(List<Integer> conf, int... params) {
        super(conf, params);

        if (conf == null || conf.size() < 3) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        //-- Set constants
        if (params != null && params.length > 0) {
            dist = params[0];
        }

        //-- Set variables
        objectRect.setSize(conf.get(0), conf.get(0));
//        targetPanel.setSize(conf.get(1), conf.get(1));
        targetRect.setSize(conf.get(1), conf.get(1));
        axis = AXIS.get(conf.get(2));

        dir = axis.randDir();

        // Set the bound box size based on the axis
        Out.d(NAME, objectRect.width,  (dist / sqrt(2)), targetRect.width,
                (int) (objectRect.width + (dist / sqrt(2)) + targetRect.width));
        switch (axis) {
            case VERTICAL -> {
                boundRect.setSize(
                        targetRect.width,
                        objectRect.width + dist + targetRect.width);
            }
            case HORIZONTAL -> {
                boundRect.setSize(
                        objectRect.width + dist + targetRect.width,
                        targetRect.width);
            }
            case FOR_DIAG, BACK_DIAG -> {
                boundRect.setSize(
                        (int) (objectRect.width + (dist / sqrt(2)) + targetRect.width),
                        (int) (objectRect.width + (dist / sqrt(2)) + targetRect.width)); // Always square
            }
        }

//        Out.d(NAME, objectRect.width,  (dist / sqrt(2)), targetPanel.getWidth(),
//                (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth()));
//        switch (axis) {
//            case VERTICAL -> {
//                boundRect.setSize(
//                        targetPanel.getWidth(),
//                        objectRect.width + dist + targetPanel.getWidth());
//            }
//            case HORIZONTAL -> {
//                boundRect.setSize(
//                        objectRect.width + dist + targetPanel.getWidth(),
//                        targetPanel.getWidth());
//            }
//            case FOR_DIAG, BACK_DIAG -> {
//                boundRect.setSize(
//                        (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth()),
//                        (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth())); // Always square
//            }
//        }

    }

    @Override
    public Point getEndPoint() {
        return targetRect.center();
//        final Rectangle tgtRect = targetPanel.getBounds();
//        return new Point((int) tgtRect.getCenterX(), (int) tgtRect.getCenterY());
    }

    @Override
    protected void positionElements() {
        final String TAG = NAME + "setElementsLocations";
//        super.positionElements();

        switch (dir) {
            case N -> {
                Out.d(TAG, "dir: N");
                objectRect.setLocation(
                        (int) (boundRect.getCenterX() - objectRect.width / 2),
                        boundRect.loLeft().y - objectRect.width);
                targetRect.setLocation(boundRect.getLocation());
            }

            case S -> {
                Out.d(TAG, "dir: S");
                objectRect.setLocation(
                        (int) (boundRect.getCenterX() - objectRect.width / 2),
                        boundRect.y);
                targetRect.setLocation(
                        boundRect.x,
                        boundRect.loLeft().y - targetRect.width);
            }

            case E -> {
                Out.d(TAG, "dir: E");
                objectRect.setLocation(
                        boundRect.x,
                        (int) (boundRect.getCenterY() - objectRect.width / 2));
                targetRect.setLocation(
                        boundRect.topRight().x - targetRect.width,
                        boundRect.y);
            }

            case W -> {
                Out.d(TAG, "dir: W");
                objectRect.setLocation(
                        boundRect.topRight().x - objectRect.width,
                        (int) (boundRect.getCenterY() - objectRect.width / 2));
                targetRect.setLocation(boundRect.getLocation());
            }

            case NE -> {
                Out.d(TAG, "dir: NE");
                objectRect.setLocation(
                        boundRect.x,
                        boundRect.loLeft().y - objectRect.width);
                targetRect.setLocation(
                        boundRect.topRight().x - targetRect.width,
                        boundRect.y);
            }

            case NW -> {
                Out.d(TAG, "dir: NW");
                objectRect.setLocation(
                        boundRect.loRight().x - objectRect.width,
                        boundRect.loRight().y - objectRect.width);
                targetRect.setLocation(boundRect.getLocation());
            }

            case SE -> {
                Out.d(TAG, "dir: SE");
                objectRect.setLocation(boundRect.getLocation());
                targetRect.setLocation(
                        boundRect.loRight().x - targetRect.width,
                        boundRect.loRight().y - targetRect.width);
            }

            case SW -> {
                Out.d(TAG, "dir: SW");
                objectRect.setLocation(
                        boundRect.topRight().x - objectRect.width,
                        boundRect.y);
                targetRect.setLocation(
                        boundRect.x,
                        boundRect.loLeft().y - targetRect.width);
            }
            
        }
    }

    @Override
    public String toString() {
        return "BoxTrial{" +
                "boundRect=" + boundRect +
                ", objectRect=" + objectRect +
                ", targetRect=" + targetRect +
                ", axis=" + axis +
                ", dir=" + dir +
                ", dist=" + dist +
                '}';
    }
}
