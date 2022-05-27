package gui;

import experiment.Block;
import experiment.Experiment;
import experiment.Task;
import tools.MinMax;
import tools.Out;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;

import static experiment.Experiment.DIRECTION.*;
import static experiment.Experiment.DIRECTION.E;
import static java.lang.Math.*;
import static experiment.Experiment.*;

public class TaskPanel extends JLayeredPane {
    private final String NAME = "TaskPanel/";

    // Constants
    protected double TB_MARGIN_mm = 20;
    protected double LR_MARGIN_mm = 20;
    protected int MAX_CEHCK_POS = 100;

    // Experiment
    protected Task mTask;
    protected Block mBlock;

    // Flags
    protected boolean mTrialActive = false;

    // Counters
    protected int mPosCount = 0;

    // Helpers
    protected Graphix mGraphix;

    // Methods ------------------------------------------------------------------------------------
    protected void start() { }

    protected void startBlock(int blkNum) {
        final String TAG = NAME + "showBlock";

        mBlock = mTask.getBlock(blkNum);
        Out.d(TAG, mTask.getNumBlocks(), mBlock);

        // Try to find positions for all the trials in the block
        if (findAllTrialsPosition(1) == 0) {
            mBlock.positionAllTrialsElements();
            Out.d(TAG, "Showing the trials");
            showTrial(1);
        } else {
            Out.e(TAG, "Couldn't find positions for the trials in the block!");
        }
    }

    protected void showTrial(int trNum) { }

    protected boolean isHit() {
        return false;
    }

    protected void grab() { }

    protected void drag() { }

    protected void release() { }

    protected void cancel() { }

    protected void hit() { }

    protected void miss() { }

    protected Dimension getDispDim() {
        Dimension result = new Dimension();
        result.width = getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
        result.height = getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));

        return result;
    }

    protected int getDispW() {
        return getWidth() - (2 * Utils.mm2px(LR_MARGIN_mm));
    }

    protected int getDispH() {
        return getHeight() - (2 * Utils.mm2px(TB_MARGIN_mm));
    }

    protected Area getDispArea() {
        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        final int tbMargin = Utils.mm2px(TB_MARGIN_mm);
        return new Area(new Rectangle(
                0, 0,
                getWidth() - 2 * lrMargin, getHeight() - 2 * tbMargin));
    }

    protected MinMax getWidthMinMax() {
        final int hzMargin = Utils.mm2px(LR_MARGIN_mm);
        return new MinMax(hzMargin, getWidth() - hzMargin);
    }

    protected MinMax getHeightMinMax() {
        final int vtMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MinMax(vtMargin, getHeight() - vtMargin);
    }

    protected MoRectangle getPanelRect() {
        final int wMargin = Utils.mm2px(LR_MARGIN_mm);
        final int hMargin = Utils.mm2px(TB_MARGIN_mm);
        return new MoRectangle(
                wMargin, hMargin,
                getWidth() - 2 * wMargin,
                getHeight() - 2 * hMargin);
    }

    protected boolean contains(MoRectangle moRect) {
        return getPanelRect().contains(moRect);
    }

    protected Point findPosition(MoRectangle rect) {
        final int wMargin = Utils.mm2px(LR_MARGIN_mm);
        final int hMargin = Utils.mm2px(TB_MARGIN_mm);

        if (rect.height > getDispH() || rect.width > getDispW()) return null;
        else { return new Point(
                    Utils.randInt(wMargin, getDispW() - rect.width),
                    Utils.randInt(hMargin, getDispH() - rect.height));
        }
    }

    /**
     * Get the cursor position relative to the panel
     * @return Point
     */
    protected Point getCursorPos() {
        Point result = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(result, this);

        return  result;
    }

    /**
     * Recursively find suitable positions for a list of trials, from (incl.) startTrNum
     * @param startTrNum Index of the first trial. If > 1  => prev. Trial restricts, otherwise, free
     * @return Success (0) Fail (1)
     */
    public int findAllTrialsPosition(int startTrNum) {
        final String TAG = NAME + "findAllTrialsPosition";

        final int minNtDist = Utils.mm2px(Experiment.BoxTask.NT_DIST_mm);
        final int maxNtDist = minNtDist + Utils.mm2px(50);

        if (startTrNum == mBlock.getNumTrials() + 1) return 0;

        Point foundPosition = null;
        Point prevEndPoint = null;
//        MoRectangle prevBoundRect = null;
        if (startTrNum > 1) {
            prevEndPoint = mBlock.getTrial(startTrNum - 1).getEndPoint();
//            prevBoundRect = mBlock.getTrial(startTrNum - 1).getBoundRect();
        }

        // Search position for the start trial
//        foundPosition = findTrialPosition(
//                mBlock.getTrial(startTrNum).getBoundRect(),
//                prevEndPoint,
//                minNtDist, maxNtDist);
//        Out.d(TAG, "startTrNum", startTrNum);
        foundPosition = findPosition(
                mBlock.getTrial(startTrNum).getBoundRect(),
                prevEndPoint,
                minNtDist);

        if (foundPosition != null) { // Position found => set the position and move on to the rest
            mBlock.setTrialLocation(startTrNum, foundPosition);
            return findAllTrialsPosition(startTrNum + 1);
        } else {
//            Out.d(TAG, "Starting again", mPosCount);
            if (mPosCount < MAX_CEHCK_POS) {
                mPosCount++;
                return findAllTrialsPosition(startTrNum);
            }
        }

        return 1;
    }

    public Point findPosition(MoRectangle ntBoundRect, Point ctEndPoint, int minNtDist) {
        final String TAG = NAME + "findPosition";

        MoRectangle dispRect = getPanelRect();

        if (ctEndPoint == null) {
            return findPosition(ntBoundRect);
        } else {
            // Create margined rectangle
//            MoRectangle ctMarginRect = ctBoundRect.getMarginedRectangel(minNtDist);
//            MoRectangle ctMarginRect = new MoRectangle(
//                    ctBoundRect.getCenter().x,
//                    ctBoundRect.getCenter().y,
//                    minNtDist);
            MoRectangle ctMarginRect = new MoRectangle(ctEndPoint, minNtDist);
            MoRectangle ntMarginRect = ntBoundRect.getMarginedRectangel(0);
//            Out.d(TAG, ctMarginRect, ntMarginRect);
            List<DIRECTION> dirs = Arrays.asList(N, S, E, W);
            Collections.shuffle(dirs);

            // Check each direction
            for (DIRECTION dir : dirs) {
                switch (dir) {
                    case N -> {
//                        Out.d(TAG, "Checking N", ctMarginRect.y - ntMarginRect.height);
                        if (ctMarginRect.y - ntMarginRect.height > dispRect.minY()) {
                            ntMarginRect.y = ctMarginRect.y - ntMarginRect.height;
                            ntMarginRect.x = Utils.randInt(
                                    max(dispRect.minX(), ctMarginRect.minX() - ntMarginRect.width),
                                    min(dispRect.maxX() - ntMarginRect.width, ctMarginRect.maxX()));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case S -> {
//                        Out.d(TAG, "Checking S", ctMarginRect.maxY() + ntMarginRect.height);
                        if (ctMarginRect.maxY() + ntMarginRect.height < dispRect.maxY()) {
                            ntMarginRect.y = ctMarginRect.maxY();
                            ntMarginRect.x = Utils.randInt(
                                    max(dispRect.minX(), ctMarginRect.minX() - ntMarginRect.width),
                                    min(dispRect.maxX() - ntMarginRect.width, ctMarginRect.maxX()));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case E -> {
//                        Out.d(TAG, "Checking E", ctMarginRect.maxX() + ntMarginRect.width);
                        if (ctMarginRect.maxX() + ntMarginRect.width < dispRect.maxX()) {
                            ntMarginRect.x = ctMarginRect.maxX();
                            ntMarginRect.y = Utils.randInt(
                                    max(dispRect.minY(), ctMarginRect.minY() - ntMarginRect.height),
                                    min(dispRect.maxY() - ntMarginRect.height, ctMarginRect.maxY()));

                            return ntMarginRect.getLocation();
                        }
                    }

                    case W -> {
//                        Out.d(TAG, "Checking W", ctMarginRect.minX() - ntMarginRect.width);
                        if (ctMarginRect.minX() - ntMarginRect.width > dispRect.minX()) {
                            ntMarginRect.x = ctMarginRect.minX() - ntMarginRect.width;
                            ntMarginRect.y = Utils.randInt(
                                    max(dispRect.minY(), ctMarginRect.minY() - ntMarginRect.height),
                                    min(dispRect.maxY() - ntMarginRect.height, ctMarginRect.maxY()));

                            return ntMarginRect.getLocation();
                        }
                    }
                }
            }

        }


        return null;
    }


}
