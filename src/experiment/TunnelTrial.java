package experiment;

import graphic.MoRectangle;
import jdk.jshell.execution.Util;
import tools.Out;
import tools.Utils;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

import static tools.Consts.*;
import static tools.Consts.STRINGS.SP;

public class TunnelTrial extends Trial implements Cloneable {
    private final String NAME = "TunnelTrial/";

    public MoRectangle line1Rect = new MoRectangle();
    public MoRectangle line2Rect = new MoRectangle();
    public MoRectangle inRect = new MoRectangle();
    public MoRectangle startTextRect = new MoRectangle();

    public Line2D.Double startLine = new Line2D.Double();
    public Line2D.Double endLine = new Line2D.Double();

    // Factors
    private int fTunnelLength, fTunnelWidth;
    private AXIS fAxis;

    // Constants and randoms
    public DIRECTION dir;
    private int linesW, textW; // px

    public TunnelTrial(List<Integer> conf, double... params) {
        super(conf, params);

        if (conf == null || conf.size() < 3) {
            Out.d(NAME, "Config not properly set!");
            return;
        }

        // Set factors
        fTunnelLength = conf.get(0);
        fTunnelWidth = conf.get(1);
        fAxis = AXIS.get(conf.get(2));

        dir = fAxis.randDir(); // Random direction based on the AXIS

        // Set sizes
        if (params != null && params.length > 0) {
            this.linesW = Utils.mm2px(params[0]);
            this.textW = Utils.mm2px(params[1]);

            switch (fAxis) {
                case VERTICAL -> {
                    line1Rect.setSize(linesW, Utils.mm2px(fTunnelLength));
                    line2Rect.setSize(line1Rect.getSize());
                    inRect.setSize(Utils.mm2px(fTunnelWidth), Utils.mm2px(fTunnelLength));
                    startTextRect.setSize(textW, textW);

                    boundRect.setSize(inRect.width + 2 * linesW + textW, inRect.height);
                }

                case HORIZONTAL -> {
                    line1Rect.setSize(Utils.mm2px(fTunnelLength), linesW);
                    line2Rect.setSize(line1Rect.getSize());
                    inRect.setSize(Utils.mm2px(fTunnelLength), Utils.mm2px(fTunnelWidth));
                    startTextRect.setSize(textW, textW);

                    boundRect.setSize(inRect.width, inRect.height + 2 * linesW + textW);
                }
            }
        }

    }

    public AXIS getAxis() {
        return fAxis;
    }

    @Override
    public Point getEndPoint() {
        Point result = new Point();

        switch (dir) {
            case W -> {
                result = new Point(
                        boundRect.x,
                        boundRect.y + boundRect.height / 2);
            }

            case E -> {
                result = new Point(
                        boundRect.x + boundRect.width,
                        boundRect.y + boundRect.height / 2);
            }

            case N -> {
                result = new Point(
                        boundRect.x + boundRect.width / 2,
                        boundRect.y);
            }

            case S -> {
                result = new Point(
                        boundRect.x + boundRect.width / 2,
                        boundRect.y + boundRect.height);
            }
        }

        return result;
    }

    /**
     * Check if a point is inside the tunnel or on the lines
     * @param p Point
     * @return Point in the tunnel OR on the lines => TRUE
     */
    public boolean isPointInside(Point p) {
        return inRect.contains(p) || line1Rect.contains(p) || line2Rect.contains(p);
    }

    @Override
    protected void positionElements() {
        super.positionElements();
        final String TAG = NAME + "setElementsLocations";

        switch (dir) {
            case W -> {
                line1Rect.setLocation(boundRect.x, boundRect.y);
                inRect.setLocation(boundRect.x, boundRect.y + line1Rect.height);
                line2Rect.setLocation(boundRect.x, inRect.y + inRect.height);
                startTextRect.setLocation(
                        boundRect.x + boundRect.width - startTextRect.width,
                        line2Rect.y + line2Rect.height);

                startLine.setLine(inRect.topRight, inRect.bottomRight);
                endLine.setLine(inRect.topLeft, inRect.bottomLeft);
            }

            case E -> {
                line1Rect.setLocation(boundRect.x, boundRect.y);
                inRect.setLocation(boundRect.x, boundRect.y + line1Rect.height);
                line2Rect.setLocation(boundRect.x, inRect.y + inRect.height);
                startTextRect.setLocation(boundRect.x, line2Rect.y + line2Rect.height);

                startLine.setLine(inRect.topLeft, inRect.bottomLeft);
                endLine.setLine(inRect.topRight, inRect.bottomRight);
            }

            case N -> {
                startTextRect.setLocation(
                        boundRect.x,
                        boundRect.y + boundRect.height - startTextRect.height);
                line2Rect.setLocation(
                        startTextRect.x + startTextRect.width,
                        boundRect.y);
                inRect.setLocation(line2Rect.x + line2Rect.width, boundRect.y);
                line1Rect.setLocation(inRect.x + inRect.width, boundRect.y);

                startLine.setLine(inRect.bottomLeft, inRect.bottomRight);
                endLine.setLine(inRect.topLeft, inRect.topRight);
            }

            case S -> {
                startTextRect.setLocation(boundRect.x, boundRect.y);
                line2Rect.setLocation(
                        startTextRect.x + startTextRect.width,
                        boundRect.y);
                inRect.setLocation(line2Rect.x + line2Rect.width, boundRect.y);
                line1Rect.setLocation(inRect.x + inRect.width, boundRect.y);

                startLine.setLine(inRect.topLeft, inRect.topRight);
                endLine.setLine(inRect.bottomLeft, inRect.bottomRight);
            }
        }
    }

    @Override
    public String toLogString() {
        return boundRect.x + SP +
                boundRect.y + SP +
                fTunnelLength + SP +
                fTunnelWidth + SP +
                dir.toString();
    }

    @Override
    public String toString() {
        return toLogString();
    }
}
