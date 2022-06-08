package experiment;

import com.google.gson.Gson;
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
     * @param trNum Trial number (starting from 1)
     * @return Trial
     */
    public Trial getTrial(int trNum) {
        if (trNum > mTrials.size()) return null;
        else return cloneTrial(mTrials.get(trNum - 1));
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

    public void setTrialLocation(int trNum, Point loc) {
        mTrials.get(trNum - 1).setBoundRectLocation(loc);
        mTrials.get(trNum - 1).positionElements();
    }

    public void positionAllTrialsElements() {
        for (Trial tr : mTrials) {
//            Out.d(NAME, tr.getClass());
            tr.positionElements();
        }
    }

    /**
     * Shuffle a duplicate of a Trial to the rest
     * @param trNum Trial number (from 1)
     * @return New number for the trial
     */
    public int dupeShuffleTrial(int trNum) {
        final String TAG = NAME + "dupeShuffleTrial";

        final int trInd = trNum - 1;

        final Gson gson = new Gson();
        final String trialJSON = gson.toJson(mTrials.get(trInd));
        final Class<? extends Trial> trialType = mTrials.get(trInd).getClass();

//        final int lastInd = mTrials.size() - 1;
        final int insertInd = Utils.randInt(trInd + 1, mTrials.size());
        mTrials.add(insertInd, gson.fromJson(trialJSON, trialType));
        Out.d(TAG, "insertInd | total", insertInd, mTrials.size());

        return insertInd + 1; // Return the number
    }

    public Trial cloneTrial(Trial inTr) {
        final Gson gson = new Gson();
        final String trialJSON = gson.toJson(inTr);
        final Class<? extends Trial> trialType = inTr.getClass();

        return gson.fromJson(trialJSON, trialType);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Block{\n");
        for (int ti = 1; ti <= mTrials.size(); ti++) {
            result.append("trial_").append(ti).append("{")
                    .append(mTrials.get(ti - 1))
                    .append("{").append("\n");
        }
        result.append("}");

        return result.toString();
    }
}
