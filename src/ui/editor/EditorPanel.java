package ui.editor;

import controllers.DesignController;
import models.Room;
import models.Furniture;
import utils.FileManager;
import utils.ExportManager;
import ui.MainFrame;
import utils.commands.RotateFurnitureCommand;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * EditorPanel — Full dark toolbar editor with split 2D/3D view, export and
 * measurement toggles.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding (dark toolbar layout)
 * MEMBER 5 CONTRIBUTION: Measurements & Export (export PNG/report buttons)
 * MEMBER 6 CONTRIBUTION: UX Polish (keyboard shortcuts, undo/redo buttons)
 */
public class EditorPanel extends JPanel {

    private DesignController controller;
    private Room currentRoom;

    private JPanel canvasContainer;
    private CardLayout canvasLayout;

    private Canvas2D canvas;
    private Canvas3D canvas3d;
    private boolean is3DMode = false;

    private PropertiesPanel propertiesPanel;
    private MainFrame mainFrame;

    private static final Color TOOLBAR_BG = new Color(14, 20, 45);
    private static final Color BTN_BG = new Color(28, 40, 75);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color DANGER = new Color(200, 50, 70);
    private static final Color SUCCESS = new Color(50, 180, 100);
    private static final Color TEXT = new Color(220, 230, 245);
    private static final Color MUTED = new Color(130, 150, 185);
    private static final Color SEP = new Color(40, 58, 95);

    public EditorPanel(MainFrame mainFrame, DesignController controller) {
        this.mainFrame = mainFrame;
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(10, 15, 35));
        initializeComponents();
    }

    private void initializeComponents() {
        // ── Dark Toolbar ──
        JPanel toolbar = buildToolbar();
        add(toolbar, BorderLayout.NORTH);

        // ── Sidebar (Catalog) ──
        CatalogPanel catalogPanel = new CatalogPanel(controller, this);
        add(catalogPanel, BorderLayout.WEST);

        // ── Canvas Container ──
        canvasLayout = new CardLayout();
        canvasContainer = new JPanel(canvasLayout);
        canvasContainer.setBackground(new Color(10, 15, 35));

        canvas = new Canvas2D();
        canvas3d = new Canvas3D();

        canvas.setController(controller);

        // Wire status bar
        if (mainFrame.getStatusBar() != null) {
            canvas.setStatusBar(mainFrame.getStatusBar());
        }

        canvasContainer.add(canvas, "2D");
        canvasContainer.add(canvas3d, "3D");
        add(canvasContainer, BorderLayout.CENTER);

        // ── Properties Panel ──
        propertiesPanel = new PropertiesPanel(() -> repaintCanvas());
        add(propertiesPanel, BorderLayout.EAST);

        canvas.setSelectionListener(f -> {
            propertiesPanel.setSelection(currentRoom, f);
            // Highlight in 3D
            engine3d.Renderer3D.setSelectedId(f != null ? f.getId() : null);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOOLBAR_BG);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Left group
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        left.setOpaque(false);

        left.add(toolBtn("<< Back", MUTED, e -> mainFrame.showView("DASHBOARD")));
        left.add(tbSep());
        left.add(toolBtn("Undo", BTN_BG, e -> {
            controller.undo();
            repaintCanvas();
            if (propertiesPanel != null)
                propertiesPanel.refresh();
        }));
        left.add(toolBtn("Redo", BTN_BG, e -> {
            controller.redo();
            repaintCanvas();
            if (propertiesPanel != null)
                propertiesPanel.refresh();
        }));
        left.add(tbSep());
        left.add(toolBtn("Rotate 45", BTN_BG, e -> rotateSelection()));
        left.add(toolBtn("Delete", DANGER, e -> deleteSelection()));
        left.add(tbSep());

        // Toggle 2D/3D
        JButton viewBtn = toolBtn("2D / 3D View", new Color(40, 65, 110), e -> toggleView());
        left.add(viewBtn);

        // Fit view
        left.add(toolBtn("Fit View", BTN_BG, e -> {
            canvas.fitRoom();
            repaintCanvas();
        }));

        bar.add(left, BorderLayout.WEST);

        // Center: room name or title
        JLabel roomLbl = new JLabel("\uD83D\uDED2  Room Design Studio");
        roomLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        roomLbl.setForeground(ACCENT);
        roomLbl.setHorizontalAlignment(SwingConstants.CENTER);
        bar.add(roomLbl, BorderLayout.CENTER);

        // Right group
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        right.setOpaque(false);

        // Measure toggle
        JButton measureBtn = toolBtn("[M] Measure", BTN_BG, e -> {
            canvas.setShowMeasurements(!canvas.isShowMeasurements());
        });
        right.add(measureBtn);
        right.add(tbSep());

        // Export PNG
        right.add(toolBtn("Export PNG", SUCCESS, e -> exportPNG()));
        // Export Report
        right.add(toolBtn("BOM Report", SUCCESS, e -> exportReport()));
        right.add(tbSep());

        // Save
        right.add(toolBtn("Save Design", ACCENT, e -> saveDesignToPortfolio()));

        bar.add(right, BorderLayout.EAST);

        // Bottom border line
        JPanel border = new JPanel();
        border.setBackground(SEP);
        border.setPreferredSize(new Dimension(0, 1));
        bar.add(border, BorderLayout.SOUTH);

        return bar;
    }

    private JButton toolBtn(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                g2.setColor(TEXT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.addActionListener(action);
        return btn;
    }

    private JLabel tbSep() {
        JLabel sep = new JLabel("|");
        sep.setForeground(SEP);
        sep.setBorder(new EmptyBorder(0, 2, 0, 2));
        return sep;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACTIONS
    // ─────────────────────────────────────────────────────────────────────────

    public void setRoom(Room room) {
        this.currentRoom = room;
        if (canvas != null)
            canvas.setRoom(room);
        if (canvas3d != null)
            canvas3d.setRoom(room);
        if (propertiesPanel != null)
            propertiesPanel.setSelection(room, null);
        // Update status bar room info
        if (mainFrame.getStatusBar() != null)
            mainFrame.getStatusBar().setRoomInfo(room.getWidth(), room.getDepth());
    }

    private void toggleView() {
        is3DMode = !is3DMode;
        if (is3DMode) {
            canvas3d.setRoom(currentRoom);
            canvasLayout.show(canvasContainer, "3D");
            canvas3d.requestFocusInWindow();
        } else {
            canvas.setRoom(currentRoom);
            canvasLayout.show(canvasContainer, "2D");
            canvas.requestFocusInWindow();
        }
    }

    public void repaintCanvas() {
        if (canvas != null)
            canvas.repaint();
        if (canvas3d != null)
            canvas3d.repaint();
    }

    private void rotateSelection() {
        if (canvas != null && canvas.getSelectedFurniture() != null) {
            Furniture f = canvas.getSelectedFurniture();
            double oldRot = f.getRotation();
            double newRot = (oldRot + 45) % 360;
            controller.executeCommand(new RotateFurnitureCommand(f, oldRot, newRot));
            repaintCanvas();
            if (propertiesPanel != null)
                propertiesPanel.refresh();
        }
    }

    private void deleteSelection() {
        if (canvas != null && canvas.getSelectedFurniture() != null) {
            controller.removeFurniture(canvas.getSelectedFurniture());
            canvas.clearSelection();
            repaintCanvas();
        }
    }

    private void saveDesignToPortfolio() {
        String name = JOptionPane.showInputDialog(this,
                "Enter design name:", "Save to Portfolio", JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            try {
                models.User user = utils.SessionManager.getInstance().getCurrentUser();
                if (user != null) {
                    FileManager.saveDesign(currentRoom, user.getUsername(), name.trim());
                    JOptionPane.showMessageDialog(this, "Saved to Portfolio! ✓", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No user logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPNG() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Floor Plan as PNG");
        fc.setSelectedFile(new File("floorplan.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png"))
                file = new File(file.getAbsolutePath() + ".png");
            try {
                ExportManager.exportToPNG(currentRoom, file);
                JOptionPane.showMessageDialog(this, "Exported to:\n" + file.getAbsolutePath(),
                        "PNG Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportReport() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Bill of Materials");
        fc.setSelectedFile(new File("bill_of_materials.html"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".html"))
                file = new File(file.getAbsolutePath() + ".html");
            try {
                ExportManager.exportBillOfMaterials(currentRoom, file);
                JOptionPane.showMessageDialog(this, "Report saved to:\n" + file.getAbsolutePath(),
                        "Report Exported", JOptionPane.INFORMATION_MESSAGE);
                // Try to open in browser
                try {
                    Desktop.getDesktop().browse(file.toURI());
                } catch (Exception ignored) {
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
