import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Small helper for SQLite: open a connection and create tables once.
 *
 * The database file is created next to where you run the program (atm_lite.db).
 */
public final class Database {

    /** JDBC URL for a file-based SQLite database in the current working directory. */
    private static final String URL = "jdbc:sqlite:atm_lite.db";

    /** Private constructor: this class only has static methods (no objects needed). */
    private Database() {
    }

    /**
     * Opens one connection to SQLite.
     * Callers should use try-with-resources so the connection closes automatically.
     */
    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL);
    }

    /**
     * Creates required tables if they are missing.
     * Safe to call every time the app starts.
     */
    public static void init() {
        // try-with-resources closes Connection and Statement automatically.
        try (Connection c = connect(); Statement s = c.createStatement()) {

            // One row per bank user.
            s.execute("""
                    CREATE TABLE IF NOT EXISTS users(
                        id INTEGER PRIMARY KEY,
                        username TEXT UNIQUE,
                        password TEXT,
                        pin TEXT,
                        balance REAL DEFAULT 0
                    )
                """);

            // Simple history of actions (deposit / withdraw / change pin).
            s.execute("""
                    CREATE TABLE IF NOT EXISTS logs(
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER,
                        action TEXT,
                        amount REAL,
                        time TEXT
                    )
                """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
