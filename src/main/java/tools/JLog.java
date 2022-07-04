package tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JLog {

    public static Logger app;
    public static Logger net;

    static {
        app = LogManager.getLogger("app");
        net = LogManager.getLogger("net");
    }


    public static void tr(Object mssg) {
        app.trace(mssg);
    }

    public static void de(Object mssg) {
        app.debug(mssg);
    }

    public static void er(Object mssg) {
        app.error(mssg);
    }

}
