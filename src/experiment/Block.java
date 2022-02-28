package experiment;

import tools.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static experiment.Experiment.*;

public class Block {
    private final ArrayList<Trial> mTrials = new ArrayList<>();

    /**
     * Constructor
     */
    public Block() {

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
    public int getNTrials() {
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

    /**
     * Shuffle a duplicate of a Trial to the rest
     * @param trialInd Trial index
     */
    public void dupeShuffleTrial(int trialInd) {
        final Trial trial = mTrials.get(trialInd);
        final int lastInd = mTrials.size() - 1;
        final int insertInd;

        if (trialInd == lastInd) insertInd = lastInd;
        else insertInd = Utils.randInt(trialInd, lastInd);

        mTrials.add(insertInd, trial);
    }

}
