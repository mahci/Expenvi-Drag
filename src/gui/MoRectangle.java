package gui;

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

    public Point getUpLeft() {
        return new Point(x, y);
    }

    public Point getUpRight() {
        return new Point(x + width, y);
    }

    public Point getLoLeft() {
        return new Point(x, y + height);
    }

    public Point getLoRight() {
        return new Point(x + width, y + height);
    }

    public Point getCenter() {
        return new Point((int) getCenterX(), (int) getCenterY());
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
        return new MoRectangle(x - margin, y - margin, width + 2 * margin, height + 2 * margin);
    }

    public String printCorners() {
        String resSB = "[" + getUpLeft().x + "," + getUpLeft().y + "]" +
                "[" + getUpRight().x + "," + getUpRight().y + "]" +
                "[" + getLoRight().x + "," + getLoRight().y + "]" +
                "[" + getLoLeft().x + "," + getLoLeft().y + "]";
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
