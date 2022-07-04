package tools;

import java.awt.*;

public class MyPoint extends Point {

    public MyPoint(Point pt) {
        super(pt);
    }

    public MyPoint(int x, int y) {
        super(x, y);
    }

    public MyPoint minus(Point sp) {
        MyPoint result = new MyPoint(x - sp.x, y - sp.y);
        return result;
    }
}
