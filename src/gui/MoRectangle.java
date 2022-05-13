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

    /**
     * Fit a rectangle inside this
     * @param rect Rectangle
     * @return Found position or null if not found
     */
    public Point fitRect(Rectangle rect) {
        if (rect.height > height || rect.width > width) return null;
        else {
            return new Point(
                    Utils.randInt(0, width - rect.width),
                    Utils.randInt(0, height - rect.height));
        }
    }

    public String printCorners() {
        StringBuilder resSB = new StringBuilder();
        resSB.append("[").append(getUpLeft().x).append(",").append(getUpLeft().y).append("]")
                .append("[").append(getUpRight().x).append(",").append(getUpRight().y).append("]")
                .append("[").append(getLoRight().x).append(",").append(getLoRight().y).append("]")
                .append("[").append(getLoLeft().x).append(",").append(getLoLeft().y).append("]");

        return resSB.toString();
    }
}
