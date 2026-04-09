import java.awt.*;
import java.awt.event.*;

/**
 * ScoreInputClient — Basketball Scoring System (Milestone 2)
 *
 * Main window for one scorer client. Connects to the BasketballScoreServer
 * on port 5000 and lets a scorer send game events in real time.
 *
 * Usage:  java ScoreInputClient [serverIP]
 *         java ScoreInputClient 192.168.1.10
 */
public class ScoreInputClient extends Frame {

    private static final int    SERVER_PORT = 5000;
    private static final String DEFAULT_IP  = "";

    private ScoreInputUI   ui;
    private ScoreInputComm comm;

    // ── Entry point ────────────────────────────────────────────────

    public static void main(String[] args) {
        // Show connection dialog to get server IP
        String serverIP = (args.length > 0) ? args[0] : showIPPrompt();
        if (serverIP == null || serverIP.isBlank()) {
            System.out.println("No server IP provided. Exiting.");
            System.exit(0);
        }
        new ScoreInputClient(serverIP.trim());
    }

    /**
     * Shows a simple AWT dialog asking for the server IP address.
     * Returns null if the user cancels.
     */
    private static String showIPPrompt() {
        // Build a modal dialog
        Frame dummy = new Frame();
        Dialog dialog = new Dialog(dummy, "Connect to Server", true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setBackground(new Color(24, 24, 42));
        dialog.setResizable(false);

        // Title
        Label title = new Label("Basketball Scoring System", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(255, 210, 60));

        Label subtitle = new Label("Enter the server IP address to connect.", Label.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 180, 220));

        Panel topPanel = new Panel(new GridLayout(2, 1, 0, 6));
        topPanel.setBackground(new Color(24, 24, 42));
        topPanel.add(title);
        topPanel.add(subtitle);

        // IP input row
        Panel inputRow = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        inputRow.setBackground(new Color(24, 24, 42));

        Label ipLabel = new Label("Server IP:");
        ipLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        ipLabel.setForeground(Color.WHITE);

        TextField ipField = new TextField(DEFAULT_IP, 18);
        ipField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ipField.setBackground(new Color(40, 40, 60));
        ipField.setForeground(Color.WHITE);
        ipField.selectAll();

        inputRow.add(ipLabel);
        inputRow.add(ipField);

        // Buttons row
        Panel btnRow = new Panel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        btnRow.setBackground(new Color(24, 24, 42));

        Button btnConnect = new Button("  Connect  ");
        btnConnect.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnConnect.setBackground(new Color(40, 120, 200));
        btnConnect.setForeground(Color.WHITE);

        Button btnCancel = new Button("  Cancel  ");
        btnCancel.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCancel.setBackground(new Color(80, 60, 60));
        btnCancel.setForeground(new Color(255, 180, 180));

        btnRow.add(btnConnect);
        btnRow.add(btnCancel);

        dialog.add(topPanel,  BorderLayout.NORTH);
        dialog.add(inputRow,  BorderLayout.CENTER);
        dialog.add(btnRow,    BorderLayout.SOUTH);

        // Result holder
        String[] result = { null };

        btnConnect.addActionListener(e -> {
            result[0] = ipField.getText().trim();
            dialog.dispose();
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        // Allow pressing Enter in the text field to confirm
        ipField.addActionListener(e -> {
            result[0] = ipField.getText().trim();
            dialog.dispose();
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dialog.dispose(); }
        });

        dialog.setSize(380, 200);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((screen.width - 380) / 2, (screen.height - 200) / 2);
        dialog.setVisible(true); // blocks until disposed

        dummy.dispose();
        return result[0];
    }

    // ── Constructor ────────────────────────────────────────────────

    public ScoreInputClient(String serverIP) {
        super("🏀 Basketball Scorer — Connecting…");

        // 1. Build the UI panel
        ui = new ScoreInputUI();

        // 2. Build the comm layer and link it to the UI
        comm = new ScoreInputComm(ui);
        ui.setComm(comm);

        // 3. Assemble the Frame
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(18, 18, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(ui,            BorderLayout.CENTER);

        setSize(680, 720);
        setResizable(false);
        centerOnScreen();

        // 4. Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                comm.disconnect();
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);

        // 5. Connect to server (after window is visible so status updates show)
        ui.setStatus("Connecting to " + serverIP + ":" + SERVER_PORT + " …");
        comm.connect(serverIP, SERVER_PORT);

        // Update the frame title to include scorer ID once connected
        if (comm.isConnected()) {
            setTitle("🏀 Basketball Scorer — " + comm.getScorerID());
        }
    }

    // ── Header bar ─────────────────────────────────────────────────

    private Panel buildHeader() {
        Panel header = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        header.setBackground(new Color(10, 10, 20));

        Label title = new Label("BASKETBALL SCORING SYSTEM", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(new Color(255, 210, 60));
        header.add(title);

        return header;
    }

    // ── Helpers ────────────────────────────────────────────────────

    private void centerOnScreen() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width  - getWidth())  / 2;
        int y = (screen.height - getHeight()) / 2;
        setLocation(x, y);
    }
}
