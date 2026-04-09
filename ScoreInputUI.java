import java.awt.*;
import java.awt.event.*;

/**
 * AWT UI panel for the ScoreInputClient.
 * Contains the live scoreboard display and all scorer input buttons.
 */
public class ScoreInputUI extends Panel {

    // ── Scoreboard labels ──────────────────────────────────────────
    private Label lblScoreA      = new Label("0",  Label.CENTER);
    private Label lblScoreB      = new Label("0",  Label.CENTER);
    private Label lblFoulsA      = new Label("0",  Label.CENTER);
    private Label lblFoulsB      = new Label("0",  Label.CENTER);
    private Label lblTimeoutsA   = new Label("3",  Label.CENTER);
    private Label lblTimeoutsB   = new Label("3",  Label.CENTER);
    private Label lblQuarter     = new Label("Q1", Label.CENTER);
    private Label lblActiveTeam  = new Label("▶ Team A", Label.CENTER);
    private Label lblStatus      = new Label("Connecting…", Label.CENTER);
    private Label lblConnectionStatus = new Label("Not connected", Label.CENTER);

    // ── Score input buttons ────────────────────────────────────────
    private Button btn2pt     = new Button("+2  Field Goal");
    private Button btn3pt     = new Button("+3  Three-Point");
    private Button btnFoul    = new Button("🟥  Foul");
    private Button btnTimeout = new Button("⏱  Timeout");
    private Button btnPause   = new Button("⏸  Pause / Resume");
    private Button btnNextQ   = new Button("⏭  Next Quarter");
    private Button btnTeamA   = new Button("Switch → Team A");
    private Button btnTeamB   = new Button("Switch → Team B");
    private Button btnReset   = new Button("↺  Reset Game");

    // Reference to comm layer for sending commands
    private ScoreInputComm comm;

    public ScoreInputUI() {
        buildUI();
    }

    public void setComm(ScoreInputComm comm) {
        this.comm = comm;
        wireButtonActions();
    }

    // ── Build the full AWT layout ──────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(18, 18, 30));

        add(buildScoreboard(), BorderLayout.NORTH);
        add(buildControls(),   BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    /**
     * Top section: team scores, fouls, timeouts, quarter, status.
     */
    private Panel buildScoreboard() {
        Panel board = new Panel(new GridLayout(1, 3, 12, 0));
        board.setBackground(new Color(24, 24, 42));

        // ── Team A column ──────────────────────────────────────────
        Panel teamA = new Panel(new GridLayout(5, 1, 0, 4));
        teamA.setBackground(new Color(30, 60, 114));
        teamA.add(makeLabel("TEAM  A", Color.WHITE, 14, Font.BOLD));
        styleScoreLabel(lblScoreA, new Color(255, 220, 50));
        teamA.add(lblScoreA);
        teamA.add(makeLabel("Fouls", Color.LIGHT_GRAY, 11, Font.PLAIN));
        styleStat(lblFoulsA, Color.WHITE);
        teamA.add(lblFoulsA);
        Panel toA = new Panel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        toA.setBackground(new Color(30, 60, 114));
        toA.add(makeLabel("TOs:", Color.LIGHT_GRAY, 11, Font.PLAIN));
        styleStat(lblTimeoutsA, new Color(100, 220, 180));
        toA.add(lblTimeoutsA);
        teamA.add(toA);

        // ── Center column: quarter + game status ───────────────────
        Panel center = new Panel(new GridLayout(5, 1, 0, 4));
        center.setBackground(new Color(24, 24, 42));
        center.add(makeLabel("QUARTER", Color.GRAY, 11, Font.PLAIN));
        styleScoreLabel(lblQuarter, new Color(200, 200, 255));
        lblQuarter.setFont(new Font("SansSerif", Font.BOLD, 28));
        center.add(lblQuarter);
        center.add(makeLabel("Active", Color.GRAY, 11, Font.PLAIN));
        lblActiveTeam.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblActiveTeam.setForeground(new Color(100, 220, 180));
        center.add(lblActiveTeam);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setForeground(new Color(100, 220, 100));
        center.add(lblStatus);

        // ── Team B column ──────────────────────────────────────────
        Panel teamB = new Panel(new GridLayout(5, 1, 0, 4));
        teamB.setBackground(new Color(114, 30, 60));
        teamB.add(makeLabel("TEAM  B", Color.WHITE, 14, Font.BOLD));
        styleScoreLabel(lblScoreB, new Color(255, 220, 50));
        teamB.add(lblScoreB);
        teamB.add(makeLabel("Fouls", Color.LIGHT_GRAY, 11, Font.PLAIN));
        styleStat(lblFoulsB, Color.WHITE);
        teamB.add(lblFoulsB);
        Panel toB = new Panel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        toB.setBackground(new Color(114, 30, 60));
        toB.add(makeLabel("TOs:", Color.LIGHT_GRAY, 11, Font.PLAIN));
        styleStat(lblTimeoutsB, new Color(100, 220, 180));
        toB.add(lblTimeoutsB);
        teamB.add(toB);

        board.add(teamA);
        board.add(center);
        board.add(teamB);

        // Pad the board
        Panel wrapper = new Panel(new BorderLayout());
        wrapper.setBackground(new Color(18, 18, 30));
        wrapper.add(board, BorderLayout.CENTER);
        wrapper.add(new Label(""), BorderLayout.SOUTH); // spacing
        return wrapper;
    }

    /**
     * Middle section: all command buttons in a grid.
     */
    private Panel buildControls() {
        Panel outer = new Panel(new BorderLayout(0, 6));
        outer.setBackground(new Color(18, 18, 30));

        Label sectionTitle = makeLabel("── SCORER CONTROLS ──", new Color(140, 140, 180), 11, Font.PLAIN);
        outer.add(sectionTitle, BorderLayout.NORTH);

        Panel grid = new Panel(new GridLayout(3, 3, 8, 8));
        grid.setBackground(new Color(18, 18, 30));

        styleBtn(btn2pt,     new Color(40, 120, 200),  Color.WHITE, 20);
        styleBtn(btn3pt,     new Color(20, 80,  160),  Color.WHITE, 20);
        styleBtn(btnFoul,    new Color(180, 50, 50),   Color.WHITE, 18);
        styleBtn(btnTimeout, new Color(160, 110, 20),  Color.WHITE, 18);
        styleBtn(btnPause,   new Color(80, 80, 140),   Color.WHITE, 18);
        styleBtn(btnNextQ,   new Color(60, 140, 80),   Color.WHITE, 18);
        styleBtn(btnTeamA,   new Color(30, 60, 114),   Color.WHITE, 16);
        styleBtn(btnTeamB,   new Color(114, 30, 60),   Color.WHITE, 16);
        styleBtn(btnReset,   new Color(60, 60, 60),    new Color(255, 180, 180), 16);

        grid.add(btn2pt);
        grid.add(btn3pt);
        grid.add(btnFoul);
        grid.add(btnTimeout);
        grid.add(btnPause);
        grid.add(btnNextQ);
        grid.add(btnTeamA);
        grid.add(btnTeamB);
        grid.add(btnReset);

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }

    /**
     * Bottom status bar.
     */
    private Panel buildStatusBar() {
        Panel bar = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(new Color(10, 10, 20));
        lblConnectionStatus.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblConnectionStatus.setForeground(new Color(100, 180, 100));
        bar.add(lblConnectionStatus);
        return bar;
    }

    // ── Button wiring ──────────────────────────────────────────────

    private void wireButtonActions() {
        btn2pt.addActionListener(e  -> send("2"));
        btn3pt.addActionListener(e  -> send("3"));
        btnFoul.addActionListener(e -> send("F"));
        btnTimeout.addActionListener(e -> send("T"));
        btnPause.addActionListener(e   -> send("P"));
        btnNextQ.addActionListener(e   -> send("Q"));
        btnTeamA.addActionListener(e   -> send("A"));
        btnTeamB.addActionListener(e   -> send("B"));
        btnReset.addActionListener(e   -> send("R"));
    }

    private void send(String cmd) {
        if (comm != null && comm.isConnected()) {
            comm.sendCommand(cmd);
        }
    }

    // ── Public update methods called by ScoreInputComm ─────────────

    /**
     * Called on every STATE broadcast to refresh all labels.
     */
    public void updateState(int scoreA, int scoreB,
                            int foulsA, int foulsB,
                            int timeoutsA, int timeoutsB,
                            int quarter, String activeTeam, String status) {

        lblScoreA.setText(String.valueOf(scoreA));
        lblScoreB.setText(String.valueOf(scoreB));
        lblFoulsA.setText(String.valueOf(foulsA));
        lblFoulsB.setText(String.valueOf(foulsB));
        lblTimeoutsA.setText(String.valueOf(timeoutsA));
        lblTimeoutsB.setText(String.valueOf(timeoutsB));
        lblQuarter.setText("Q" + quarter);

        lblActiveTeam.setText("▶ Team " + activeTeam);
        lblActiveTeam.setForeground(
            activeTeam.equalsIgnoreCase("A")
                ? new Color(80, 160, 255)
                : new Color(255, 100, 120)
        );

        switch (status.toUpperCase()) {
            case "LIVE":
                lblStatus.setText("● LIVE");
                lblStatus.setForeground(new Color(80, 220, 100));
                enableControls();
                break;
            case "PAUSED":
                lblStatus.setText("⏸ PAUSED");
                lblStatus.setForeground(new Color(255, 200, 60));
                enableControls();
                break;
            case "GAMEOVER":
                lblStatus.setText("■ GAME OVER");
                lblStatus.setForeground(new Color(220, 80, 80));
                disableScoringButtons();
                break;
            default:
                lblStatus.setText(status);
        }
    }

    public void setTitle(String title) {
        // Forwarded from Frame; stored for reference
    }

    public void setStatus(String msg) {
        lblConnectionStatus.setText(msg);
    }

    public void disableControls() {
        setControlsEnabled(false);
    }

    public void enableControls() {
        setControlsEnabled(true);
    }

    private void disableScoringButtons() {
        btn2pt.setEnabled(false);
        btn3pt.setEnabled(false);
        btnFoul.setEnabled(false);
        btnTimeout.setEnabled(false);
        btnNextQ.setEnabled(false);
        // Keep pause, team switch, and reset available
    }

    private void setControlsEnabled(boolean enabled) {
        btn2pt.setEnabled(enabled);
        btn3pt.setEnabled(enabled);
        btnFoul.setEnabled(enabled);
        btnTimeout.setEnabled(enabled);
        btnPause.setEnabled(enabled);
        btnNextQ.setEnabled(enabled);
        btnTeamA.setEnabled(enabled);
        btnTeamB.setEnabled(enabled);
        btnReset.setEnabled(enabled);
    }

    // ── Styling helpers ────────────────────────────────────────────

    private Label makeLabel(String text, Color fg, int size, int style) {
        Label l = new Label(text, Label.CENTER);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(fg);
        return l;
    }

    private void styleScoreLabel(Label l, Color fg) {
        l.setFont(new Font("SansSerif", Font.BOLD, 48));
        l.setForeground(fg);
    }

    private void styleStat(Label l, Color fg) {
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(fg);
    }

    private void styleBtn(Button b, Color bg, Color fg, int size) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, size));
    }
}
