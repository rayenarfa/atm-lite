import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * One window for the whole app (fewer classes than many separate JFrame files).
 *
 * Sections:
 * 1) Window + navigation (CardLayout)
 * 2) Login card
 * 3) Register card
 * 4) Dashboard card
 * 5) Small UI helpers
 */
public class AtmUi extends JFrame {

    // -------------------------------------------------------------------------
    // 1) Window + navigation (CardLayout)
    // -------------------------------------------------------------------------

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    /** Currently logged-in user (null until login succeeds). */
    private AtmData.User session;

    /** Default-button targets (Enter key behaves like clicking these). */
    private JButton loginBtn;
    private JButton registerCreateBtn;

    // Login fields
    private JTextField loginUser;
    private JPasswordField loginPass;
    private JLabel loginMsg;

    // Register fields
    private JTextField regUser;
    private JPasswordField regPass;
    private JPasswordField regPin;
    private JPasswordField regPin2;
    private JLabel regMsg;

    // Dashboard fields
    private JLabel dashWelcome;
    private JLabel dashBalance;
    private JTextField dashAmount;
    private JPasswordField dashPin;
    private JPasswordField dashNewPin;
    private JPasswordField dashNewPin2;
    private JLabel dashMsg;

    public AtmUi() {
        super("ATM Lite");

        // Use the OS native look (usually nicer for teachers/students).
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(null);

        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(buildLoginCard(), "login");
        root.add(buildRegisterCard(), "register");
        root.add(buildDashboardCard(), "dashboard");

        setContentPane(root);
        showLogin();
        setVisible(true);
    }

    private void showLogin() {
        session = null;
        loginMsg.setText(" ");
        loginUser.setText("");
        loginPass.setText("");
        cards.show(root, "login");
        getRootPane().setDefaultButton(loginBtn);
        SwingUtilities.invokeLater(() -> loginUser.requestFocusInWindow());
    }

    private void showRegister() {
        regMsg.setText(" ");
        regUser.setText("");
        regPass.setText("");
        regPin.setText("");
        regPin2.setText("");
        cards.show(root, "register");
        getRootPane().setDefaultButton(registerCreateBtn);
        SwingUtilities.invokeLater(() -> regUser.requestFocusInWindow());
    }

    private void showDashboard() {
        refreshDashboardHeader();
        dashMsg.setText(" ");
        dashAmount.setText("");
        dashPin.setText("");
        dashNewPin.setText("");
        dashNewPin2.setText("");
        cards.show(root, "dashboard");
        // No default button on dashboard (avoid accidental submits).
        getRootPane().setDefaultButton(null);
    }

    // -------------------------------------------------------------------------
    // 2) Login card
    // -------------------------------------------------------------------------

    private JPanel buildLoginCard() {
        JPanel p = new JPanel(new BorderLayout(0, 16));

        JLabel title = new JLabel("ATM Lite", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));

        JLabel subtitle = new JLabel("Sign in with username and password", SwingConstants.CENTER);
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 6));
        top.add(title);
        top.add(subtitle);
        p.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Login"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Username"), g);
        g.gridy = 1;
        loginUser = new JTextField(18);
        form.add(loginUser, g);

        g.gridy = 2;
        form.add(new JLabel("Password"), g);
        g.gridy = 3;
        loginPass = new JPasswordField(18);
        form.add(loginPass, g);

        g.gridy = 4;
        g.gridwidth = 2;
        loginMsg = new JLabel(" ");
        loginMsg.setForeground(new Color(180, 40, 40));
        form.add(loginMsg, g);

        p.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton registerBtn = new JButton("Register");
        loginBtn = new JButton("Login");
        south.add(registerBtn);
        south.add(loginBtn);
        p.add(south, BorderLayout.SOUTH);

        // Enter in username/password fires the same action as clicking Login.
        Runnable doLogin = this::onLogin;
        loginUser.addActionListener(e -> doLogin.run());
        loginPass.addActionListener(e -> doLogin.run());
        loginBtn.addActionListener(e -> doLogin.run());

        registerBtn.addActionListener(e -> showRegister());

        return p;
    }

    private void onLogin() {
        String u = loginUser.getText().trim();
        String pw = new String(loginPass.getPassword()).trim();
        if (u.isEmpty() || pw.isEmpty()) {
            loginMsg.setText("Please enter username and password.");
            return;
        }

        AtmData.User logged = AtmData.login(u, pw);
        if (logged == null) {
            loginMsg.setText("Wrong username or password.");
            return;
        }

        session = logged;
        showDashboard();
    }

    // -------------------------------------------------------------------------
    // 3) Register card
    // -------------------------------------------------------------------------

    private JPanel buildRegisterCard() {
        JPanel p = new JPanel(new BorderLayout(0, 16));

        JLabel title = new JLabel("Create account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        p.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int row = 0;
        g.gridwidth = 1;

        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Username"), g);
        g.gridx = 1;
        regUser = new JTextField(18);
        form.add(regUser, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Password"), g);
        g.gridx = 1;
        regPass = new JPasswordField(18);
        form.add(regPass, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("PIN (4 digits)"), g);
        g.gridx = 1;
        regPin = new JPasswordField(18);
        form.add(regPin, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Confirm PIN"), g);
        g.gridx = 1;
        regPin2 = new JPasswordField(18);
        form.add(regPin2, g);

        row++;
        JLabel hint = new JLabel("Tip: PIN must be exactly 4 numbers, e.g. 1234");
        hint.setForeground(new Color(90, 90, 90));
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        form.add(hint, g);

        row++;
        regMsg = new JLabel(" ");
        regMsg.setForeground(new Color(180, 40, 40));
        g.gridy = row;
        form.add(regMsg, g);

        p.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton backBtn = new JButton("Back");
        registerCreateBtn = new JButton("Create account");
        south.add(backBtn);
        south.add(registerCreateBtn);
        p.add(south, BorderLayout.SOUTH);

        Runnable doRegister = this::onRegister;
        regUser.addActionListener(e -> doRegister.run());
        regPass.addActionListener(e -> doRegister.run());
        regPin.addActionListener(e -> doRegister.run());
        regPin2.addActionListener(e -> doRegister.run());
        registerCreateBtn.addActionListener(e -> doRegister.run());

        backBtn.addActionListener(e -> showLogin());

        return p;
    }

    private void onRegister() {
        String u = regUser.getText().trim();
        String p = new String(regPass.getPassword()).trim();
        String pin = new String(regPin.getPassword()).trim();
        String pin2 = new String(regPin2.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty() || pin.isEmpty() || pin2.isEmpty()) {
            regMsg.setText("Please fill all fields.");
            return;
        }
        if (!AtmData.pinOk(pin)) {
            regMsg.setText("PIN must be exactly 4 digits.");
            return;
        }
        if (!pin.equals(pin2)) {
            regMsg.setText("PINs do not match.");
            return;
        }

        if (!AtmData.register(u, p, pin)) {
            regMsg.setText("Cannot create account (username may already exist).");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created. Please sign in.");
        showLogin();
    }

    // -------------------------------------------------------------------------
    // 4) Dashboard card
    // -------------------------------------------------------------------------

    private JPanel buildDashboardCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 6));
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        dashWelcome = new JLabel(" ");
        dashWelcome.setFont(dashWelcome.getFont().deriveFont(Font.BOLD, 20f));
        dashBalance = new JLabel(" ");
        dashBalance.setFont(dashBalance.getFont().deriveFont(Font.BOLD, 16f));
        header.add(dashWelcome);
        header.add(dashBalance);
        p.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));

        JPanel money = new JPanel(new GridBagLayout());
        money.setBorder(BorderFactory.createTitledBorder("Deposit / Withdraw"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        g.gridx = 0;
        g.gridy = 0;
        money.add(new JLabel("Amount"), g);
        g.gridy = 1;
        dashAmount = new JTextField(10);
        money.add(dashAmount, g);
        g.gridy = 2;
        money.add(new JLabel("PIN confirm"), g);
        g.gridy = 3;
        dashPin = new JPasswordField(10);
        money.add(dashPin, g);

        JPanel moneyBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton dep = new JButton("Deposit");
        JButton wit = new JButton("Withdraw");
        moneyBtns.add(dep);
        moneyBtns.add(wit);
        g.gridy = 4;
        money.add(moneyBtns, g);

        JPanel pinPanel = new JPanel(new GridBagLayout());
        pinPanel.setBorder(BorderFactory.createTitledBorder("Change PIN"));
        GridBagConstraints pg = new GridBagConstraints();
        pg.insets = new Insets(8, 8, 8, 8);
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.weightx = 1;
        pg.gridx = 0;
        pg.gridy = 0;
        pinPanel.add(new JLabel("New PIN"), pg);
        pg.gridy = 1;
        dashNewPin = new JPasswordField(10);
        pinPanel.add(dashNewPin, pg);
        pg.gridy = 2;
        pinPanel.add(new JLabel("Confirm"), pg);
        pg.gridy = 3;
        dashNewPin2 = new JPasswordField(10);
        pinPanel.add(dashNewPin2, pg);
        JButton chg = new JButton("Update PIN");
        pg.gridy = 4;
        pinPanel.add(chg, pg);

        center.add(money);
        center.add(pinPanel);
        p.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        dashMsg = new JLabel(" ");
        dashMsg.setForeground(new Color(30, 90, 160));
        JButton logout = new JButton("Logout");
        bottom.add(dashMsg, BorderLayout.CENTER);
        bottom.add(logout, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);

        dep.addActionListener(e -> onDeposit());
        wit.addActionListener(e -> onWithdraw());
        chg.addActionListener(e -> onChangePin());
        logout.addActionListener(e -> showLogin());

        return p;
    }

    private void refreshDashboardHeader() {
        if (session == null) return;
        AtmData.User fresh = AtmData.reload(session.id);
        if (fresh != null) session = fresh;

        dashWelcome.setText("Welcome, " + session.username);
        dashBalance.setText("Balance: " + String.format("%.2f", session.balance));
    }

    private void onDeposit() {
        Double amount = readAmount(dashAmount.getText());
        String pin = new String(dashPin.getPassword()).trim();
        if (amount == null) {
            dashMsg.setText("Enter a valid amount (> 0).");
            return;
        }
        if (!AtmData.pinOk(pin)) {
            dashMsg.setText("Enter your 4-digit PIN to confirm.");
            return;
        }
        if (AtmData.deposit(session, amount, pin)) {
            dashMsg.setText("Deposit OK.");
            refreshDashboardHeader();
        } else {
            dashMsg.setText("Deposit failed (check PIN).");
        }
    }

    private void onWithdraw() {
        Double amount = readAmount(dashAmount.getText());
        String pin = new String(dashPin.getPassword()).trim();
        if (amount == null) {
            dashMsg.setText("Enter a valid amount (> 0).");
            return;
        }
        if (!AtmData.pinOk(pin)) {
            dashMsg.setText("Enter your 4-digit PIN to confirm.");
            return;
        }
        if (AtmData.withdraw(session, amount, pin)) {
            dashMsg.setText("Withdraw OK.");
            refreshDashboardHeader();
        } else {
            dashMsg.setText("Withdraw failed (PIN or not enough money).");
        }
    }

    private void onChangePin() {
        String pin = new String(dashNewPin.getPassword()).trim();
        String pin2 = new String(dashNewPin2.getPassword()).trim();
        if (!AtmData.pinOk(pin)) {
            dashMsg.setText("New PIN must be 4 digits.");
            return;
        }
        if (!pin.equals(pin2)) {
            dashMsg.setText("PIN confirmation does not match.");
            return;
        }
        if (AtmData.changePin(session, pin)) {
            dashMsg.setText("PIN updated.");
            refreshDashboardHeader();
        } else {
            dashMsg.setText("Could not update PIN.");
        }
    }

    // -------------------------------------------------------------------------
    // 5) Small UI helpers
    // -------------------------------------------------------------------------

    /** Parses a positive money amount; returns null if invalid. */
    private static Double readAmount(String text) {
        try {
            double v = Double.parseDouble(text.trim().replace(',', '.'));
            if (v <= 0) return null;
            return v;
        } catch (Exception e) {
            return null;
        }
    }
}
