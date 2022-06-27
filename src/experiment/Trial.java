package experiment;

import com.google.gson.Gson;
import graphic.MoRectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static tools.Consts.STRINGS.*;

public class Trial {

    protected List<Integer> config = new ArrayList<>();
    protected MoRectangle boundRect = new MoRectangle();

    public Trial(List<Integer> conf, int... params) {
        config.addAll(conf);
        // params in managed in subclasses
    }

    public Trial(List<Integer> conf, double... params) {
        config.addAll(conf);
        // params in managed in subclasses
    }

    public MoRectangle getBoundRect() {
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
    protected void positionElements() {
    }

    public String toLogString() {
        // Overridden by all the subclasses
        return "";
    }

    public static String getLogHeader() {
        return "trial_x" + SP +
                "trial_y" + SP +
                "object_w" + SP +
                "target_w" + SP +
                "axis" + SP +
                "direction";
    }

    @Override
    public Trial clone() {
        final Gson gson = new Gson();
        final String trialJSON = gson.toJson(this);
        final Class<? extends Trial> trialType = this.getClass();

        return gson.fromJson(trialJSON, trialType);
    }
}
