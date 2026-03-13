package ui.editor;

import models.Room;
import models.Furniture;
import utils.CollisionDetector;
import utils.SnapHelper;
import utils.commands.MoveFurnitureCommand;
import controllers.DesignController;
import ui.components.StatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Canvas2D used for the floor plan editor
 * Handles snapping, collision checking, measurements, zooming, panning, and other editor features
 */
public class Canvas2D extends JPanel {

    private Room room;
    private double scale = 1.0;
    private double offsetX = 60;
    private double offsetY = 60;
    private final int GRID_SIZE = 50;

    // Select one item
    private Furniture selectedFurniture;
    private DesignController controller;
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double initialFurnitureX, initialFurnitureY;

    // Check snapping and collisions
    private SnapHelper snapHelper = new SnapHelper();
    private List<SnapHelper.GuideLine> guideLines = new ArrayList<>();
    private boolean hasCollision = false;

    // Rubber band selection
    private Point rubberStart;
    private Rectangle rubberRect;
    private List<Furniture> multiSelected = new ArrayList<>();

    // Zoom and pan
    private boolean spaceHeld = false;
    private int panStartX, panStartY;
    private double panOffsetXStart, panOffsetYStart;
    private boolean isPanning = false;

    // Copy buffer
    private Furniture clipboard;

    // Show or hide measurements
    private boolean showMeasurements = true;

    // Status bar reference
    private StatusBar statusBar;

    public interface SelectionListener {
        void onSelectionChanged(Furniture f);
    }

    private SelectionListener selectionListener;

    // Constructor

    public Canvas2D() {
        setBackground(new Color(18, 26, 55));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setupMouseHandlers();
        setupKeyboardHandlers();
    }

    // Setup 

    private void setupMouseHandlers() {
        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

                // Hold Space and drag to move the view
                if (spaceHeld) {
                    isPanning = true;
                    panStartX = e.getX();
                    panStartY = e.getY();
                    panOffsetXStart = offsetX;
                    panOffsetYStart = offsetY;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    return;
                }

                if (room == null)
                    return;
                double roomX = toRoomX(e.getX());
                double roomY = toRoomY(e.getY());

                // Check clicked object from top
                List<Furniture> list = room.getFurnitureList();
                for (int i = list.size() - 1; i >= 0; i--) {
                    Furniture f = list.get(i);
                    if (hitTest(f, roomX, roomY)) {
                        selectedFurniture = f;
                        multiSelected.clear();
                        rubberRect = null;
                        dragStartX = roomX;
                        dragStartY = roomY;
                        initialFurnitureX = f.getX();
                        initialFurnitureY = f.getY();
                        isDragging = true;
                        if (selectionListener != null)
                            selectionListener.onSelectionChanged(f);
                        if (statusBar != null)
                            statusBar.setSelectedItem(f.getName());
                        guideLines.clear();
                        hasCollision = false;
                        repaint();
                        return;
                    }
                }

                // No item clicked, start rubber-band selection
                selectedFurniture = null;
                multiSelected.clear();
                rubberStart = e.getPoint();
                rubberRect = null;
                if (selectionListener != null)
                    selectionListener.onSelectionChanged(null);
                if (statusBar != null)
                    statusBar.setSelectedItem(null);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Pan
                if (isPanning) {
                    offsetX = panOffsetXStart + (e.getX() - panStartX);
                    offsetY = panOffsetYStart + (e.getY() - panStartY);
                    repaint();
                    return;
                }

                // Rubber band
                if (rubberStart != null && selectedFurniture == null) {
                    int x = Math.min(rubberStart.x, e.getX());
                    int y = Math.min(rubberStart.y, e.getY());
                    int w = Math.abs(e.getX() - rubberStart.x);
                    int h = Math.abs(e.getY() - rubberStart.y);
                    rubberRect = new Rectangle(x, y, w, h);
                    // Select all items inside the selected area
                    if (room != null) {
                        multiSelected.clear();
                        for (Furniture f : room.getFurnitureList()) {
                            int sx = toScreenX(f.getX()), sy = toScreenY(f.getY());
                            int sw = (int) (f.getWidth() * scale), sh = (int) (f.getDepth() * scale);
                            if (rubberRect.intersects(sx, sy, Math.max(1, sw), Math.max(1, sh)))
                                multiSelected.add(f);
                        }
                    }
                    repaint();
                    return;
                }

                // Drag furniture to move it
                if (isDragging && selectedFurniture != null && room != null) {
                    double roomX = toRoomX(e.getX());
                    double roomY = toRoomY(e.getY());
                    double dx = roomX - dragStartX;
                    double dy = roomY - dragStartY;
                    double newX = initialFurnitureX + dx;
                    double newY = initialFurnitureY + dy;

                    // Align item to the 10 cm grid
                    newX = Math.round(newX / 10.0) * 10;
                    newY = Math.round(newY / 10.0) * 10;

                    selectedFurniture.setX(newX);
                    selectedFurniture.setY(newY);

                    // Check snapping with walls and furniture
                    guideLines = snapHelper.snap(selectedFurniture, room);

                    // Check item collision
                    hasCollision = CollisionDetector.hasCollision(selectedFurniture, room);

                    // Show cursor position on status bar
                    if (statusBar != null)
                        statusBar.setCursorPos(selectedFurniture.getX(), selectedFurniture.getY());

                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (statusBar != null && room != null) {
                    statusBar.setCursorPos(toRoomX(e.getX()), toRoomY(e.getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                isPanning = false;

                if (isDragging && selectedFurniture != null) {
                    isDragging = false;
                    if (hasCollision) {
                        // Reset position if collision occurs
                        selectedFurniture.setPosition(initialFurnitureX, initialFurnitureY);
                        JOptionPane.showMessageDialog(Canvas2D.this,
                                "Furniture overlaps another item — position reverted.",
                                "Collision Detected", JOptionPane.WARNING_MESSAGE);
                    } else if (selectedFurniture.getX() != initialFurnitureX
                            || selectedFurniture.getY() != initialFurnitureY) {
                        double finalX = selectedFurniture.getX();
                        double finalY = selectedFurniture.getY();
                        selectedFurniture.setPosition(initialFurnitureX, initialFurnitureY);
                        if (controller != null) {
                            controller.executeCommand(new MoveFurnitureCommand(
                                    selectedFurniture, initialFurnitureX, initialFurnitureY, finalX, finalY));
                        }
                        selectedFurniture.setPosition(finalX, finalY);
                    }
                    guideLines.clear();
                    hasCollision = false;
                }

                rubberStart = null;
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Zoom toward the cursor
                double oldScale = scale;
                double factor = e.getWheelRotation() < 0 ? 1.12 : 0.89;
                scale = Math.max(0.1, Math.min(10.0, scale * factor));

                // Adjust offset to keep cursor in same place
                double mouseRX = (e.getX() - offsetX) / oldScale;
                double mouseRY = (e.getY() - offsetY) / oldScale;
                offsetX = e.getX() - mouseRX * scale;
                offsetY = e.getY() - mouseRY * scale;

                if (statusBar != null)
                    statusBar.setZoom((int) (scale * 100));
                repaint();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    private void setupKeyboardHandlers() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(' '), "spaceDown");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "spaceUp");
        getActionMap().put("spaceDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                spaceHeld = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        });
        getActionMap().put("spaceUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                spaceHeld = false;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // Ctrl+C to copy
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
        getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (selectedFurniture != null) {
                    clipboard = selectedFurniture.copy();
                }
            }
        });

        // Ctrl+V to paste
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "paste");
        getActionMap().put("paste", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (clipboard != null && room != null && controller != null) {
                    Furniture pasted = clipboard.copy();
                    pasted.setPosition(clipboard.getX() + 20, clipboard.getY() + 20);
                    controller.addFurniture(pasted);
                    selectedFurniture = pasted;
                    repaint();
                }
            }
        });

        // Delete item (Delete / Backspace)
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        getActionMap().put("delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (selectedFurniture != null && controller != null) {
                    controller.removeFurniture(selectedFurniture);
                    selectedFurniture = null;
                    if (selectionListener != null)
                        selectionListener.onSelectionChanged(null);
                    repaint();
                }
            }
        });

        // Undo / redo actions
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        getActionMap().put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    controller.undo();
                    repaint();
                }
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        getActionMap().put("redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    controller.redo();
                    repaint();
                }
            }
        });

        // Clear selection with Escape key
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getActionMap().put("escape", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedFurniture = null;
                multiSelected.clear();
                if (selectionListener != null)
                    selectionListener.onSelectionChanged(null);
                repaint();
            }
        });

        // Press M to show or hide measurements
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "toggleMeasure");
        getActionMap().put("toggleMeasure", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showMeasurements = !showMeasurements;
                repaint();
            }
        });
    }

    // Painting 

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (room == null) {
            drawEmpty(g);
            return;
        }

        // Blueprint style background
        g.setColor(new Color(10, 15, 35));
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fit view automatically when first loaded
        if (scale == 1.0)
            fitRoom();

        AffineTransform saved = g2.getTransform();
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        // Draw floor and shadow
        Color fc = room.getFloorColor();
        g2.setColor(new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), 180));
        g2.fillRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // Draw grid
        g2.setColor(new Color(0, 0, 0, 22));
        g2.setStroke(new BasicStroke(0.5f));
        for (double x = 0; x <= room.getWidth(); x += GRID_SIZE)
            g2.draw(new Line2D.Double(x, 0, x, room.getDepth()));
        for (double y = 0; y <= room.getDepth(); y += GRID_SIZE)
            g2.draw(new Line2D.Double(0, y, room.getWidth(), y));

        // Room walls
        g2.setColor(new Color(30, 45, 80));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // Snap guide lines
        for (SnapHelper.GuideLine gl : guideLines) {
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 5, 4 }, 0));
            g2.setColor(gl.color());
            g2.draw(new Line2D.Double(gl.x1(), gl.y1(), gl.x2(), gl.y2()));
        }

        // Furniture
        for (Furniture f : room.getFurnitureList()) {
            drawFurniture(g2, f);
        }

        // Selected items highlight
        for (Furniture f : multiSelected) {
            drawSelectionHighlight(g2, f, new Color(99, 179, 237, 80));
        }

        // Rubber band selection
        if (rubberRect != null) {
            g2.setTransform(saved);
            g2.setColor(new Color(99, 179, 237, 30));
            g2.fill(rubberRect);
            g2.setColor(new Color(99, 179, 237, 180));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 4, 3 }, 0));
            g2.draw(rubberRect);
            g2.setTransform(saved);
            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);
        }

        // Measurements
        if (showMeasurements)
            drawMeasurements(g2);

        // Show room information at bottom
        drawRoomInfo(g2);

        g2.setTransform(saved);

        // Draw compass on screen
        drawCompassRose(g2, getWidth() - 60, 60, 40);

        // Show hint bar
        drawHintBar(g2);
    }

    private void drawEmpty(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(18, 26, 55));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(99, 179, 237, 120));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        String msg = "No room loaded";
        g2.drawString(msg, (getWidth() - g2.getFontMetrics().stringWidth(msg)) / 2, getHeight() / 2);
    }

    private void drawFurniture(Graphics2D g2, Furniture f) {
        AffineTransform old = g2.getTransform();
        double cx = f.getX() + f.getWidth() / 2.0;
        double cy = f.getY() + f.getDepth() / 2.0;
        g2.rotate(Math.toRadians(f.getRotation()), cx, cy);

        boolean isSelected = f == selectedFurniture;
        boolean collision = isSelected && hasCollision;
        boolean inMulti = multiSelected.contains(f);

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fill(new Rectangle2D.Double(f.getX() + 3, f.getY() + 3, f.getWidth(), f.getDepth()));

        // Draw furniture icon
        Color fill = collision ? new Color(255, 80, 80, 180) : f.getColor();
        utils.SchematicPainter.paintIcon(g2, f.getType(), (int) f.getX(), (int) f.getY(), (int) f.getWidth(),
                (int) f.getDepth(),
                fill, isSelected || inMulti);

        // Draw label
        if (f.getWidth() * scale > 30 && f.getDepth() * scale > 18) {
            int fontSize = (int) Math.max(6, Math.min(12, f.getWidth() / 18));
            g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            String label = f.getName();
            g2.setColor(contrastColor(fill));
            float lx = (float) (f.getX() + (f.getWidth() - fm.stringWidth(label)) / 2.0);
            float ly = (float) (f.getY() + f.getDepth() / 2.0 + fm.getAscent() / 2.0 - fm.getDescent());
            g2.drawString(label, lx, ly);
        }

        // Show front direction arrow
        if (f.getWidth() * scale > 40) {
            double ax = f.getX() + f.getWidth() / 2.0;
            double frontY = f.getY() + f.getDepth();
            int[] xs = { (int) (ax - 4), (int) (ax + 4), (int) ax };
            int[] ys = { (int) (frontY - 9), (int) (frontY - 9), (int) frontY };
            g2.setColor(new Color(255, 255, 255, 160));
            g2.fillPolygon(xs, ys, 3);
        }

        // If selected, show dimension labels
        if (isSelected && showMeasurements) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) Math.max(7, 10 / scale)));
            g2.setColor(new Color(236, 220, 60));
            g2.setStroke(new BasicStroke(0.8f));
            // Show width label above the item
            String wLabel = String.format("%.0fcm", f.getWidth());
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(wLabel,
                    (float) (f.getX() + (f.getWidth() - fm2.stringWidth(wLabel)) / 2f),
                    (float) (f.getY() - 4));
            // Depth label (right side)
            String dLabel = String.format("%.0fcm", f.getDepth());
            AffineTransform rotLabel = g2.getTransform();
            g2.rotate(-Math.PI / 2, f.getX() + f.getWidth() + 10, f.getY() + f.getDepth() / 2);
            g2.drawString(dLabel,
                    (float) (f.getX() + f.getWidth() + 10 - fm2.stringWidth(dLabel) / 2f),
                    (float) (f.getY() + f.getDepth() / 2 + fm2.getAscent() / 2f));
            g2.setTransform(rotLabel);
        }

        g2.setTransform(old);
    }

    private void drawSelectionHighlight(Graphics2D g2, Furniture f, Color c) {
        AffineTransform old = g2.getTransform();
        g2.rotate(Math.toRadians(f.getRotation()),
                f.getX() + f.getWidth() / 2, f.getY() + f.getDepth() / 2);
        g2.setColor(c);
        g2.fill(new Rectangle2D.Double(f.getX(), f.getY(), f.getWidth(), f.getDepth()));
        g2.setTransform(old);
    }

    private void drawMeasurements(Graphics2D g2) {
        if (room == null)
            return;
        double w = room.getWidth(), d = room.getDepth();
        g2.setColor(new Color(99, 179, 237, 200));
        g2.setStroke(new BasicStroke(1.0f));
        g2.setFont(new Font("Segoe UI", Font.BOLD, (int) Math.max(9, 13 / scale)));

        // Width dimension line above room
        double arrowY = -28;
        g2.draw(new Line2D.Double(0, arrowY, w, arrowY));
        // Arrow heads
        g2.draw(new Line2D.Double(0, arrowY, 7, arrowY - 4));
        g2.draw(new Line2D.Double(0, arrowY, 7, arrowY + 4));
        g2.draw(new Line2D.Double(w, arrowY, w - 7, arrowY - 4));
        g2.draw(new Line2D.Double(w, arrowY, w - 7, arrowY + 4));
        // Tick marks for each metre
        for (double x = 100; x < w; x += 100) {
            g2.draw(new Line2D.Double(x, arrowY - 4, x, arrowY + 4));
        }
        String wLabel = String.format("%.0fcm  (%.2fm)", w, w / 100.0);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(wLabel, (float) ((w - fm.stringWidth(wLabel)) / 2.0), (float) (arrowY - 5));

        // Depth dimension line (left side)
        double arrowX = -28;
        g2.draw(new Line2D.Double(arrowX, 0, arrowX, d));
        g2.draw(new Line2D.Double(arrowX, 0, arrowX - 4, 7));
        g2.draw(new Line2D.Double(arrowX, 0, arrowX + 4, 7));
        g2.draw(new Line2D.Double(arrowX, d, arrowX - 4, d - 7));
        g2.draw(new Line2D.Double(arrowX, d, arrowX + 4, d - 7));
        for (double y = 100; y < d; y += 100) {
            g2.draw(new Line2D.Double(arrowX - 4, y, arrowX + 4, y));
        }
        // Rotated depth label
        AffineTransform at = g2.getTransform();
        g2.rotate(-Math.PI / 2, arrowX, d / 2);
        String dLabel = String.format("%.0fcm  (%.2fm)", d, d / 100.0);
        g2.drawString(dLabel, (float) ((arrowX - fm.stringWidth(dLabel) / 2.0)), (float) (d / 2 - 4));
        g2.setTransform(at);

        // Grid labels (50 cm)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, (int) Math.max(6, 9 / scale)));
        g2.setColor(new Color(130, 155, 195, 160));
        for (double x = 0; x <= w; x += 100) {
            g2.drawString(String.format("%.0f", x), (float) (x + 2), (float) (-12));
        }
    }

    private void drawRoomInfo(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, (int) Math.max(8, 11 / scale)));
        g2.setColor(new Color(99, 179, 237, 200));
        String areaStr = String.format("Area: %.1f m²  |  %s",
                room.getAreaM2(), room.getRoomType().replace("_", " "));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(areaStr,
                (float) ((room.getWidth() - fm.stringWidth(areaStr)) / 2.0),
                (float) (room.getDepth() + 18));
    }

    private void drawCompassRose(Graphics2D g2, int cx, int cy, int r) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(99, 179, 237, 150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        // North arrow (red)
        g2.setColor(new Color(236, 72, 153, 220));
        g2.fillPolygon(new int[] { cx, cx - 7, cx + 7 }, new int[] { cy - r + 4, cy + 5, cy + 5 }, 3);
        // South arrow (white)
        g2.setColor(new Color(180, 190, 210, 180));
        g2.fillPolygon(new int[] { cx, cx - 7, cx + 7 }, new int[] { cy + r - 4, cy - 5, cy - 5 }, 3);
        // North label
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        g2.drawString("N", cx - 4, cy - r + 16);
    }

    private void drawHintBar(Graphics2D g2) {
        String hint = "Scroll: Zoom  |  Space+Drag: Pan  |  Ctrl+C/V: Copy/Paste  |  M: Measurements  |  Del: Remove  |  Ctrl+Z/Y: Undo/Redo";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int hw = fm.stringWidth(hint);
        int barX = (getWidth() - hw) / 2 - 10, barY = getHeight() - 22;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(barX, barY, hw + 20, 16, 6, 6);
        g2.setColor(new Color(140, 165, 200, 200));
        g2.drawString(hint, barX + 10, barY + 12);
    }

    // Helper methods

    public void fitRoom() {
        if (room == null)
            return;
        double margin = 80;
        double viewW = getWidth() - margin * 2;
        double viewH = getHeight() - margin * 2;
        scale = Math.min(viewW / room.getWidth(), viewH / room.getDepth());
        if (scale <= 0)
            scale = 0.5;
        offsetX = (getWidth() - room.getWidth() * scale) / 2.0;
        offsetY = (getHeight() - room.getDepth() * scale) / 2.0;
        if (statusBar != null) {
            statusBar.setZoom((int) (scale * 100));
            statusBar.setRoomInfo(room.getWidth(), room.getDepth());
        }
    }

    private double toRoomX(int screenX) {
        return (screenX - offsetX) / scale;
    }

    private double toRoomY(int screenY) {
        return (screenY - offsetY) / scale;
    }

    private int toScreenX(double rx) {
        return (int) (rx * scale + offsetX);
    }

    private int toScreenY(double ry) {
        return (int) (ry * scale + offsetY);
    }

    private boolean hitTest(Furniture f, double rx, double ry) {
        double cx = f.getX() + f.getWidth() / 2.0;
        double cy = f.getY() + f.getDepth() / 2.0;
        double rad = Math.toRadians(-f.getRotation());
        double dx = rx - cx, dy = ry - cy;
        double lx = cx + dx * Math.cos(rad) - dy * Math.sin(rad);
        double ly = cy + dx * Math.sin(rad) + dy * Math.cos(rad);
        return new Rectangle2D.Double(f.getX(), f.getY(), f.getWidth(), f.getDepth()).contains(lx, ly);
    }

    private Color contrastColor(Color bg) {
        double lum = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        return lum > 140 ? new Color(30, 30, 30) : Color.WHITE;
    }

    // Public methods

    public void setRoom(Room room) {
        this.room = room;
        scale = 1.0; // Trigger auto fit on next paint
        guideLines.clear();
        hasCollision = false;
        multiSelected.clear();
        rubberRect = null;
        selectedFurniture = null;
        repaint();
    }

    public void setSelectionListener(SelectionListener l) {
        selectionListener = l;
    }

    public void setController(DesignController c) {
        controller = c;
    }

    public void setStatusBar(StatusBar sb) {
        statusBar = sb;
    }

    public void setShowMeasurements(boolean v) {
        showMeasurements = v;
        repaint();
    }

    public boolean isShowMeasurements() {
        return showMeasurements;
    }

    public Furniture getSelectedFurniture() {
        return selectedFurniture;
    }

    public List<Furniture> getMultiSelected() {
        return multiSelected;
    }

    public void clearSelection() {
        selectedFurniture = null;
        multiSelected.clear();
        if (selectionListener != null)
            selectionListener.onSelectionChanged(null);
    }
}
