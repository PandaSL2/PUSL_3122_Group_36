package ui;

import models.RoomTemplate;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

/**
 * RoomSetupPanel — Create new room with templates, color pickers and preview.
 * MEMBER 4 CONTRIBUTION: Room Templates & Multi-Room Support
 */
public class RoomSetupPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField widthField, depthField, heightField;
    private Color wallColor = new Color(230, 230, 240);
    private Color floorColor = new Color(200, 180, 150);
    private Color ceilingColor = new Color(250, 250, 255);
    private String selectedType = "CUSTOM";
    private RoomPreviewPanel preview;

    private static final Color BG1 = new Color(10, 15, 35);
    private static final Color BG2 = new Color(18, 26, 55);
    private static final Color CARD = new Color(22, 30, 55);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color ACCENT2 = new Color(236, 72, 153);
    private static final Color TEXT = new Color(237, 242, 247);
    private static final Color MUTED = new Color(160, 174, 192);

    public RoomSetupPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setOpaque(true);
        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, BG1, getWidth(), getHeight(), BG2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void buildUI() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 40, 18, 40));

        JLabel title = gradientLabel("Create New Room", new Font("Segoe UI", Font.BOLD, 28));
        JLabel sub = new JLabel("Configure your room or choose a ready-made template");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);

        JButton backBtn = smallBtn("<< Back");
        backBtn.addActionListener(e -> mainFrame.showView("DASHBOARD"));

        JPanel titleCol = new JPanel(new GridLayout(2, 1, 0, 4));
        titleCol.setOpaque(false);
        titleCol.add(title);
        titleCol.add(sub);

        header.add(titleCol, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Main split: left (form) | right (preview) ──
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 30, 20, 30);

        // Left: form card
        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            }
        };
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(32, 32, 32, 32));

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(6, 0, 6, 0);
        fc.gridx = 0;
        fc.weightx = 1.0;

        // Templates section
        fc.gridy = 0;
        formCard.add(sectionLabel("Room Templates"), fc);

        fc.gridy = 1;
        formCard.add(buildTemplateRow(), fc);

        // Dimensions
        fc.gridy = 2;
        fc.insets = new Insets(18, 0, 6, 0);
        formCard.add(sectionLabel("Dimensions (cm)"), fc);

        fc.gridy = 3;
        fc.insets = new Insets(4, 0, 4, 0);
        formCard.add(buildDimensionRow(), fc);

        // Colors
        fc.gridy = 4;
        fc.insets = new Insets(18, 0, 6, 0);
        formCard.add(sectionLabel("Colors"), fc);

        fc.gridy = 5;
        fc.insets = new Insets(4, 0, 4, 0);
        formCard.add(buildColorRow(), fc);

        // Create button
        fc.gridy = 6;
        fc.insets = new Insets(24, 0, 0, 0);
        JButton createBtn = createGradientBtn("Start Designing →");
        createBtn.addActionListener(e -> createRoom());
        formCard.add(createBtn, fc);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.55;
        gc.weighty = 1.0;
        main.add(formCard, gc);

        // Right: preview
        preview = new RoomPreviewPanel();
        gc.gridx = 1;
        gc.weightx = 0.45;
        main.add(preview, gc);

        add(main, BorderLayout.CENTER);
        updatePreview();
    }

    private JPanel buildTemplateRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 8, 0));
        row.setOpaque(false);

        List<RoomTemplate> templates = RoomTemplate.getAllTemplates();
        String[] icons = { "\uD83D\uDECB", "\uD83D\uDECC", "\uD83D\uDCBB", "\uD83C\uDF73", "\uD83D\uDEC1" };
        for (int i = 0; i < templates.size(); i++) {
            RoomTemplate t = templates.get(i);
            final String icon = icons[Math.min(i, icons.length - 1)];
            JButton btn = new JButton("<html><center>" + icon + "<br><font size=2>" + t.displayName.replace(" ", "<br>")
                    + "</font></center></html>") {
                private boolean ov = false;
                {
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            ov = true;
                            repaint();
                        }

                        public void mouseExited(MouseEvent e) {
                            ov = false;
                            repaint();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = selectedType.equals(t.name);
                    if (sel) {
                        g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT2));
                    } else {
                        g2.setColor(ov ? new Color(38, 55, 95) : new Color(30, 42, 75));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    if (sel) {
                        g2.setColor(new Color(255, 255, 255, 80));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    }
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setForeground(TEXT);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(0, 75));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selectedType = t.name;
                widthField.setText(String.valueOf((int) t.width));
                depthField.setText(String.valueOf((int) t.depth));
                heightField.setText(String.valueOf((int) t.height));
                wallColor = t.wallColor;
                floorColor = t.floorColor;
                ceilingColor = t.ceilingColor;
                updatePreview();
                row.repaint();
            });
            row.add(btn);
        }
        return row;
    }

    private JPanel buildDimensionRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        widthField = styledField("Width", "500");
        depthField = styledField("Depth", "400");
        heightField = styledField("Height", "250");
        for (JTextField f : new JTextField[] { widthField, depthField, heightField }) {
            f.getDocument().addDocumentListener(simpleListener(() -> updatePreview()));
            row.add(f);
        }
        return row;
    }

    private JPanel buildColorRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        row.add(colorBtn("Floor", floorColor, c -> {
            floorColor = c;
            updatePreview();
        }));
        row.add(colorBtn("Wall", wallColor, c -> {
            wallColor = c;
            updatePreview();
        }));
        row.add(colorBtn("Ceiling", ceilingColor, c -> {
            ceilingColor = c;
            updatePreview();
        }));
        return row;
    }

    interface ColorConsumer {
        void accept(Color c);
    }

    private JButton colorBtn(String label, Color initial, ColorConsumer callback) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Color swatch
                int sw = 20;
                g2.setColor(callback == null ? Color.WHITE : getBackground().brighter());
                g2.fillRoundRect(8, (getHeight() - 14) / 2, 14, 14, 4, 4);
                g2.setColor(TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), sw + 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setBackground(new Color(30, 44, 75));
        btn.setForeground(TEXT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        btn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose " + label + " Color", initial);
            if (chosen != null) {
                btn.setBackground(chosen.darker().darker());
                callback.accept(chosen);
            }
        });
        return btn;
    }

    private void createRoom() {
        try {
            double w = Double.parseDouble(widthField.getText().trim());
            double d = Double.parseDouble(depthField.getText().trim());
            double h = Double.parseDouble(heightField.getText().trim());
            if (w < 100 || d < 100 || h < 200) {
                JOptionPane.showMessageDialog(this, "Minimum dimensions: 100 × 100 × 200 cm", "Too Small",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Check if a template is selected — use template room
            for (RoomTemplate t : RoomTemplate.getAllTemplates()) {
                if (t.name.equals(selectedType)) {
                    models.Room room = t.createRoom();
                    room.setWidth(w);
                    room.setDepth(d);
                    room.setHeight(h);
                    room.setWallColor(wallColor);
                    room.setFloorColor(floorColor);
                    room.setCeilingColor(ceilingColor);
                    mainFrame.getDesignController().setCurrentRoom(room);
                    mainFrame.openEditor(room);
                    return;
                }
            }
            mainFrame.startDesignSession(w, d, h, wallColor, floorColor, ceilingColor, selectedType);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePreview() {
        if (preview == null)
            return;
        try {
            double w = Double.parseDouble(widthField.getText().trim());
            double d = Double.parseDouble(depthField.getText().trim());
            preview.update(w, d, floorColor, wallColor);
        } catch (Exception ignored) {
        }
    }

    // ── Helpers ──

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MUTED);
        return lbl;
    }

    private JTextField styledField(String hint, String def) {
        JTextField f = new JTextField(def);
        f.setBackground(new Color(28, 40, 70));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(50, 70, 110), 1),
                new EmptyBorder(8, 10, 8, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setToolTipText(hint + " (cm)");
        return f;
    }

    private JButton smallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(30, 44, 75));
        btn.setForeground(MUTED);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createGradientBtn(String text) {
        JButton btn = new JButton(text) {
            private boolean ov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        ov = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        ov = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = ov ? new Color(120, 195, 255) : ACCENT;
                Color c2 = ov ? new Color(255, 110, 190) : ACCENT2;
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(0, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel gradientLabel(String text, Font font) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
            }
        };
        lbl.setFont(font);
        lbl.setPreferredSize(new Dimension(400, font.getSize() + 8));
        return lbl;
    }

    private javax.swing.event.DocumentListener simpleListener(Runnable r) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                r.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                r.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                r.run();
            }
        };
    }

    // ── Room Preview Panel ──
    static class RoomPreviewPanel extends JPanel {
        private double w = 500, d = 400;
        private Color floor = new Color(200, 180, 150);
        private Color wall = new Color(230, 230, 240);

        RoomPreviewPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 300));
            setBorder(new EmptyBorder(24, 24, 24, 24));
        }

        void update(double w, double d, Color floor, Color wall) {
            this.w = w;
            this.d = d;
            this.floor = floor;
            this.wall = wall;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background card
            g2.setColor(new Color(22, 30, 55));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(new Color(40, 60, 100, 80));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            int pad = 40;
            int maxW = getWidth() - pad * 2;
            int maxH = getHeight() - pad * 2 - 30;
            double ratio = maxW <= 0 || maxH <= 0 ? 1 : Math.min(maxW / Math.max(w, 1), maxH / Math.max(d, 1));
            int pw = (int) (w * ratio);
            int ph = (int) (d * ratio);
            int rx = (getWidth() - pw) / 2;
            int ry = (getHeight() - ph - 30) / 2 + 10;

            // Floor
            g2.setColor(floor);
            g2.fillRect(rx, ry, pw, ph);
            // Walls (top + left border)
            g2.setColor(wall);
            g2.fillRect(rx, ry, pw, 8);
            g2.fillRect(rx, ry, 8, ph);
            // Outline
            g2.setColor(new Color(99, 179, 237, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(rx, ry, pw, ph);

            // Dimension labels
            g2.setColor(new Color(160, 174, 192));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.format("%.0f cm", w), rx + pw / 2 - 20, ry + ph + 16);
            AffineTransform at = g2.getTransform();
            g2.rotate(-Math.PI / 2, rx - 12, ry + ph / 2);
            g2.drawString(String.format("%.0f cm", d), rx - 12, ry + ph / 2);
            g2.setTransform(at);

            // Area
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String area = String.format("Area: %.1f m²", w * d / 10000.0);
            g2.setColor(new Color(99, 179, 237));
            g2.drawString(area, (getWidth() - g2.getFontMetrics().stringWidth(area)) / 2,
                    getHeight() - 10);
        }
    }
}
