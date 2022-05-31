package control;


/**
 * Class responsible for getting the data from the Server and perform the actions
 */
public class Controller {
    private final String NAME = "Controller/"; // class tag

    public static Controller instance; // Singleton

    //------------------------------------------------------------------------------------------

    /**
     * Get the single instance
     * @return Singleton instnace
     */
    public static Controller get() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    /**
     * Contrsuctor
     */
    private Controller() {
        String TAG = NAME;

    }

    // Actions ---------------------------------------------------------------------------------------

    // Runnables -------------------------------------------------------------------------------------



}
