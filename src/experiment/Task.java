package experiment;

import java.util.ArrayList;
import java.util.List;

public class Task {
    protected List<Block> mBlocks = new ArrayList<>();

    public Task(int nBlocks) {

    }

    public int getNumBlocks() {
        return mBlocks.size();
    }

}
