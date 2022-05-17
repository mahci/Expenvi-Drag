package experiment;

import gui.MoPanel;
import tools.Out;

import java.awt.*;
import java.util.List;

import static experiment.Experiment.*;
import static java.lang.Math.*;

public class BoxTrial extends Trial {
    private final String NAME = "BoxTrial/";

    public Rectangle objectRect = new Rectangle();
    public MoPanel targetPanel = new MoPanel();

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
        targetPanel.setSize(conf.get(1), conf.get(1));
        axis = AXIS.get(conf.get(2));

        dir = axis.randDir();

        // Set the bound box size based on the axis
        Out.d(NAME, objectRect.width,  (dist / sqrt(2)), targetPanel.getWidth(),
                (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth()));
        switch (axis) {
            case VERTICAL -> {
                boundRect.setSize(
                        targetPanel.getWidth(),
                        objectRect.width + dist + targetPanel.getWidth());
            }
            case HORIZONTAL -> {
                boundRect.setSize(
                        objectRect.width + dist + targetPanel.getWidth(),
                        targetPanel.getWidth());
            }
            case FOR_DIAG, BACK_DIAG -> {
                boundRect.setSize(
                        (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth()),
                        (int) (objectRect.width + (dist / sqrt(2)) + targetPanel.getWidth())); // Always square
            }
        }

    }

    @Override
    public Point getEndPoint() {
        final Rectangle tgtRect = targetPanel.getBounds();
        return new Point((int) tgtRect.getCenterX(), (int) tgtRect.getCenterY());
    }

    @Override
    public void setElementsLocations() {
        super.setElementsLocations();

        switch (dir) {
            case N -> {
                objectRect.setLocation(
                        (int) (boundRect.getCenterX() - objectRect.width / 2),
                        boundRect.getLoLeft().y - objectRect.width);
                targetPanel.setLocation(boundRect.getLocation());
            }

            case S -> {
                objectRect.setLocation(
                        (int) (boundRect.getCenterX() - objectRect.width / 2),
                        boundRect.y);
                targetPanel.setLocation(
                        boundRect.x,
                        boundRect.getLoLeft().y - targetPanel.getWidth());
            }

            case E -> {
                objectRect.setLocation(
                        boundRect.x,
                        (int) (boundRect.getCenterY() - objectRect.width / 2));
                targetPanel.setLocation(
                        boundRect.getUpRight().x - targetPanel.getWidth(),
                        boundRect.y);
            }

            case W -> {
                objectRect.setLocation(
                        boundRect.getUpRight().x - objectRect.width,
                        (int) (boundRect.getCenterY() - objectRect.width / 2));
                targetPanel.setLocation(boundRect.getLocation());
            }

            case NE -> {
                objectRect.setLocation(
                        boundRect.x,
                        boundRect.getLoLeft().y - objectRect.width);
                targetPanel.setLocation(
                        boundRect.getUpRight().x - targetPanel.getWidth(),
                        boundRect.y);
            }

            case NW -> {
                objectRect.setLocation(
                        boundRect.getLoRight().x - objectRect.width,
                        boundRect.getLoRight().y - objectRect.width);
                targetPanel.setLocation(boundRect.getLocation());
            }

            case SE -> {
                objectRect.setLocation(boundRect.getLocation());
                targetPanel.setLocation(
                        boundRect.getLoRight().x - targetPanel.getWidth(),
                        boundRect.getLoRight().y - targetPanel.getWidth());
            }

            case SW -> {
                objectRect.setLocation(
                        boundRect.getUpRight().x - objectRect.width,
                        boundRect.y);
                targetPanel.setLocation(
                        boundRect.x,
                        boundRect.getLoLeft().y - targetPanel.getWidth());
            }
            
        }
    }

    @Override
    public String toString() {
        return "BoxTrial{" +
                "boundRect=" + boundRect +
                ", objectRect=" + objectRect +
                ", targetPanel=" + targetPanel +
                ", axis=" + axis +
                ", dir=" + dir +
                ", dist=" + dist +
                '}';
    }
}
