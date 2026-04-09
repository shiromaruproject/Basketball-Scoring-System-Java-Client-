import java.io.*;
import java.net.*;

/**
 * Handles all TCP networking for the ScoreInputClient.
 * Manages connection, background listener thread, and sending commands.
 */
public class ScoreInputComm {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String scorerID = "SCORER_?";
    private boolean connected = false;

    private ScoreInputUI ui;

    public ScoreInputComm(ScoreInputUI ui) {
        this.ui = ui;
    }

    /**
     * Connects to the basketball score server.
     * @param host Server IP address
     * @param port Server port (5000)
     */
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            connected = true;

            // Read the handshake: CONNECTED|SCORER_X
            String handshake = in.readLine();
            if (handshake != null && handshake.startsWith("CONNECTED|")) {
                scorerID = handshake.split("\\|")[1];
                ui.setTitle("🏀 Basketball Scorer — " + scorerID);
                ui.setStatus("Connected as " + scorerID);
            }

            startListenerThread();

        } catch (IOException e) {
            connected = false;
            ui.setStatus("⚠ Connection failed: " + e.getMessage());
            ui.disableControls();
        }
    }

    /**
     * Starts the background thread that reads STATE updates from the server.
     */
    private void startListenerThread() {
        Thread listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    handleServerMessage(msg);
                }
            } catch (IOException e) {
                if (connected) {
                    ui.setStatus("⚠ Disconnected from server.");
                    ui.disableControls();
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Routes incoming server messages to the appropriate handler.
     */
    private void handleServerMessage(String message) {
        if (message == null) return;

        if (message.startsWith("STATE|")) {
            parseAndApplyState(message);
        }
        // Future message types can be handled here
    }

    /**
     * Parses a STATE|... message and updates the UI.
     * Format: STATE|scoreA|scoreB|foulsA|foulsB|timeoutsA|timeoutsB|quarter|activeTeam|status
     */
    private void parseAndApplyState(String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length < 10) return;

            int scoreA    = parseIntSafe(parts[1], 0);
            int scoreB    = parseIntSafe(parts[2], 0);
            int foulsA    = parseIntSafe(parts[3], 0);
            int foulsB    = parseIntSafe(parts[4], 0);
            int timeoutsA = parseIntSafe(parts[5], 3);
            int timeoutsB = parseIntSafe(parts[6], 3);
            int quarter   = parseIntSafe(parts[7], 1);
            String active = parts[8];
            String status = parts[9];

            // AWT is single-threaded; use EventQueue to be safe
            java.awt.EventQueue.invokeLater(() ->
                ui.updateState(scoreA, scoreB, foulsA, foulsB,
                               timeoutsA, timeoutsB, quarter, active, status)
            );

        } catch (Exception e) {
            // Never crash on a malformed state — just ignore
            System.err.println("Malformed STATE message: " + message);
        }
    }

    /**
     * Sends a single command to the server (e.g., "2", "3", "F").
     */
    public void sendCommand(String command) {
        if (connected && out != null) {
            out.println(command);
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return connected;
    }

    public String getScorerID() {
        return scorerID;
    }

    private int parseIntSafe(String s, int defaultVal) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}
