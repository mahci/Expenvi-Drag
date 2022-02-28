package tools;

import java.awt.geom.Dimension2D;

public class DimensionD extends Dimension2D {
    public double width;
    public double height;

    public DimensionD(double width, double height) {
        setSize(width, height);
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
}
