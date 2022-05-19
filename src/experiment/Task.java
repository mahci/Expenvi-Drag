package experiment;

import java.util.ArrayList;
import java.util.List;

public class Task {

    public final long NT_DELAY_ms = 700; // Delay before showing the next trial

    protected List<Block> mBlocks = new ArrayList<>();

    public Task(int nBlocks) {

    }

    public int getNumBlocks() {
        return mBlocks.size();
    }

    public Block getBlock(int blNum) {
        if (blNum <= mBlocks.size()) return mBlocks.get(blNum - 1);
        else return null;
    }

}
