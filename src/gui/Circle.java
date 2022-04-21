package gui;

import java.awt.*;
import java.awt.geom.*;

public class Circle implements Shape {
    public Point center;
    public int radius;
    private Ellipse2D ell;

    public Circle() {
        center = new Point();
        radius = 0;
        ell = new Ellipse2D.Double();
    }

    public void setRadius(int r) {
        radius = r;
        ell.setFrame(0, 0, r, r);
    }

    public void setCenter(int cX, int cY) {
        center = new Point(cX, cY);
        ell.setFrameFromCenter(center, new Point(getX(), getY()));
    }

    public void translate(int dX, int dY) {
        center.translate(dX, dY);
        ell.setFrameFromCenter(center, new Point(getX(), getY()));
    }

    public int getR() {
        return radius;
    }

    public int getX() {
        return center.x - radius;
    }

    public int getY() {
        return center.y - radius;
    }


    @Override
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getR(), getR());
    }

    @Override
    public Rectangle2D getBounds2D() {
        return getBounds();
    }

    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
//        Ellipse2D.Double ell = new Ellipse2D.Double(getX(), getY(), radius, radius);
        return ell.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return null;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return null;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
