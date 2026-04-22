/**
 * ATM Lite — minimal educational ATM (Swing + SQLite).
 *
 * This class is the program entry point: Java always starts execution in main().
 */
public class Main {

    /**
     * Starts the application on the Swing event thread (EDT).
     * Swing windows must be created on the EDT to avoid UI bugs.
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Create database tables if they do not exist yet.
            Database.init();
            // Open the single main window (login screen first).
            new AtmUi();
        });
    }
}
