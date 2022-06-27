package control;

import experiment.Experiment;
import panels.MainFrame;
import tools.Out;
import tools.Memo;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import static tools.Consts.*;

public class Server {
    private final static String NAME = "Server/";

    private static Server instance; // Singelton

    private final int PORT = 8000; // always the same
    private final int CONNECTION_TIMEOUT = 60 * 1000; // 1 min

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter outPW;
    private BufferedReader inBR;

    private String mPcDateId;

    private ExecutorService executor;

    //----------------------------------------------------------------------------------------

    //-- Runnable for waiting for incoming connections
    private class ConnectionRunnable implements Runnable {
        String TAG = NAME + "ConnectionRunnable";

        @Override
        public void run() {
            try {
                Out.d(TAG, "Waiting for connections...");
                serverSocket = new ServerSocket(PORT);
                socket = serverSocket.accept();

                // When reached here, Moose is connected
                Out.d(TAG, "Moose connected!");

                // Create streams
                inBR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outPW = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);

                // Start receiving
                executor.execute(new InRunnable());

                // Set the active technique/pId
                send(new Memo(
                        STRINGS.CONFIG,
                        STRINGS.TECH,
                        MainFrame.get().mActiveTechnique));

                // Send the exp_id
                if (MainFrame.get().mMode.equals(Experiment.MODE.TEST)) {
                    send(new Memo(
                            STRINGS.LOG,
                            STRINGS.EXP_ID,
                            Logger.get().getLogId()));
                } else if (MainFrame.get().mMode.equals(Experiment.MODE.PRACTICE)) {
                    send(new Memo(
                            STRINGS.LOG,
                            STRINGS.EXP_ID,
                            Logger.get().getPracticeLogId()));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //-- Runnable for sending messages to Moose
    private class OutRunnable implements Runnable {
        String TAG = NAME + "OutRunnable";
        Memo message;

        public OutRunnable(Memo mssg) {
            message = mssg;
        }

        @Override
        public void run() {
            if (message != null && outPW != null) {
                outPW.println(message);
                outPW.flush();
            }
        }
    }

    //-- Runnable for receiving messages from Moose
    private class InRunnable implements Runnable {
        String TAG = NAME + "InRunnable";

        @Override
        public void run() {
            while (!Thread.interrupted() && inBR != null) {
                try {
                    Out.d(TAG, "Reading messages...");
                    String message = inBR.readLine();
                    Out.d(TAG, "Message: " + message);
                    if (message != null) {
                        Memo memo = Memo.valueOf(message);

                        // On Moose connection, send the active technique
                        if (memo.getAction().equals(STRINGS.CONNECTION)) {

                            if (memo.getMode().equals(STRINGS.INTRO)) {
                                send(new Memo(
                                        STRINGS.CONFIG,
                                        STRINGS.TECH,
                                        MainFrame.get().mActiveTechnique));
                            }
                        }

                        // Dragging...
                        if (memo.getAction().equals(STRINGS.DRAG)) {

                            switch (memo.getMode()) {
                                case STRINGS.GRAB -> MainFrame.get().grab();
                                case STRINGS.RELEASE -> MainFrame.get().release();
                                case STRINGS.REVERT -> MainFrame.get().revert();
                            }
                        }

                    } else {
                        Out.d(TAG, "Moose disconnected.");
                        start();
                        return;
                    }
                } catch (IOException e) {
                    System.out.println("Error in reading from Moose");
                    start();
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------

    /**
     * Get the instance
     * @return single instance
     */
    public static Server get() {
        if (instance == null) instance = new Server();
        return instance;
    }

    /**
     * Constructor
     */
    public Server() {
        String TAG = NAME;

        // Init executerService for running threads
//        executor = Executors.newCachedThreadPool();
    }

    /**
     * Start the server
     */
    public void start() {
        String TAG = NAME + "start";

        executor = Executors.newCachedThreadPool();
        executor.execute(new ConnectionRunnable());
    }

    /**
     * Send a Memo to the Moose
     * Called from outside
     * @param mssg Memo message
     */
    public void send(Memo mssg) {
        Out.d(NAME, mssg);
        if (executor != null) executor.execute(new OutRunnable(mssg));
    }

    public void close() {
        try {
            // Send end message to the Moose
            send(new Memo(STRINGS.CONNECTION, STRINGS.END, ""));

            // Close the socket, etc.
            if (serverSocket != null && socket != null) {
                Out.d(NAME, "Closing the socket...");
                serverSocket.close();
                socket.close();
            }
            Out.d(NAME, "Shutting down the executer...");
            if (executor != null) executor.shutdownNow();
        } catch (IOException e) {
            Out.e(NAME, "Couldn't close the socket!");
            e.printStackTrace();
        }
    }
}
