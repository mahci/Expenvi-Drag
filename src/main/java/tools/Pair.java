package tools;

public class Pair {
    public int first;
    public int second;

    public Pair(int f, int s) {
        first = f;
        second = s;
    }

    public void set(int f, int s) {
        first = f;
        second = s;
    }

    public boolean areBoth(int value) {
        return (first == value) && (second == value);
    }

    /**
     * Basically AND
     * @return
     */
    public int fXs() {
        return first * second;
    }

    /**
     * Increase first
     */
    public void incF() {
        first++;
    }

    /**
     * Increase first
     */
    public void incS() {
        second++;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
