package log;

import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class TimeLog {
    public int trial_time;     // From trial_show to hit/miss
    public int block_time;     // From first trial_show to the last release in the block
    public int homing_time;    // From comb key press to first move
    public int task_time;       // From first trial in the task to the last
    public long exp_time;       // From first trial_show to the last trial's release

    public void logHomingTime(int time) {
        if (homing_time == 0) homing_time = time;
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
