package gui;

import org.w3c.dom.css.Rect;
import tools.Out;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import static java.lang.Math.*;

public class Line {
    private final String NAME = "Line/";

    public Point p1;
    public Point p2;
    public int x1;
    public int x2;
    public int y1;
    public int y2;

    private int tol;

    public Line() {
        p1 = new Point();
        p2 = new Point();
    }

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        setXY();
    }

    public Line(int x1, int y1, int x2, int y2) {
        set(x1, y1, x2, y2);
    }

    public void set(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        setP();
    }

    public void setP1(int x, int y) {
        p1 = new Point(x, y);
        setXY();
    }

    public void setP2(int x, int y) {
        p2 = new Point(x, y);
        setXY();
    }

    public void setTol(int t) {
        tol = t;
    }

    /**
     * Check if a point is inside the tolerance from each side
     * @param tol Tolerance in px
     * @return True/false
     */
    public boolean isNear(Point p, int tol) {
        final double dist =
                abs((x2 - x1) * (y1 - p.y) - (x1 - p.x) * (y2 - y1))
                / sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));

        return getBoundRect(tol).contains(p);
    }

    public void translate(int dX, int dY) {
        p1.translate(dX, dY);
        p2.translate(dX, dY);

        setXY();

        Out.d("Line", x1, x2, y1, y2);
    }

    public void rotate(int oX, int oY, double deg) {
        final double radian = toRadians(deg);
        final double sinR = sin(radian);
        final double cosR = cos(radian);

        final Point cP1 = p1;
        final Point cP2 = p2;

        p1 = new Point(
                (int) (cosR * (cP1.x - oX) - sinR * (cP1.y - oY) + oX),
                (int) (sinR * (cP1.x - oX) + cosR * (cP1.y - oY) + oY));

        p2 = new Point(
                (int) (cosR * (cP2.x - oX) - sinR * (cP2.y - oY) + oX),
                (int) (sinR * (cP2.x - oX) + cosR * (cP2.y - oY) + oY));

        setXY();
    }

    public Path2D.Double getBoundRect(int tol) {
        final String TAG = NAME + "getBoundRect";

        Point[] boundPs = new Point[4];
        Path2D.Double boundRect = new Path2D.Double();
        Rectangle rect = new Rectangle();

        if (y1 == y2) {
            // Only two diagonal points suffice
            boundPs[0] = new Point(x1, y1 - tol);
            boundPs[1] = new Point(x2, y2 + tol);

            rect.setFrameFromDiagonal(boundPs[0], boundPs[1]);
            boundRect = new Path2D.Double(rect);

        } else if (x1 == x2) {
            boundPs[0] = new Point(x1 - tol, y1);
            boundPs[1] = new Point(x2 + tol, y2);

            rect.setFrameFromDiagonal(boundPs[0], boundPs[1]);
            boundRect = new Path2D.Double(rect);
        } else {

            final double sideTol = tol / sqrt(2); // Angle is always 45 deg
            Point p1;
            Point p2;
            if (y1 > y2) {
                p1 = new Point((int) (x1 - sideTol), (int) (y1 - sideTol));
                p2 = new Point((int) (x1 + sideTol), (int) (y1 + sideTol));

                boundRect.moveTo(x1 - sideTol, y1 - sideTol);
                boundRect.lineTo(x2 - sideTol, y2 - sideTol);
                boundRect.lineTo(x2 + sideTol, y2 + sideTol);
                boundRect.lineTo(x1 + sideTol, y1 + sideTol);
//                boundRect.closePath();
            } else {
                p1 = new Point((int) (x1 + sideTol), (int) (y1 - sideTol));
                p2 = new Point((int) (x1 - sideTol), (int) (y1 + sideTol));

                boundRect.moveTo(x1 - sideTol, y1 + sideTol);
                boundRect.lineTo(x2 - sideTol, y2 + sideTol);
                boundRect.lineTo(x2 + sideTol, y2 - sideTol);
                boundRect.lineTo(x1 + sideTol, y1 - sideTol);
//                boundRect.closePath();
            }
//            rect.setFrameFromDiagonal(p1, p2);

//            boundRect = new Path2D.Double(rect);


//            rect.setFrameFromDiagonal(p1, p2);

//            final double len = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
//            rect = new Rectangle(x1, y1, (int) len, 2 * tol);
//            boundRect = new Path2D.Double(rect);
//            AffineTransform transform = new AffineTransform();
//            int deg = 45;
//            if (y1 > y2) deg = -45;
//            double rad = toRadians(deg);
//            transform.rotate(rad);
//            boundRect = new Path2D.Double(transform.createTransformedShape(rect));

            final double a = (y2 - y1) * 1.0 / (x2 - x1);
            final double b = y1 - a * x1;

            final double b1 = b + tol * sqrt(pow(a, 2) + 1);
            final double b2 = b - tol * sqrt(pow(a, 2) + 1);

            final double pa = -1 / a;
            final double pb1 = y1 - pa * x1; // Perpendicular1 b
            final double pb2 = y2 - pa * x2; // perpendicular2 b

            boundPs[0] = new Point(
                    (int) ((a * (pb1 - b1)) / (pow(a, 2) + 1)),
                    (int) ((b1 - pb1) / (pow(a, 2) + 1) + pb1));

            boundPs[1] = new Point(
                    (int) ((a * (pb1 - b2)) / (pow(a, 2) + 1)),
                    (int) ((b2 - pb1) / (pow(a, 2) + 1) + pb1));

            boundPs[2] = new Point(
                    (int) ((a * (pb2 - b1)) / (pow(a, 2) + 1)),
                    (int) ((b1 - pb2) / (pow(a, 2) + 1) + pb1));

            boundPs[3] = new Point(
                    (int) ((a * (pb2 - b2)) / (pow(a, 2) + 1)),
                    (int) ((b2 - pb2) / (pow(a, 2) + 1) + pb1));

//            for (Point p : boundPs) {
//                boundRect.add(p);
////                Out.d(TAG, p);
//            }
        }



//        Out.d(TAG, "--------------");

        return boundRect;
    }

    private void setXY() {
        x1 = p1.x;
        x2 = p2.x;

        y1 = p1.y;
        y2 = p2.y;
    }

    private void setP() {
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
    }

    @Override
    public String toString() {
        return "Line{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }
}
