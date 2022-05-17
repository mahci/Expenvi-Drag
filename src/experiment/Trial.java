package experiment;

import gui.MoRectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Trial {

    protected List<Integer> config = new ArrayList<>();
    protected MoRectangle boundRect = new MoRectangle();

    public Trial(List<Integer> conf, int... params) {
        config.addAll(conf);
        // params in managed in subclasses
    }

    public Rectangle getBoundRect() {
        return boundRect;
    }

    /**
     * Overriden in subclasses
     * @return Point
     */
    public Point getEndPoint() {
        return new Point();
    }

    public void setBoundRectLocation(Point p) {
        boundRect.setLocation(p);
    }

    /**
     * Overriden in subclasses
     */
    public void setElementsLocations() {
    }

    public String toLogString() {
        return "";
    }

    public static String getLogHeader() {
        return "";
    }

}
