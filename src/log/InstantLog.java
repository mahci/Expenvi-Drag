package log;

import experiment.Experiment;
import experiment.Task;
import tools.Out;
import tools.Utils;

import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class InstantLog {
    public long trial_show;             // the moment trial is shown on the screen

    public long first_move;             // first movement of the cursor

    // first/last entry of the cursor into the object
    public long first_cur_obj_entry;
    public long last_cur_obj_entry;

    // first/last grab moments (last only different if start_error)
    public long first_grab;
    public long last_grab;

    // the moment of DRAG start (no first/last <= always hit/miss after first)
    public long drag_start;

    // {Peek} first/last time the cursor (while dragging) enters temp
    public long first_cur_temp_entry;
    public long last_cur_temp_entry;

    // {Peek} first/last time the object enters temp
    public long first_obj_temp_entry;
    public long last_obj_temp_entry;

    // {Peek} first/last time the object exits temp
    public long first_obj_temp_exit;
    public long last_obj_temp_exit;

    // entry moment to the tunnel (while dragging)
    public long tunnel_entry;

    // first/las time the cursor enters the target (or exit Tunnel) while dragging
    public long first_cur_tgt_entry;
    public long last_cur_tgt_entry;

    // first/last time the object fully enters the target, or zero if never {Box}
    public long first_obj_tgt_entry;
    public long last_objt_tgt_entry;

    // release moment (only considered after drag)
    public long release;

    // first/last revert moment (last only different if start_error)
    public long first_revert;
    public long last_revert;

    public void logTrialShow() {
        if (trial_show == 0) trial_show = Utils.nowMillis();
    }

    public void logMove() {
        if (first_move == 0) first_move = Utils.nowMillis();
    }

    public void logCurObjEntry() {
        if (first_cur_obj_entry == 0) {
            first_cur_obj_entry = Utils.nowMillis();
            last_cur_obj_entry = Utils.nowMillis();
        } else {
            last_cur_obj_entry = Utils.nowMillis();
        }
    }

    public void logGrab() {
        if (first_grab == 0) {
            first_grab = Utils.nowMillis();
            last_grab = Utils.nowMillis();
        } else {
            last_grab = Utils.nowMillis();
        }
    }

    public void logDragStart() {
        if (drag_start == 0) drag_start = Utils.nowMillis();
    }

    public void logCurTempEntry() {
        if (first_cur_temp_entry == 0) {
            first_cur_temp_entry = Utils.nowMillis();
            last_cur_temp_entry = Utils.nowMillis();
        } else {
            last_cur_temp_entry = Utils.nowMillis();
        }
    }

    public void logObjTempEntry() {
        if (first_obj_temp_entry == 0) {
            first_obj_temp_entry = Utils.nowMillis();
            last_obj_temp_entry = Utils.nowMillis();
        } else {
            last_obj_temp_entry = Utils.nowMillis();
        }
    }

    public void logObjTempExit() {
        if (first_obj_temp_exit == 0) {
            first_obj_temp_exit = Utils.nowMillis();
            last_obj_temp_exit = Utils.nowMillis();
        } else {
            last_obj_temp_exit = Utils.nowMillis();
        }
    }

    public void logTunnelEntry() {
        if (tunnel_entry == 0) tunnel_entry = Utils.nowMillis();
    }

    public void logCurTgtEntry() {
        if (first_cur_tgt_entry == 0) {
            first_cur_tgt_entry = Utils.nowMillis();
            last_cur_tgt_entry = Utils.nowMillis();
        } else {
            last_cur_tgt_entry = Utils.nowMillis();
        }
    }

    public void logObjTgtEntry() {
        if (first_obj_tgt_entry == 0) {
            first_obj_tgt_entry = Utils.nowMillis();
            last_objt_tgt_entry = Utils.nowMillis();
        } else {
            last_objt_tgt_entry = Utils.nowMillis();
        }
    }

    public void logRelease() {
        if (release == 0) release = Utils.nowMillis();
    }

    public void logRevert() {
        if (first_revert == 0) {
            first_revert = Utils.nowMillis();
            last_revert = Utils.nowMillis();
        } else {
            last_revert = Utils.nowMillis();
        }
    }

    public int getPointTime() {
        if (last_cur_obj_entry != 0) return (int) (last_cur_obj_entry - first_move);
        else return -1; // {Tunnel}
    }

    public int getGrabTime() {
        if (last_cur_obj_entry != 0) return (int) (last_grab - last_cur_obj_entry);
        else return (int) (last_grab - first_move); // {Tunnel}
    }

    public int getGrabToDragTime() {
        return (int) (drag_start - last_grab);
    }

    public int getTunnelEntryTime() {
       return (int) (tunnel_entry - drag_start);
    }

    public int getDragTime(Experiment.TASK taskType) {
        int result = -1;
        switch (taskType) {
            case BOX, TUNNEL -> { // For Tunnel, cur_tgt_entry is exit
                if (last_cur_tgt_entry > 0) result = (int) (last_cur_tgt_entry - drag_start);
            }
            case BAR, PEEK -> {
                if (last_objt_tgt_entry > 0) result = (int) (last_objt_tgt_entry - drag_start);
            }
        }

        return result;
    }

    public int getTempEntryTime() {
        if (last_obj_temp_entry > 0) return (int) (last_obj_temp_entry - drag_start);
        else return -1;
    }

    public int getTempToTgtTime() {
        if (last_obj_temp_exit != 0 && last_objt_tgt_entry > last_obj_temp_exit) {
            return (int) (last_objt_tgt_entry - last_obj_temp_exit);
        } else {
            return -1;
        }
    }

    public int getReleaseTime(Experiment.TASK taskType) {
        int result = -1;
        switch (taskType) {
            case BOX, TUNNEL -> { // For Tunnel, cur_tgt_entry is exit
                if (last_cur_tgt_entry > 0 && release > 0) result = (int) (release - last_cur_tgt_entry);
            }
            case BAR, PEEK -> {
                if (last_objt_tgt_entry > 0 && release > 0) result = (int) (release - last_objt_tgt_entry);
            }
        }

        return result;
    }

    public int getRevertTime() {
        if (last_obj_temp_entry > 0) return (int) (last_revert - last_obj_temp_entry);
        else return -1;
    }

    public int getTrialTime() {
        if (release > 0) return (int) (release - last_grab);
        else return (int) (last_revert - last_grab);
    }

    public int getTotalTime() {
        if (release > 0) return (int) (release - first_move);
        else return (int) (last_revert - first_move);
    }

    @Override
    public String toString() {
        final Field[] fields = getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : fields) {
                sb.append(field.getLong(this)).append(SP);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
