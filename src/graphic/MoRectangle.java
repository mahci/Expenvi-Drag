package graphic;

import experiment.Experiment;
import tools.Utils;

import java.awt.*;

public class MoRectangle extends Rectangle {

    public MoRectangle() {

    }

    public MoRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public MoRectangle(int cx, int cy, int margin) {
        this.x = cx - margin;
        this.y = cy - margin;
        this.width = 2 * margin;
        this.height = 2 * margin;
    }

    public MoRectangle(Point center, int margin) {
        this.x = center.x - margin;
        this.y = center.y - margin;
        this.width = 2 * margin;
        this.height = 2 * margin;
    }

    public void setLocationLoLeft(Point loLeftP) {
        x = loLeftP.x;
        y = loLeftP.y - height;
    }

    public void setLocationTopRight(Point topRightR) {
        x = topRightR.x - width;
        y = topRightR.y;
    }

    public void resize(Experiment.DIRECTION dir, int dVt, int dHz) {
        switch (dir) {
            case N -> {
                final int newH = height - dVt;
                if (newH > 0) {
                    height = newH;
                    y += dVt;
                }
            }
            case S -> {
                final int newH = height + dVt;
                if (newH > 0) {
                    height = newH;
                }
            }
            case E -> {
                final int newW = width + dHz;
                if (newW > 0) {
                    width = newW;
                }
            }
            case W -> {
                final int newW = width - dHz;
                if (newW > 0) {
                    width = newW;
                    x += dHz;
                }
            }
        }
    }

    public Point topLeft() {
        return new Point(x, y);
    }

    public Point topRight() {
        return new Point(x + width, y);
    }

    public Point loLeft() {
        return new Point(x, y + height);
    }

    public Point loRight() {
        return new Point(x + width, y + height);
    }

    public Point center() {
        return new Point((int) getCenterX(), (int) getCenterY());
    }

    public int area() {
        return height * width;
    }
    /**
     * Fit a rectangle inside this
     * @param rect Rectangle
     * @return Found position or null if not found
     */
    public Point fitRect(Rectangle rect) {
        if (rect.height > height || rect.width > width) return null;
        else {
            return new Point(
                    Utils.randInt(x, width - rect.width),
                    Utils.randInt(y, height - rect.height));
        }
    }

    public int minX() {
        return x;
    }

    public int minY() {
        return y;
    }

    public int maxX() {
        return x + width;
    }

    public int maxY() {
        return y + height;
    }

    public MoRectangle getMarginedRectangel(int margin) {
        return new MoRectangle(
                x - margin, y - margin,
                width + 2 * margin, height + 2 * margin);
    }

    public String printCorners() {
        String resSB = "[" + topLeft().x + "," + topLeft().y + "]" +
                "[" + topRight().x + "," + topRight().y + "]" +
                "[" + loRight().x + "," + loRight().y + "]" +
                "[" + loLeft().x + "," + loLeft().y + "]";
        return resSB;
    }

    @Override
    public String toString() {
        return "MoRect[" +
                "x=" + x + "," +
                "y=" + y + "," +
                "width=" + width + "," +
                "height=" + height + "]";
    }
}
