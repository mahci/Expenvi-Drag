package gui;

import java.awt.*;

public class MoRectangle extends Rectangle {

    public boolean isInside(Point pt) {

        if (pt != null) {
            return (pt.x >= x) && (pt.y >= y) &&
                    (pt.x <= x + width) && (pt.y <= y + height);
        } else {
            return false;
        }
    }

}
