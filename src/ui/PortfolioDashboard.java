package ui;

import models.User;
import utils.FileManager;
import utils.SessionManager;
import ui.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * PortfolioDashboard — Dark themed, card-based portfolio view with hover
 * effects.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding
 */
public class PortfolioDashboard extends JPanel {
    private MainFrame mainFrame;
    private JPanel designsPanel;
    private JTextField searchField;

    private static final Color BG_MAIN = new Color(10, 15, 35);
    private static final Color BG_HEADER = new Color(14, 20, 45);
    private static final Color CARD_BG = new Color(22, 30, 55);
    private static final Color CARD_HOVER = new Color(32, 44, 78);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(236, 72, 153);
    private static final Color TEXT_PRI = new Color(237, 242, 247);
    private static final Color TEXT_MUT = new Color(160, 174, 192);
    private static final Color BORDER_C = new Color(40, 55, 90);

    private java.util.List<String> allDesigns = new java.util.ArrayList<>();

    public PortfolioDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        initUI();
    }

    private void initUI() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(99, 179, 237, 40));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(22, 36, 22, 36));

        // Left: logo + greeting
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("My Portfolio") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                g2.setPaint(gp);
                g2.setFont(getFont());
                g2.drawString(getText(), 0, getFontMetrics(getFont()).getAscent());
            }
        };
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setPreferredSize(new Dimension(300, 32));

        User user = SessionManager.getInstance().getCurrentUser();
        JLabel welcomeLbl = new JLabel(
                "Welcome back, " + (user != null ? user.getUsername() : "Designer") + " \uD83D\uDC4B");
        welcomeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeLbl.setForeground(TEXT_MUT);

        titleBlock.add(titleLbl);
        titleBlock.add(welcomeLbl);
        header.add(titleBlock, BorderLayout.WEST);

        // Center: search bar
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        searchField.putClientProperty("JTextField.placeholderText", "Search designs...");
        searchField.setBackground(new Color(30, 42, 75));
        searchField.setForeground(TEXT_PRI);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1),
                new EmptyBorder(8, 14, 8, 14)));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(240, 38));
        searchField.setOpaque(false);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterDesigns();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterDesigns();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterDesigns();
            }
        });

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        searchWrap.setOpaque(false);
        searchWrap.add(searchField);
        header.add(searchWrap, BorderLayout.CENTER);

        // Right: New + Logout
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        ModernButton newBtn = new ModernButton("+ New Design");
        newBtn.setBackground(new Color(99, 179, 237));
        newBtn.addActionListener(e -> mainFrame.showView("ROOM_SETUP"));
        rightButtons.add(newBtn);

        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setBackground(new Color(200, 50, 80));
        logoutBtn.addActionListener(e -> {
            SessionManager.getInstance().logout();
            mainFrame.showView("LOGIN");
        });
        rightButtons.add(logoutBtn);
        header.add(rightButtons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Designs Grid ──
        designsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        designsPanel.setBackground(BG_MAIN);
        designsPanel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JScrollPane scroll = new JScrollPane(designsPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG_MAIN);
        scroll.getViewport().setBackground(BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        // Style scrollbar
        scroll.getVerticalScrollBar().setBackground(BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        // ── Status Bar ──
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        statusBar.setBackground(BG_HEADER);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        JLabel statusLbl = new JLabel("RoomCraft Designer 2.0  |  PUSL3122 Group Project");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(TEXT_MUT);
        statusBar.add(statusLbl);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void refresh() {
        designsPanel.removeAll();
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null)
            return;

        allDesigns = FileManager.getUserDesigns(user.getUsername());
        renderDesignCards(allDesigns);
    }

    private void filterDesigns() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            renderDesignCards(allDesigns);
        } else {
            java.util.List<String> filtered = new java.util.ArrayList<>();
            for (String d : allDesigns) {
                if (d.toLowerCase().contains(query))
                    filtered.add(d);
            }
            renderDesignCards(filtered);
        }
    }

    private void renderDesignCards(java.util.List<String> designs) {
        designsPanel.removeAll();

        if (designs.isEmpty()) {
            JLabel empty = new JLabel("No designs yet. Create your first one!");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setForeground(TEXT_MUT);
            designsPanel.add(empty);
        }

        for (String name : designs) {
            designsPanel.add(createDesignCard(name));
        }

        designsPanel.revalidate();
        designsPanel.repaint();
    }

    private JPanel createDesignCard(String designName) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }

                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                        setCursor(Cursor.getDefaultCursor());
                    }

                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2)
                            loadDesign(designName);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? CARD_HOVER : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(hover ? ACCENT : new Color(50, 70, 110, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                if (hover) {
                    g2.setColor(new Color(99, 179, 237, 30));
                    g2.setStroke(new BasicStroke(3.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(210, 185));
        card.setBorder(new EmptyBorder(16, 16, 14, 16));

        // Thumbnail
        JPanel thumb = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 22, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Mini room preview
                g2.setColor(new Color(200, 180, 140, 80));
                g2.fillRect(15, 20, getWidth() - 30, getHeight() - 40);
                g2.setColor(new Color(99, 179, 237, 60));
                g2.fillRect(22, 32, 25, 18);
                g2.setColor(new Color(236, 72, 153, 60));
                g2.fillRect(58, 45, 20, 14);
            }
        };
        thumb.setPreferredSize(new Dimension(178, 90));
        thumb.setOpaque(false);

        JLabel iconLbl = new JLabel("\uD83C\uDFE0");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        thumb.add(iconLbl);
        card.add(thumb, BorderLayout.CENTER);

        // Name
        JLabel nameLbl = new JLabel(designName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_PRI);
        nameLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(nameLbl, BorderLayout.NORTH);

        // Buttons row
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);

        ModernButton openBtn = new ModernButton("Open");
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        openBtn.setBackground(new Color(99, 179, 237));
        openBtn.addActionListener(e -> loadDesign(designName));

        ModernButton delBtn = new ModernButton("Delete");
        delBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        delBtn.setBackground(new Color(180, 40, 60));
        delBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete design \"" + designName + "\"?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                FileManager.deleteDesign(
                        SessionManager.getInstance().getCurrentUser().getUsername(), designName);
                refresh();
            }
        });

        btns.add(openBtn);
        btns.add(delBtn);
        card.add(btns, BorderLayout.SOUTH);

        return card;
    }

    private void loadDesign(String designName) {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            models.Room room = FileManager.loadDesign(user.getUsername(), designName);
            mainFrame.openEditor(room);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load design: " + ex.getMessage());
        }
    }

    // Simple wrap layout helper
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right;
                int height = 0, rowW = 0, rowH = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowW + d.width > maxWidth) {
                            height += rowH + vgap;
                            rowW = 0;
                            rowH = 0;
                        }
                        if (rowW != 0)
                            rowW += hgap;
                        rowW += d.width;
                        rowH = Math.max(rowH, d.height);
                    }
                }
                height += rowH;
                height += insets.top + insets.bottom + vgap * 2;
                return new Dimension(maxWidth, height);
            }
        }
    }
}
