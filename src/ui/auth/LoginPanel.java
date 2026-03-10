package ui.auth;

import controllers.AuthController;
import ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginPanel — Full redesign with gradient dark background and premium card UI.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding
 */
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static final Color BG1 = new Color(10, 15, 35);
    private static final Color BG2 = new Color(20, 30, 65);
    private static final Color CARD_BG = new Color(22, 30, 55, 230);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(236, 72, 153);
    private static final Color TEXT_PRIMARY = new Color(237, 242, 247);
    private static final Color TEXT_MUTED = new Color(160, 174, 192);
    private static final Color INPUT_BG = new Color(30, 42, 75);
    private static final Color INPUT_BORDER = new Color(50, 70, 110);

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authController = new AuthController();
        setLayout(new GridBagLayout());
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, BG1, getWidth(), getHeight(), BG2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // decorative circles
        g2.setColor(new Color(99, 179, 237, 18));
        g2.fillOval(-80, -80, 300, 300);
        g2.fillOval(getWidth() - 150, getHeight() - 150, 300, 300);
        g2.setColor(new Color(236, 72, 153, 12));
        g2.fillOval(getWidth() - 200, -100, 350, 350);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Defer so getWidth/getHeight available
        SwingUtilities.invokeLater(this::buildUI);
    }

    private void buildUI() {
        removeAll();
        setLayout(new GridBagLayout());

        // Card panel
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                // Border
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(99, 179, 237, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 520));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        c.gridx = 0;

        // Logo icon
        JLabel icon = new JLabel("\uD83C\uDFE0");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(icon, c);

        // Title (gradient text workaround via label)
        JLabel title = createGradientLabel("RoomCraft Designer", new Font("Segoe UI", Font.BOLD, 26));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 2, 0);
        card.add(title, c);

        JLabel sub = new JLabel("Sign in to your workspace");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 24, 0);
        card.add(sub, c);

        // Username
        c.insets = new Insets(4, 0, 4, 0);
        JLabel uLabel = new JLabel("Username");
        uLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uLabel.setForeground(TEXT_MUTED);
        c.gridy = 3;
        card.add(uLabel, c);

        usernameField = createStyledField(false);
        c.gridy = 4;
        card.add(usernameField, c);

        // Password
        JLabel pLabel = new JLabel("Password");
        pLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pLabel.setForeground(TEXT_MUTED);
        c.gridy = 5;
        c.insets = new Insets(12, 0, 4, 0);
        card.add(pLabel, c);

        passwordField = (JPasswordField) createStyledField(true);
        c.gridy = 6;
        c.insets = new Insets(4, 0, 4, 0);
        card.add(passwordField, c);

        // Spacer
        c.gridy = 7;
        c.insets = new Insets(16, 0, 8, 0);
        card.add(createGradientButton("Sign In", true), c);

        c.gridy = 8;
        c.insets = new Insets(8, 0, 0, 0);
        JButton regBtn = createGradientButton("Create Account", false);
        card.add(regBtn, c);

        // Wire button actions by scanning card children
        Component[] comps = card.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JButton btn) {
                if ("Sign In".equals(btn.getText())) {
                    btn.addActionListener(e -> handleLogin());
                } else if ("Create Account".equals(btn.getText())) {
                    btn.addActionListener(e -> handleRegister());
                }
            }
        }

        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // Add card to panel
        GridBagConstraints cc = new GridBagConstraints();
        cc.anchor = GridBagConstraints.CENTER;
        add(card, cc);

        revalidate();
        repaint();
    }

    private JTextField createStyledField(boolean password) {
        JTextField field = password ? new JPasswordField(20) : new JTextField(20);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        field.setPreferredSize(new Dimension(340, 44));

        // Focus highlight
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });
        return field;
    }

    private JButton createGradientButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1, c2;
                if (primary) {
                    c1 = hover ? new Color(120, 195, 255) : ACCENT;
                    c2 = hover ? new Color(250, 100, 180) : ACCENT2;
                } else {
                    g2.setColor(hover ? new Color(40, 55, 90) : new Color(30, 42, 75));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(TEXT_MUTED);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(),
                            (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    return;
                }
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(340, 46));
        return btn;
    }

    private JLabel createGradientLabel(String text, Font font) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                g2.drawString(getText(), x, fm.getAscent());
            }
        };
        lbl.setFont(font);
        lbl.setPreferredSize(new Dimension(340, font.getSize() + 10));
        return lbl;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        // Show loading or disable buttons?
        setLoading(true);

        authController.login(user, pass).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                setLoading(false);
                if (success) {
                    mainFrame.showView("DASHBOARD");
                } else {
                    showError("Invalid username or password.", "Login Failed");
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                setLoading(false);
                showError("Connection error: " + ex.getMessage(), "Error");
            });
            return null;
        });
    }

    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please fill in both fields.", "Missing Fields");
            return;
        }
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters for Supabase.", "Weak Password");
            return;
        }

        setLoading(true);
        authController.register(user, pass).thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                setLoading(false);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Account created! Please check your email for verification (if enabled) or sign in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    passwordField.setText("");
                } else {
                    showError("Registration failed. Email might be invalid or already taken.", "Failed");
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                setLoading(false);
                showError("Connection error: " + ex.getMessage(), "Error");
            });
            return null;
        });
    }

    private void setLoading(boolean loading) {
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        // Find buttons and disable them
        for (Component c : getComponents()) {
            if (c instanceof JPanel card) {
                for (Component cc : card.getComponents()) {
                    if (cc instanceof JButton b)
                        b.setEnabled(!loading);
                }
            }
        }
    }

    private void showError(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
}
