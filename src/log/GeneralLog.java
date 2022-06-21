package log;

import experiment.Experiment;
import experiment.Task;

import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class GeneralLog {
    public Experiment.TASK task;
    public Experiment.TECHNIQUE technique;
    public int block_num;
    public int trial_num;

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
