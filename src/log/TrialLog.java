package log;

import experiment.Trial;

import java.awt.*;
import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class TrialLog {
    public Trial trial;

    public int point_time;          // From first movement to last entry ({Tunnel} 0)

    public int grab_time;           // From last entry to grab ({Tunnel} move > last grab)
    public int grab_x;              // Last GRAB X coordinate
    public int grab_y;              // Last GRAB Y coordinate

    public int grab_to_drag_time;   // From last grab to drag start

    public int temp_entry_time;     // {Peek} drag start > temp entry
    public int temp_to_tgt_time;    // {Peek} last temp exit > last target entry

    public int tunnel_entry_time;   // {Tunnel} drag_start > tunnel entry

    // From drag start to last target entry ({Tunnel} tunnel entry > tunnel exit) ({Bar, Peek} obj in target entry)
    public int drag_time;

    // From last target entry to release ({Tunnel} tunnel exit > release) ({Bar, Peek} obj in target entry)
    public int release_time;
    public int release_x;           // RELEASE X coordinate
    public int release_y;           // RELEASE Y coordinate

    // {Peek} From last obj into temp entry to revert
    public int revert_time;
    public int revert_x;           // {Peek} REVERT X coordinate
    public int revert_y;           // {Peek} REVERT Y coordinate

    public int trial_time;          // From drag start to RELEASE/last REVERT
    public int total_time;          // From first move to RELEASE/last REVERT

    public int result;              //1 (Hit) or 0 (Miss)
    public double accuracy;         // {Tunnel}

    public void logGrabPoint(Point grP) {
        grab_x = grP.x;
        grab_y = grP.y;
    }

    public void logReleasePoint(Point relP) {
        release_x = relP.x;
        release_y = relP.y;
    }

    public void logRevertPoint(Point revP) {
        revert_x = revP.x;
        revert_y = revP.y;
    }

    @Override
    public String toString() {
        final Field[] fields = getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : fields) {
                sb.append(field.get(this)).append(SP);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
