package log;

import experiment.Experiment;
import tools.Utils;

import java.awt.*;
import java.lang.reflect.Field;

import static tools.Consts.STRINGS.SP;

public class ActionLog {
    public Experiment.ACTION type;

    public int x;
    public int y;

    public long moment;

    public ActionLog(Experiment.ACTION type, Point p) {
        this.type = type;
        this.x = p.x;
        this.y = p.y;
        this.moment = Utils.nowMillis();
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
