package ui;

import javax.swing.*;
import java.awt.*;

/**
 * SplashScreen — Animated startup splash with logo and progress bar.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding
 */
public class SplashScreen extends JWindow {

    private int progress = 0;
    private JLabel statusLabel;
    private static final Color BG_DARK = new Color(15, 20, 40);
    private static final Color BG_MID = new Color(25, 35, 70);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(236, 72, 153);

    public SplashScreen() {
        setSize(520, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), BG_MID);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Decorative circles
                g2.setColor(new Color(99, 179, 237, 30));
                g2.fillOval(-40, -40, 200, 200);
                g2.setColor(new Color(236, 72, 153, 20));
                g2.fillOval(350, 100, 200, 200);
            }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createLineBorder(new Color(99, 179, 237, 80), 1));

        // Logo area
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel icon = new JLabel("\uD83C\uDFE0");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 4, 0);
        logoPanel.add(icon, gbc);

        JLabel title = new JLabel("RoomCraft Designer") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, fm.getAscent());
            }
        };
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setPreferredSize(new Dimension(400, 40));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        logoPanel.add(title, gbc);

        JLabel subtitle = new JLabel("3D Interior Design & Visualization Suite");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(160, 174, 192));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        logoPanel.add(subtitle, gbc);

        content.add(logoPanel, BorderLayout.CENTER);

        // Bottom: progress bar + status
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(160, 174, 192));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        // Custom progress bar
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Track
                g2.setColor(new Color(30, 40, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                // Fill
                int fillW = (int) (getWidth() * getValue() / 100.0);
                if (fillW > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT, fillW, 0, ACCENT2);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, fillW, getHeight(), 6, 6);
                }
            }
        };
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setBorderPainted(false);
        bar.setOpaque(false);
        bottomPanel.add(bar, BorderLayout.CENTER);

        JLabel versionLabel = new JLabel("v2.0 — PUSL3122 Group Project");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(100, 110, 130));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        bottomPanel.add(versionLabel, BorderLayout.SOUTH);

        content.add(bottomPanel, BorderLayout.SOUTH);
        add(content);

        // Animate progress
        Timer timer = new Timer(30, null);
        String[] messages = {
                "Loading resources...", "Initializing 3D engine...",
                "Loading furniture catalog...", "Setting up workspace...", "Ready!"
        };
        timer.addActionListener(e -> {
            progress += 2;
            bar.setValue(progress);
            int idx = Math.min(progress / 20, messages.length - 1);
            statusLabel.setText(messages[idx]);
            if (progress >= 100) {
                timer.stop();
            }
        });
        timer.start();
    }

    /**
     * Show splash, animate for given ms, then close.
     */
    public static void showAndWait(int durationMs) {
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException ignored) {
        }
        splash.dispose();
    }
}
