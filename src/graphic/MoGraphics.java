package graphic;

import java.awt.*;
import java.awt.geom.Line2D;

public class MoGraphics {

    private Graphics2D g2d;

    public MoGraphics(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public void drawRectangle(Color color, Rectangle rect) {
        g2d.setColor(color);
        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    public void drawLine(Color color, Line2D line) {
        g2d.setColor(color);
        g2d.drawLine((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
    }

    public void drawLines(Color color, Line2D... lines) {
        g2d.setColor(color);
        for (Line2D line : lines) {
            g2d.drawLine((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
        }
    }

    public void drawCircle(Color color, MoCircle circ) {
        g2d.setColor(color);
        g2d.draw(circ);
    }

    public void fillCircle(Color color, MoCircle circ) {
        g2d.setColor(color);
        g2d.fill(circ);
    }

    public void fillRectangle(Color color, Rectangle rect) {
        g2d.setColor(color);
        g2d.fill(rect);
    }

    public void fillRectangles(Color color, Rectangle... rects) {
        g2d.setColor(color);
        for (Rectangle rect : rects) {
            g2d.fill(rect);
        }
    }

    public void drawString(Color color, Font font, String txt, int x, int y) {
        g2d.setColor(color);
        g2d.setFont(font);
        g2d.drawString(txt, x, y);
    }
}
