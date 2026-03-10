package ui.editor;

import engine3d.Camera;
import engine3d.Renderer3D;
import models.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Canvas3D — 3D view with mouse drag orbit, scroll zoom, WASD walk, FPS
 * counter.
 * MEMBER 2 CONTRIBUTION: 3D Engine Upgrades
 */
public class Canvas3D extends JPanel {

    private Room room;
    private Camera camera = new Camera();

    // Mouse drag state
    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    // FPS
    private int fps = 0;
    private long lastFrameTime = System.nanoTime();
    private int frameCount = 0;

    // Render timer
    private Timer renderTimer;

    private static final Color BG = new Color(10, 15, 35);

    public Canvas3D() {
        setBackground(BG);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setupMouseControls();
        setupKeyboardControls();

        // Continuous render loop (~60fps)
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
    }

    private void setupMouseControls() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                dragging = true;
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!dragging || room == null)
                    return;
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Left drag: orbit
                    camera.rotateYaw(dx * 0.35);
                    camera.rotatePitch(-dy * 0.25);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Right drag: zoom
                    camera.zoom(dy * 4.0);
                }

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                camera.zoom(e.getWheelRotation() * 25.0);
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    private void setupKeyboardControls() {
        addKeyListener(new KeyAdapter() {
            private final double WALK_STEP = 20;

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> camera.walkForward(WALK_STEP);
                    case KeyEvent.VK_S -> camera.walkForward(-WALK_STEP);
                    case KeyEvent.VK_A -> camera.walkStrafe(-WALK_STEP);
                    case KeyEvent.VK_D -> camera.walkStrafe(WALK_STEP);
                    case KeyEvent.VK_F -> camera.toggleFirstPerson();
                    case KeyEvent.VK_R -> resetCamera();
                    case KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS -> camera.zoom(-40);
                    case KeyEvent.VK_MINUS -> camera.zoom(40);
                    case KeyEvent.VK_LEFT -> camera.rotateYaw(-5);
                    case KeyEvent.VK_RIGHT -> camera.rotateYaw(5);
                    case KeyEvent.VK_UP -> camera.rotatePitch(4);
                    case KeyEvent.VK_DOWN -> camera.rotatePitch(-4);
                }
            }
        });
    }

    public void setRoom(Room room) {
        this.room = room;
        resetCamera();
        repaint();
    }

    private void resetCamera() {
        if (room != null) {
            camera.radius = Math.max(room.getWidth(), room.getDepth()) * 1.1;
            camera.yaw = 40;
            camera.pitch = 32;
            camera.firstPerson = false;
            camera.orbitAround(room.getWidth(), room.getDepth());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (room == null) {
            g2.setColor(new Color(99, 179, 237));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString("No room loaded. Go back and create one.", 40, getHeight() / 2);
            return;
        }

        // Orbit
        camera.orbitAround(room.getWidth(), room.getDepth());

        // Render
        Renderer3D.render(g2, room, camera, getWidth(), getHeight());

        // FPS counter
        frameCount++;
        long now = System.nanoTime();
        if (now - lastFrameTime >= 1_000_000_000L) {
            fps = frameCount;
            frameCount = 0;
            lastFrameTime = now;
        }

        // HUD overlay
        drawHUD(g2);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // FPS badge
        String fpsTxt = fps + " FPS";
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        int fw = g2.getFontMetrics().stringWidth(fpsTxt) + 14;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(getWidth() - fw - 8, 8, fw, 22, 6, 6);
        g2.setColor(fps >= 30 ? new Color(99, 220, 130) : new Color(237, 120, 80));
        g2.drawString(fpsTxt, getWidth() - fw - 2, 23);

        // Mode badge
        String mode = camera.firstPerson ? "\uD83D\uDC41 First Person [F]" : "\uD83E\uDEA9 Orbit [F]";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        int mw = g2.getFontMetrics().stringWidth(mode) + 14;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(8, 8, mw, 22, 6, 6);
        g2.setColor(new Color(160, 200, 255));
        g2.drawString(mode, 14, 23);

        // Controls hint at bottom
        String hint = "Drag: Orbit  |  Scroll/+/-: Zoom  |  WASD: Walk  |  F: Toggle Mode  |  R: Reset";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect((getWidth() - hw) / 2 - 8, getHeight() - 28, hw + 16, 18, 6, 6);
        g2.setColor(new Color(160, 180, 210, 200));
        g2.drawString(hint, (getWidth() - hw) / 2, getHeight() - 15);
    }
}
