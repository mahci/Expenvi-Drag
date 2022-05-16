package experiment;

import tools.Out;
import tools.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Block {
    private final String NAME = "Block/";

    public final ArrayList<Trial> mTrials = new ArrayList<>();

    public Block() {

    }

    /**
     * Constructor
     */
    public Block(List<Integer> factor1, List<Integer> factor2) {
        List<Integer> config = new ArrayList<>();
        for (int vi : factor1) {
            for (int vj : factor2) {
                config.add(factor1.get(vi), factor2.get(vj));

                // Create trials based on the combination
                mTrials.add(new Trial(config));

                config.clear();
            }
        }

    }

    /**
     * Constructor
     */
    public Block(int[] factor1, int[] factor2, int[] factor3, int... params) {
        List<Integer> config = new ArrayList<>();
        for (int vi : factor1) {
            for (int vj : factor2) {
                for (int vk : factor3) {
                    config.add(vi);
                    config.add(vj);
                    config.add(vk);

                    // Create trials based on the combination
                    mTrials.add(new Trial(config, params));

                    config.clear();
                }
            }
        }

    }

    /**
     * Get a trial
     * @param trInd Trial index
     * @return Trial
     */
    public Trial getTrial(int trInd) {
        return mTrials.get(trInd);
    }

    /**
     * Get the number of trials
     * @return Number of trials
     */
    public int getNumTrials() {
        return mTrials.size();
    }

    /**
     * Get the rest of trials (after and not incl. the trialInd)
     * @param trialInd Trial index
     * @return List of Trials
     */
    public List<Trial> getRestOfTrials(int trialInd) {
        if (trialInd == mTrials.size() - 1) return new ArrayList<>();
        else return mTrials.subList(trialInd + 1, mTrials.size());
    }

    public void setTrial(int index, Trial tr) {
        mTrials.set(index, tr);
    }

    public void setTrialLocation(int trInd, Point loc) {
        mTrials.get(trInd).setBoundRectLocation(loc);
    }

    public void setTrialElements() {
        for (Trial tr : mTrials) {
            tr.setElementsLocations();
        }
    }

    /**
     * Shuffle a duplicate of a Trial to the rest
     * @param trialInd Trial index
     */
    public int dupeShuffleTrial(int trialInd) {
        final String TAG = NAME + "dupeShuffleTrial";

        final Trial trial = mTrials.get(trialInd);
        final int lastInd = mTrials.size() - 1;
        final int insertInd;
        Out.d(TAG, "trInd | lastInd", trialInd, lastInd);
        if (trialInd == lastInd) insertInd = lastInd;
        else insertInd = Utils.randInt(trialInd + 1, lastInd);

        mTrials.add(insertInd, trial);

        return insertInd;
    }

}
