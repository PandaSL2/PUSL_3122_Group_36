package ui.editor;

import controllers.DesignController;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import models.Furniture;
import models.Room;
import ui.components.StatusBar;
import utils.CollisionDetector;
import utils.SnapHelper;
import utils.commands.MoveFurnitureCommand;

/**
 * Interactive 2D floor plan canvas for the RoomCraft application.
 * <p>
 * Provides the primary visual editor for placing, moving, rotating, and arranging
 * furniture within a room model. Supports intuitive drag-and-drop interaction,
 * assisted placement (snapping, collision avoidance), multi-selection, zooming/panning,
 * copy-paste, undo/redo integration, and informative overlays (measurements, compass, grid).
 * </p>
 * 
 * <p>Key implemented features:</p>
 * <ul>
 *   <li>Snap-to-wall and snap-to-furniture guide lines</li>
 *   <li>Real-time collision detection with visual feedback and position reversion</li>
 *   <li>Room and furniture dimension overlays (toggleable with 'M')</li>
 *   <li>Compass rose indicating orientation</li>
 *   <li>Rubber-band multi-selection</li>
 *   <li>Mouse-wheel zoom centered on cursor</li>
 *   <li>Spacebar + drag panning</li>
 *   <li>Clipboard-based copy/paste (Ctrl+C/V)</li>
 *   <li>Status bar integration for cursor coordinates, zoom level, and selection</li>
 * </ul>
 * 
 * <p>Contribution: Measurement overlays, dimension labels, compass indicator,
 * room/area information display, and enhanced visual feedback mechanisms.</p>
 * 
 * @author [10953373 - Udawaththa Wijegunawardhana]
 * @version 2.0
 * @since PUSL3122 Group Project
 */
public class Canvas2D extends JPanel {

    private Room room;
    private double scale = 1.0;
    private double offsetX = 60;
    private double offsetY = 60;
    private final int GRID_SIZE = 50; //cm

    // Selection & interaction state
    private Furniture selectedFurniture;
    private DesignController controller;
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double initialFurnitureX, initialFurnitureY;

    // Snapping & collision
    private SnapHelper snapHelper = new SnapHelper();
    private List<SnapHelper.GuideLine> guideLines = new ArrayList<>();
    private boolean hasCollision = false;

    // Rubber-band multi-selection
    private Point rubberStart;
    private Rectangle rubberRect;
    private List<Furniture> multiSelected = new ArrayList<>();

    // View control (pan/zoom)
    private boolean spaceHeld = false;
    private int panStartX, panStartY;
    private double panOffsetXStart, panOffsetYStart;
    private boolean isPanning = false;

    // Clipboard
    private Furniture clipboard;

    // UI toggles
    private boolean showMeasurements = true;

    // External UI references
    private StatusBar statusBar;

    /**
     * Listener interface for notifying parent components of selection changes.
     */
    public interface SelectionListener {
        void onSelectionChanged(Furniture f);
    }


    private SelectionListener selectionListener;

    /**
     * Constructs the canvas and initializes input handlers.
     */
    public Canvas2D() {
        setBackground(new Color(18, 26, 55));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setupMouseHandlers();
        setupKeyboardHandlers();
    }

    /**
     * Registers mouse listeners for selection, dragging, rubber-band,
     * panning, and wheel-based zooming.
     */
    private void setupMouseHandlers() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

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

                // Select topmost furniture under cursor (reverse iteration)
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

                // No hit → begin rubber-band selection
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
                // Panning
                if (isPanning) {
                    offsetX = panOffsetXStart + (e.getX() - panStartX);
                    offsetY = panOffsetYStart + (e.getY() - panStartY);
                    repaint();
                    return;
                }

                // Rubber-band multi selection
                if (rubberStart != null && selectedFurniture == null) {
                    int x = Math.min(rubberStart.x, e.getX());
                    int y = Math.min(rubberStart.y, e.getY());
                    int w = Math.abs(e.getX() - rubberStart.x);
                    int h = Math.abs(e.getY() - rubberStart.y);
                    rubberRect = new Rectangle(x, y, w, h);
                    // Select all furniture within rubber rect
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

                // Furniture dragging
                if (isDragging && selectedFurniture != null && room != null) {
                    double roomX = toRoomX(e.getX());
                    double roomY = toRoomY(e.getY());
                    double dx = roomX - dragStartX;
                    double dy = roomY - dragStartY;
                    double newX = initialFurnitureX + dx;
                    double newY = initialFurnitureY + dy;

                    // Grid snap (10cm)
                    newX = Math.round(newX / 10.0) * 10;
                    newY = Math.round(newY / 10.0) * 10;

                    selectedFurniture.setX(newX);
                    selectedFurniture.setY(newY);

                    // Wall + furniture snap
                    guideLines = snapHelper.snap(selectedFurniture, room);

                    // Collision check
                    hasCollision = CollisionDetector.hasCollision(selectedFurniture, room);

                    // Status bar cursor
                    if (statusBar != null)
                        statusBar.setCursorPos(selectedFurniture.getX(), selectedFurniture.getY());

                    repaint();
                }
            }

            /**
             * Updates cursor coordinates in the status bar.
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                if (statusBar != null && room != null) {
                    statusBar.setCursorPos(toRoomX(e.getX()), toRoomY(e.getY()));
                }
            }

            
            /**
             * Finalizes drag actions and commits move commands.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                isPanning = false;

                if (isDragging && selectedFurniture != null) {
                    isDragging = false;
                    if (hasCollision) {
                        // Revert position on collision
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

            
            /**
             * Handles scroll wheel zooming.
             */
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Zoom toward cursor position
                double oldScale = scale;
                double factor = e.getWheelRotation() < 0 ? 1.12 : 0.89;
                scale = Math.max(0.1, Math.min(10.0, scale * factor));

                // Adjust offset so cursor stays fixed
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

    /**
     * Registers keyboard shortcuts for common editing operations.
     */
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

        // Ctrl+C — copy
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
        getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (selectedFurniture != null) {
                    clipboard = selectedFurniture.copy();
                }
            }
        });

        // Ctrl+V — paste
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

        // Delete / Backspace
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

        // Ctrl+Z / Ctrl+Y
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

        // Escape — deselect
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

        // M — toggle measurements
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

        // Professional Blueprint Background
        g.setColor(new Color(10, 15, 35));
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Auto-fit on first paint if scale not manually set
        if (scale == 1.0)
            fitRoom();

        AffineTransform saved = g2.getTransform();
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        // 1. Floor + shadow
        Color fc = room.getFloorColor();
        g2.setColor(new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), 180));
        g2.fillRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // 2. Grid
        g2.setColor(new Color(0, 0, 0, 22));
        g2.setStroke(new BasicStroke(0.5f));
        for (double x = 0; x <= room.getWidth(); x += GRID_SIZE)
            g2.draw(new Line2D.Double(x, 0, x, room.getDepth()));
        for (double y = 0; y <= room.getDepth(); y += GRID_SIZE)
            g2.draw(new Line2D.Double(0, y, room.getWidth(), y));

        // 3. Room wall outline
        g2.setColor(new Color(30, 45, 80));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // 4. Snap guide lines (below furniture)
        for (SnapHelper.GuideLine gl : guideLines) {
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 5, 4 }, 0));
            g2.setColor(gl.color());
            g2.draw(new Line2D.Double(gl.x1(), gl.y1(), gl.x2(), gl.y2()));
        }

        // 5. Furniture
        for (Furniture f : room.getFurnitureList()) {
            drawFurniture(g2, f);
        }

        // 6. Multi-select highlight
        for (Furniture f : multiSelected) {
            drawSelectionHighlight(g2, f, new Color(99, 179, 237, 80));
        }

        // 7. Rubber band
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

        // 8. Measurements overlay
        if (showMeasurements)
            drawMeasurements(g2);

        // 9. Room info (bottom of room)
        drawRoomInfo(g2);

        g2.setTransform(saved);

        // 10. Compass rose (overlay — screen space)
        drawCompassRose(g2, getWidth() - 60, 60, 40);

        // 11. Hint bar
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

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fill(new Rectangle2D.Double(f.getX() + 3, f.getY() + 3, f.getWidth(), f.getDepth()));

        // Render Professional Schematic Icon
        Color fill = collision ? new Color(255, 80, 80, 180) : f.getColor();
        utils.SchematicPainter.paintIcon(g2, f.getType(), (int) f.getX(), (int) f.getY(), (int) f.getWidth(),
                (int) f.getDepth(),
                fill, isSelected || inMulti);

        // Label
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

        // Front-direction arrow
        if (f.getWidth() * scale > 40) {
            double ax = f.getX() + f.getWidth() / 2.0;
            double frontY = f.getY() + f.getDepth();
            int[] xs = { (int) (ax - 4), (int) (ax + 4), (int) ax };
            int[] ys = { (int) (frontY - 9), (int) (frontY - 9), (int) frontY };
            g2.setColor(new Color(255, 255, 255, 160));
            g2.fillPolygon(xs, ys, 3);
        }

        // Selected: dimension labels adjacent to box
        if (isSelected && showMeasurements) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) Math.max(7, 10 / scale)));
            g2.setColor(new Color(236, 220, 60));
            g2.setStroke(new BasicStroke(0.8f));
            // Width label (above)
            String wLabel = String.format("%.0fcm", f.getWidth());
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(wLabel,
                    (float) (f.getX() + (f.getWidth() - fm2.stringWidth(wLabel)) / 2f),
                    (float) (f.getY() - 4));
            // Depth label (right)
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

        // Width dimension line (above room)
        double arrowY = -28;
        g2.draw(new Line2D.Double(0, arrowY, w, arrowY));
        // Arrow heads
        g2.draw(new Line2D.Double(0, arrowY, 7, arrowY - 4));
        g2.draw(new Line2D.Double(0, arrowY, 7, arrowY + 4));
        g2.draw(new Line2D.Double(w, arrowY, w - 7, arrowY - 4));
        g2.draw(new Line2D.Double(w, arrowY, w - 7, arrowY + 4));
        // Tick marks at each metre
        for (double x = 100; x < w; x += 100) {
            g2.draw(new Line2D.Double(x, arrowY - 4, x, arrowY + 4));
        }
        String wLabel = String.format("%.0fcm  (%.2fm)", w, w / 100.0);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(wLabel, (float) ((w - fm.stringWidth(wLabel)) / 2.0), (float) (arrowY - 5));

        // Depth dimension line (left of room)
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

        // Grid labels (50cm markers)
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

    /**
     * Draws a compass rose on the canvas to indicate orientation.
     * The compass includes a circular background, directional arrows,
     *  and a label showing the North direction.
 
     *  @param g2 Graphics2D object used for rendering
     *  @param cx x-coordinate of the compass center
     * @param cy y-coordinate of the compass center
     * @param r  radius of the compass
    */
    private void drawCompassRose(Graphics2D g2, int cx, int cy, int r) {
        // Enable smooth rendering for shapes
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(99, 179, 237, 150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);

        // N arrow (red)
        g2.setColor(new Color(236, 72, 153, 220));
        g2.fillPolygon(new int[] { cx, cx - 7, cx + 7 }, new int[] { cy - r + 4, cy + 5, cy + 5 }, 3);
        
        // S arrow (white)
        g2.setColor(new Color(180, 190, 210, 180));
        g2.fillPolygon(new int[] { cx, cx - 7, cx + 7 }, new int[] { cy + r - 4, cy - 5, cy - 5 }, 3);
        
        // N label
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

    // View & Coordinate Helpers
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

    // Public API


    /**
     * Assigns the current room model to the canvas.
     */
    public void setRoom(Room room) {
        this.room = room;
        scale = 1.0; // trigger auto-fit on next paint
        guideLines.clear();
        hasCollision = false;
        multiSelected.clear();
        rubberRect = null;
        selectedFurniture = null;
        repaint();
    }

    /**
     * Registers a listener that will receive selection updates.
     */
    public void setSelectionListener(SelectionListener l) {
        selectionListener = l;
    }

    /**
     * Assigns the main design controller used for executing commands.
     */
    public void setController(DesignController c) {
        controller = c;
    }

    /**
     * Connects the canvas to a status bar for displaying cursor and zoom info.
     */
    public void setStatusBar(StatusBar sb) {
        statusBar = sb;
    }

    /**
     * Enables or disables the measurement overlay.
     */
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

    /**
     * Returns all furniture objects currently selected by rubber-band.
     */
    public List<Furniture> getMultiSelected() {
        return multiSelected;
    }

     /**
     * Clears all current selections.
     */
    public void clearSelection() {
        selectedFurniture = null;
        multiSelected.clear();
        if (selectionListener != null)
            selectionListener.onSelectionChanged(null);
    }
}
