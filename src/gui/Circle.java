package gui;

import tools.Out;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class Circle implements Shape {
    private final static String NAME = "Circle/";

    public Point center;
    public int radius;
    private Ellipse2D ell = new Ellipse2D.Double();

    public Circle() {
        center = new Point();
        radius = 0;
    }

    public Circle(Point cntr, int r) {
        center = cntr;
        radius = r;
        ell.setFrameFromCenter(center, getUL());
    }

    public Circle(int cx, int cy, int r) {
        this(new Point(cx, cy), r);
    }

    public Circle(double cx, double cy, int r) {
        this(new Point((int) cx, (int) cy), r);
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

    public Point getUL() {
        return new Point(center.x - radius, center.y - radius);
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

    public List<Integer> intersection(int y) {
        final String TAG = NAME + "intersection";

        List<Integer> result = new ArrayList<>();
        final int upperY = center.y - radius;
        final int lowerY = center.y + radius;
        Out.d(TAG, upperY, lowerY);
        if (y < upperY || y > lowerY) return result;
        else {
            // Two intersections (+-) (will be the same if y = upper/lowerY
            int x = (int) (sqrt(pow(radius, 2) - pow((y - center.y), 2)) + center.x);
            result.add(x);
            x = (int) (-sqrt(pow(radius, 2) - pow((y - center.y), 2)) + center.x);
            result.add(x);
        }

        return result;
    }

    public List<Point> getPoints() {
        final String TAG = NAME + "intersection";

        List<Point> result = new ArrayList<>();
        int cx = center.x;
        int cy = center.y;
        for (int angle = 0; angle < 360; angle++) {
            result.add(new Point(
                    (int) (cx + radius * cos(angle)),
                    (int) (cy + radius * sin(angle))));
        }

        return result;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return ell.intersects(x, y, w, h);
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
        return ell.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return ell.getPathIterator(at, flatness);
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
