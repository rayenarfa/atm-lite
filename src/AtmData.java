import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

/**
 * All “business rules” + SQL in one small class (easy to read for class demos).
 *
 * Sections:
 * 1) User model (in-memory)
 * 2) PIN helper
 * 3) Register / login / reload user
 * 4) Deposit / withdraw / change PIN + logs
 */
public final class AtmData {

    private AtmData() {
    }

    // -------------------------------------------------------------------------
    // 1) User model (in-memory)
    // -------------------------------------------------------------------------

    /**
     * Represents one logged-in user in RAM (not the whole database row).
     */
    public static class User {
        public int id;
        public String username;
        public String pin;
        public double balance;
    }

    // -------------------------------------------------------------------------
    // 2) PIN helper
    // -------------------------------------------------------------------------

    /** PIN must be exactly 4 digits (0-9), educational simplicity. */
    public static boolean pinOk(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    // -------------------------------------------------------------------------
    // 3) Register / login / reload user
    // -------------------------------------------------------------------------

    /** Creates a new user row. Returns false if username already exists or DB error. */
    public static boolean register(String username, String password, String pin) {
        try (Connection c = Database.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users(username,password,pin) VALUES(?,?,?)"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, pin);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Reads a user if username+password match. Returns null if login fails. */
    public static User login(String username, String password) {
        try (Connection c = Database.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT id,username,pin,balance FROM users WHERE username=? AND password=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            User u = new User();
            u.id = rs.getInt("id");
            u.username = rs.getString("username");
            u.pin = rs.getString("pin");
            u.balance = rs.getDouble("balance");
            return u;
        } catch (Exception e) {
            return null;
        }
    }

    /** Reloads balance/PIN/username after a transaction (fresh data from DB). */
    public static User reload(int userId) {
        try (Connection c = Database.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT id,username,pin,balance FROM users WHERE id=?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            User u = new User();
            u.id = rs.getInt("id");
            u.username = rs.getString("username");
            u.pin = rs.getString("pin");
            u.balance = rs.getDouble("balance");
            return u;
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // 4) Deposit / withdraw / change PIN + logs
    // -------------------------------------------------------------------------

    /** Adds money if PIN matches. Also writes a log row. */
    public static boolean deposit(User u, double amount, String enteredPin) {
        if (u == null || amount <= 0 || !pinOk(enteredPin)) return false;

        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET balance = balance + ? WHERE id=? AND pin=?"
            );
            ps.setDouble(1, amount);
            ps.setInt(2, u.id);
            ps.setString(3, enteredPin);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                c.rollback();
                return false;
            }

            insertLog(c, u.id, "deposit", amount);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Removes money if PIN matches and balance is enough. Also writes a log row. */
    public static boolean withdraw(User u, double amount, String enteredPin) {
        if (u == null || amount <= 0 || !pinOk(enteredPin)) return false;

        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET balance = balance - ? WHERE id=? AND pin=? AND balance >= ?"
            );
            ps.setDouble(1, amount);
            ps.setInt(2, u.id);
            ps.setString(3, enteredPin);
            ps.setDouble(4, amount);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                c.rollback();
                return false;
            }

            // Store withdrawals as negative amounts in logs (simple convention).
            insertLog(c, u.id, "withdraw", -amount);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Updates PIN (educational demo: no “old PIN” check). */
    public static boolean changePin(User u, String newPin) {
        if (u == null || !pinOk(newPin)) return false;

        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);

            PreparedStatement ps = c.prepareStatement("UPDATE users SET pin=? WHERE id=?");
            ps.setString(1, newPin);
            ps.setInt(2, u.id);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                c.rollback();
                return false;
            }

            insertLog(c, u.id, "change_pin", 0);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Internal helper: append one line to logs inside an open transaction. */
    private static void insertLog(Connection c, int userId, String action, double amount) throws Exception {
        PreparedStatement log = c.prepareStatement(
                "INSERT INTO logs(user_id,action,amount,time) VALUES(?,?,?,?)"
        );
        log.setInt(1, userId);
        log.setString(2, action);
        log.setDouble(3, amount);
        log.setString(4, LocalDateTime.now().toString());
        log.executeUpdate();
    }
}
