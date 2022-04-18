package gui;

import tools.Out;

import java.awt.*;

public class Line {
    public Point p1;
    public Point p2;
    public int x1;
    public int x2;
    public int y1;
    public int y2;

    private int tol;

    public Line() {

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

    public void setP1(int x1, int y1) {
        this.x1 = x1;
        this.y1 = y1;
    }

    public void setTol(int t) {
        tol = t;
    }

    /**
     * Check if a point is inside the tolerance from each side
     * @return True/false
     */
    public boolean isNear(Point p) {
        final double dist =
                Math.abs((x2 - x1) * (y1 - p.y) - (x1 - p.x) * (y2 - y1))
                / Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        return dist <= tol;
    }

    public void translate(int dX, int dY) {
        p1.translate(dX, dY);
        p2.translate(dX, dY);

        setXY();

        Out.e("Line", x1, x2, y1, y2);
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

}
