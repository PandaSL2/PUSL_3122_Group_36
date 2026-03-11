package ui.editor;

import controllers.DesignController;
import models.Furniture;
import models.FurnitureCatalog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * CatalogPanel — Visual furniture catalog with category tabs and search bar.
 * MEMBER 3 CONTRIBUTION: Furniture System Expansion
 */
public class CatalogPanel extends JPanel {

    private DesignController controller;
    private EditorPanel editorPanel;
    private JTextField searchField;
    private JPanel tilesPanel;
    private String currentCategory = null; // null = All

    private static final Color BG = new Color(14, 20, 45);
    private static final Color HEADER = new Color(18, 26, 58);
    private static final Color TILE_BG = new Color(25, 35, 70);
    private static final Color TILE_HOV = new Color(38, 55, 100);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color TEXT = new Color(220, 230, 245);
    private static final Color MUTED = new Color(130, 150, 180);
    private static final Color BORDER = new Color(40, 58, 95);

    public CatalogPanel(DesignController controller, EditorPanel editorPanel) {
        this.controller = controller;
        this.editorPanel = editorPanel;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setPreferredSize(new Dimension(220, 0));
        buildUI();
    }

    private void buildUI() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(12, 10, 10, 10));

        JLabel title = new JLabel("Furniture Catalog");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ACCENT);
        header.add(title, BorderLayout.NORTH);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "\uD83D\uDD0D  Search...");
        searchField.setBackground(new Color(28, 40, 72));
        searchField.setForeground(TEXT);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refresh();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refresh();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refresh();
            }
        });
        header.add(searchField, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── Category Tabs ──
        JPanel catPanel = new JPanel(new GridLayout(0, 1, 0, 1));
        catPanel.setBackground(new Color(12, 17, 40));
        catPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        String[] cats = FurnitureCatalog.getInstance().getCategories().toArray(new String[0]);
        String[] icons = { "\uD83D\uDECB", "\uD83D\uDECC", "\uD83C\uDF73", "\uD83D\uDEC1", "\uD83D\uDCBB",
                "\uD83C\uDF3F" };
        // "All" tab first
        catPanel.add(catTab("All", "\uD83D\uDDC2"));
        for (int i = 0; i < cats.length; i++) {
            catPanel.add(catTab(cats[i], icons[Math.min(i, icons.length - 1)]));
        }

        JScrollPane catScroll = new JScrollPane(catPanel);
        catScroll.setBorder(null);
        catScroll.setPreferredSize(new Dimension(220, 145));
        catScroll.getVerticalScrollBar().setUnitIncrement(8);
        catScroll.setBackground(new Color(12, 17, 40));
        add(catScroll, BorderLayout.CENTER);

        // ── Tiles area with anchoring to top ──
        JPanel tilesWrapper = new JPanel(new GridBagLayout());
        tilesWrapper.setBackground(BG);
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.gridx = 0;
        tgbc.gridy = 0;
        tgbc.weightx = 1.0;
        tgbc.weighty = 0.0;
        tgbc.fill = GridBagConstraints.HORIZONTAL;
        tgbc.anchor = GridBagConstraints.NORTH;

        tilesPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        tilesPanel.setBackground(BG);
        tilesPanel.setBorder(new EmptyBorder(6, 8, 8, 8));
        tilesWrapper.add(tilesPanel, tgbc);

        // Glue to push items to the top
        tgbc.gridy = 1;
        tgbc.weighty = 1.0;
        tilesWrapper.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 9999)), tgbc);

        JScrollPane tileScroll = new JScrollPane(tilesWrapper);
        tileScroll.setBorder(null);
        tileScroll.setBackground(BG);
        tileScroll.getViewport().setBackground(BG);
        tileScroll.getVerticalScrollBar().setUnitIncrement(12);
        add(tileScroll, BorderLayout.SOUTH);
        tileScroll.setPreferredSize(new Dimension(220, 400)); // Fixed height for tiles scroll area

        refresh();
    }

    private JButton catTab(String category, String icon) {
        JButton btn = new JButton(icon + "  " + category) {
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
                boolean sel = (category.equals("All") && currentCategory == null)
                        || category.equals(currentCategory);
                if (sel) {
                    g2.setColor(new Color(35, 55, 100));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(ACCENT);
                    g2.fillRect(0, 0, 3, getHeight());
                } else {
                    g2.setColor(ov ? new Color(22, 32, 65) : new Color(12, 17, 40));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btn.setForeground(TEXT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            currentCategory = "All".equals(category) ? null : category;
            searchField.setText("");
            refresh();
        });
        return btn;
    }

    private void refresh() {
        tilesPanel.removeAll();
        String query = searchField.getText().trim();
        List<FurnitureCatalog.CatalogEntry> items;
        if (!query.isEmpty()) {
            items = FurnitureCatalog.getInstance().search(query);
        } else if (currentCategory != null) {
            items = FurnitureCatalog.getInstance().getByCategory(currentCategory);
        } else {
            items = FurnitureCatalog.getInstance().getAll();
        }

        for (FurnitureCatalog.CatalogEntry entry : items) {
            tilesPanel.add(buildTile(entry));
        }

        if (items.isEmpty()) {
            JLabel empty = new JLabel("No results");
            empty.setForeground(MUTED);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            tilesPanel.add(empty);
        }

        tilesPanel.revalidate();
        tilesPanel.repaint();
    }

    private JPanel buildTile(FurnitureCatalog.CatalogEntry entry) {
        JPanel tile = new JPanel(new BorderLayout(8, 0)) {
            private boolean ov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        ov = true;
                        repaint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }

                    public void mouseExited(MouseEvent e) {
                        ov = false;
                        repaint();
                        setCursor(Cursor.getDefaultCursor());
                    }

                    public void mouseClicked(MouseEvent e) {
                        Furniture f = entry.create();
                        // Place near center of room
                        f.setPosition(50, 50);
                        controller.addFurniture(f);
                        editorPanel.repaintCanvas();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ov ? TILE_HOV : TILE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ov ? new Color(99, 179, 237, 80) : BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        tile.setOpaque(false);
        tile.setBorder(new EmptyBorder(7, 8, 7, 8));
        tile.setMaximumSize(new Dimension(99999, 52));

        // Schematic icon swatch
        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                utils.SchematicPainter.paintIcon(g2, entry.type(), 0, 0, getWidth(), getHeight(), entry.color(), false);
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(32, 32));

        // Text
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        textPanel.setOpaque(false);

        JLabel name = new JLabel(entry.name());
        name.setFont(new Font("Segoe UI", Font.BOLD, 11));
        name.setForeground(TEXT);

        JLabel dim = new JLabel(String.format("%.0f×%.0f cm  |  %s",
                entry.w(), entry.d(),
                entry.material().name().charAt(0) + entry.material().name().toLowerCase().substring(1)));
        dim.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dim.setForeground(MUTED);

        textPanel.add(name);
        textPanel.add(dim);

        // Wrap swatch to prevent vertical stretching
        JPanel swatchWrapper = new JPanel(new GridBagLayout());
        swatchWrapper.setOpaque(false);
        swatchWrapper.add(swatch);

        tile.add(swatchWrapper, BorderLayout.WEST);
        tile.add(textPanel, BorderLayout.CENTER);

        // Click hint
        JLabel plus = new JLabel("+");
        plus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        plus.setForeground(new Color(99, 179, 237, 100));
        tile.add(plus, BorderLayout.EAST);

        return tile;
    }
}
