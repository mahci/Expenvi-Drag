package log;

import experiment.Experiment;
import experiment.Task;
import experiment.Trial;

import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class GeneralLog {
    public Experiment.TASK task;
    public Experiment.TECHNIQUE technique;
    public int block_num;
    public int trial_num;
    public String trialStr; // Set by each Trial subclass

    public static String getLogHeader() {
        return "task" + SP +
                "technique" + SP +
                "block_num" + SP +
                "trial_num" + SP +
                Trial.getLogHeader();
    }

    @Override
    public String toString() {
        return task + SP +
                technique + SP +
                block_num + SP  +
                trial_num + SP +
                trialStr;
    }

//    @Override
//    public String toString() {
//        final Field[] fields = getClass().getDeclaredFields();
//        StringBuilder sb = new StringBuilder();
//        try {
//            for (Field field : fields) {
//                sb.append(field.get(this)).append(SP);
//            }
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//
//        return sb.deleteCharAt(sb.length() - 1).toString();
//    }

}
