package tools;

public class MinMax {
    public int min;
    public int max;

    public MinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Check if a value is between min and max (inlcusive)
     * @param value Value to check
     * @return True/False
     */
    public boolean isBetweenIncl(int value) {return value <= max && value >= min;}

    /**
     * isWithing exclusive of min and max
     * @param value Value to check
     * @return True/false
     */
    public boolean isBetween(int value) {return value < max && value > min;}

    /**
     * Get the range
     * @return Range between min and max
     */
    public int getRange() {return max - min;}

    /**
     * Move both the min and max
     * @param minAmt Amount to move min
     * @param maxAmt Amoutn to move max
     */
    public void move(int minAmt, int maxAmt) {
        min += minAmt;
        max += maxAmt;
    }

    public void moveMin(int amt) {
        min += amt;
    }

    public void moveMax(int amt) {
        max += amt;
    }
}
