package engine3d;

import models.Furniture;
import models.Room;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Renderer3D {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private static double[] zBuffer;
    private static int[] pixelBuffer;
    private static BufferedImage screenBuffer;

    // Lighting parameters
    private static final double AMBIENT = 0.22;
    private static final double DIFFUSE_STR = 0.65;
    private static final double SPECULAR_STR = 0.18;
    private static final int SPECULAR_POW = 24;

    // Shadow floor darkening
    private static final double SHADOW_ALPHA = 0.28;

    // Currently selected furniture id for highlight
    private static String selectedId = null;

    public static void setSelectedId(String id) {
        selectedId = id;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public static void render(Graphics2D g2, Room room, Camera camera,
            int panelWidth, int panelHeight) {
        // Resize buffer if needed
        if (screenBuffer == null
                || screenBuffer.getWidth() != WIDTH
                || screenBuffer.getHeight() != HEIGHT) {
            screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            pixelBuffer = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
            zBuffer = new double[WIDTH * HEIGHT];
        }

        // Clear
        Arrays.fill(zBuffer, Double.POSITIVE_INFINITY);

        // Sky gradient background
        fillSkyGradient(room);

        // View / Projection matrices
        Matrix4x4 view = Matrix4x4.getLookAt(camera.position, camera.target, camera.up);
        Matrix4x4 proj = Matrix4x4.getProjection(65, (double) WIDTH / HEIGHT, 0.5, 2000);
        Matrix4x4 vp = proj.multiply(view);

        // Light direction (warm sunlight angle)
        Vector3 lightDir = new Vector3(0.5, 1.0, 0.6).normalize();
        // Camera/eye position for specular
        Vector3 eyePos = camera.position;

        // 1. Ceiling
        renderCeiling(room, vp, lightDir, eyePos);

        // 2. Floor (with procedural wood texture)
        renderFloor(room, vp, lightDir, eyePos);

        // 3. Walls
        renderWalls(room, vp, lightDir, eyePos);

        // 4. Furniture shadows (dark floor quads, drawn before furniture)
        for (Furniture f : room.getFurnitureList()) {
            renderFurnitureShadow(f, vp);
        }

        // 5. Furniture
        for (Furniture f : room.getFurnitureList()) {
            renderFurniture(f, vp, lightDir, eyePos);
        }

        // Scale to panel
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(screenBuffer, 0, 0, panelWidth, panelHeight, null);

        // FPS / overlay (drawn in Java2D on top)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(99, 179, 237, 180));
        g2.drawString("3D View  |  Scroll: zoom  |  Drag: orbit  |  WASD: walk", 10, panelHeight - 12);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BACKGROUND
    // ─────────────────────────────────────────────────────────────────────────

    private static void fillSkyGradient(Room room) {
        Color c = room.getCeilingColor() != null ? room.getCeilingColor() : new Color(220, 230, 255);
        Color sky = c.brighter();
        Color horiz = new Color(210, 210, 220);
        for (int y = 0; y < HEIGHT; y++) {
            float t = (float) y / HEIGHT;
            int r = (int) (sky.getRed() * (1 - t) + horiz.getRed() * t);
            int g = (int) (sky.getGreen() * (1 - t) + horiz.getGreen() * t);
            int b = (int) (sky.getBlue() * (1 - t) + horiz.getBlue() * t);
            int row = y * WIDTH;
            Arrays.fill(pixelBuffer, row, row + WIDTH,
                    clamp(r) << 16 | clamp(g) << 8 | clamp(b));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GEOMETRY SECTIONS
    // ─────────────────────────────────────────────────────────────────────────

    private static void renderCeiling(Room room, Matrix4x4 vp, Vector3 light, Vector3 eye) {
        double w = room.getWidth(), d = room.getDepth(), h = room.getHeight();
        Color c = room.getCeilingColor() != null ? room.getCeilingColor() : new Color(250, 250, 255);
        double step = 100;
        for (double x = 0; x < w; x += step) {
            for (double z = 0; z < d; z += step) {
                double cw = Math.min(step, w - x), cd = Math.min(step, d - z);
                addQuad(
                        new Vector3(x, h, z), new Vector3(x + cw, h, z),
                        new Vector3(x + cw, h, z + cd), new Vector3(x, h, z + cd),
                        vp, c, light, eye, false);
            }
        }
    }

    private static void renderFloor(Room room, Matrix4x4 vp, Vector3 light, Vector3 eye) {
        double w = room.getWidth(), d = room.getDepth();
        Color base = room.getFloorColor();
        double step = 40; // Finer grid for wood texture
        boolean checker = true;
        for (double x = 0; x < w; x += step) {
            for (double z = 0; z < d; z += step) {
                double cw = Math.min(step, w - x), cd = Math.min(step, d - z);
                // Alternating plank tones (wood grain simulation)
                int plankIdx = (int) (z / step);
                Color plankColor = checker && (plankIdx % 2 == 0)
                        ? base
                        : new Color(
                                Math.min(255, base.getRed() + 12),
                                Math.max(0, base.getGreen() - 8),
                                Math.max(0, base.getBlue() - 5));
                addQuad(
                        new Vector3(x, 0, z), new Vector3(x, 0, z + cd),
                        new Vector3(x + cw, 0, z + cd), new Vector3(x + cw, 0, z),
                        vp, plankColor, light, eye, false);
            }
        }
    }

    private static void renderWalls(Room room, Matrix4x4 vp, Vector3 light, Vector3 eye) {
        double w = room.getWidth(), d = room.getDepth(), h = room.getHeight();
        Color c = room.getWallColor() != null ? room.getWallColor() : new Color(230, 230, 240);
        double step = 80;

        // Back wall (Z=0, facing +Z)
        for (double x = 0; x < w; x += step) {
            for (double y = 0; y < h; y += step) {
                double cw = Math.min(step, w - x), ch = Math.min(step, h - y);
                addQuad(new Vector3(x + cw, y, 0), new Vector3(x, y, 0),
                        new Vector3(x, y + ch, 0), new Vector3(x + cw, y + ch, 0),
                        vp, c, light, eye, false);
            }
        }
        // Left wall (X=0, facing +X)
        for (double z = 0; z < d; z += step) {
            for (double y = 0; y < h; y += step) {
                double cd = Math.min(step, d - z), ch = Math.min(step, h - y);
                addQuad(new Vector3(0, y, z), new Vector3(0, y + ch, z),
                        new Vector3(0, y + ch, z + cd), new Vector3(0, y, z + cd),
                        vp, c, light, eye, false);
            }
        }
    }

    private static void renderFurnitureShadow(Furniture f, Matrix4x4 vp) {
        double x = f.getX(), z = f.getY(), w = f.getWidth(), d = f.getDepth();
        double rot = Math.toRadians(f.getRotation());
        double cx = x + w / 2, cz = z + d / 2;
        // Inset shadow slightly
        double inset = 4;
        Vector3 p1 = transform(x + inset, -0.5, z + inset, cx, cz, rot);
        Vector3 p2 = transform(x + w - inset, -0.5, z + inset, cx, cz, rot);
        Vector3 p3 = transform(x + w - inset, -0.5, z + d - inset, cx, cz, rot);
        Vector3 p4 = transform(x + inset, -0.5, z + d - inset, cx, cz, rot);

        // We draw shadow as a darkened floor quad using SHADOW_ALPHA
        addQuadRaw(p1, p4, p3, p2, vp, new Color(0, 0, 0, (int) (255 * SHADOW_ALPHA)));
    }

    private static void renderFurniture(Furniture f, Matrix4x4 vp,
            Vector3 light, Vector3 eye) {
        double x = f.getX(), z = f.getY(), w = f.getWidth(),
                d = f.getDepth(), h = f.getHeight();
        double rot = Math.toRadians(f.getRotation());
        double cx = x + w / 2, cz = z + d / 2;
        Color c = f.getColor();
        boolean selected = f.getId() != null && f.getId().equals(selectedId);

        switch (f.getType()) {
            case TABLE -> renderTable(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case CHAIR -> renderChair(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case SOFA -> renderSofa(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case BED -> renderBed(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case DESK -> renderDesk(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case BOOKSHELF -> renderBookshelf(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case RUG -> renderRug(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case LAMP -> renderLamp(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case PLANT -> renderPlant(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case WARDROBE -> renderWardrobe(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case TV_STAND -> renderTVStand(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case BATHTUB -> renderBathtub(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case TOILET -> renderToilet(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case SINK -> renderSink(x, z, w, d, h, rot, cx, cz, c, vp, light, eye);
            case CABINET -> addBox(x, 0, z, w, h, d, rot, cx, cz, c, vp, light, eye);
            default -> addBox(x, 0, z, w, h, d, rot, cx, cz, c, vp, light, eye);
        }

        // Selection highlight: bright outline box
        if (selected) {
            Color hl = new Color(99, 220, 255);
            // thin outer shell
            addBox(x - 2, 0, z - 2, w + 4, h + 2, d + 4, rot, cx, cz, hl, vp, light, eye);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FURNITURE GEOMETRY
    // ─────────────────────────────────────────────────────────────────────────

    private static void renderTable(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double topH = h * 0.06, legW = w * 0.07;
        addBox(x, h - topH, z, w, topH, d, rot, cx, cz, c, vp, l, e);
        for (double ox : new double[] { 0, w - legW })
            for (double oz : new double[] { 0, d - legW })
                addBox(x + ox, 0, z + oz, legW, h - topH, legW, rot, cx, cz, c.darker(), vp, l, e);
    }

    private static void renderChair(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double seatH = h * 0.45, legW = w * 0.09, backD = d * 0.12;
        addBox(x, seatH, z, w, h * 0.06, d, rot, cx, cz, c, vp, l, e); // seat
        addBox(x, seatH, z, w, h - seatH, backD, rot, cx, cz, c.darker(), vp, l, e); // back
        for (double ox : new double[] { 0, w - legW })
            for (double oz : new double[] { 0, d - legW })
                addBox(x + ox, 0, z + oz, legW, seatH, legW, rot, cx, cz, c.darker(), vp, l, e);
    }

    private static void renderSofa(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double seatH = h * 0.4, backD = d * 0.2, armW = w * 0.12;
        addBox(x, 0, z, w, seatH, d, rot, cx, cz, c, vp, l, e); // base
        addBox(x, seatH, z, w, h - seatH, backD, rot, cx, cz, c.darker(), vp, l, e); // back
        addBox(x, seatH, z + backD, armW, h * 0.28, d - backD, rot, cx, cz, c.darker(), vp, l, e); // left arm
        addBox(x + w - armW, seatH, z + backD, armW, h * 0.28, d - backD, rot, cx, cz, c.darker(), vp, l, e); // right
                                                                                                              // arm
        // Cushion
        Color cush = c.brighter();
        addBox(x + armW + 2, seatH, z + backD + 2, w - armW * 2 - 4, h * 0.12, d - backD - 4, rot, cx, cz, cush, vp, l,
                e);
    }

    private static void renderBed(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double baseH = h * 0.35, headD = d * 0.06;
        addBox(x, 0, z, w, baseH, d, rot, cx, cz, c.darker(), vp, l, e); // frame
        addBox(x, 0, z, w, h, headD, rot, cx, cz, c, vp, l, e); // headboard
        addBox(x + 4, baseH, z + headD + 4, w - 8, h * 0.16, d - headD - 8, rot, cx, cz, Color.WHITE, vp, l, e); // mattress
        // Pillow
        addBox(x + 10, baseH + h * 0.16, z + headD + 6, w / 2 - 15, h * 0.06, d * 0.18, rot, cx, cz,
                new Color(240, 240, 250), vp, l, e);
        addBox(x + w / 2 + 5, baseH + h * 0.16, z + headD + 6, w / 2 - 15, h * 0.06, d * 0.18, rot, cx, cz,
                new Color(240, 240, 250), vp, l, e);
    }

    private static void renderDesk(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double topH = h * 0.05, drawerW = w * 0.3;
        addBox(x, h - topH, z, w, topH, d, rot, cx, cz, c, vp, l, e); // top
        addBox(x, 0, z, drawerW, h - topH, d, rot, cx, cz, c.darker(), vp, l, e); // drawers
        addBox(x + w - 8, 0, z, 8, h - topH, d, rot, cx, cz, c.darker(), vp, l, e); // right leg
    }

    private static void renderBookshelf(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double thick = 5;
        addBox(x, 0, z, thick, h, d, rot, cx, cz, c.darker(), vp, l, e); // left side
        addBox(x + w - thick, 0, z, thick, h, d, rot, cx, cz, c.darker(), vp, l, e); // right side
        addBox(x, 0, z, w, h, thick, rot, cx, cz, c.darker(), vp, l, e); // back
        addBox(x, h - thick, z, w, thick, d, rot, cx, cz, c.darker(), vp, l, e); // top
        // Shelves + random book colors
        Color[] books = { new Color(180, 60, 60), new Color(60, 100, 180), new Color(60, 150, 80),
                new Color(180, 130, 40) };
        for (int s = 1; s < 5; s++) {
            double sy = h * s * 0.18;
            addBox(x, sy, z, w, thick, d, rot, cx, cz, c, vp, l, e); // shelf
            // Books
            addBox(x + thick * 2, sy + thick, z + thick, w * 0.6, h * 0.15, d - thick * 2, rot, cx, cz,
                    books[s % books.length], vp, l, e);
        }
    }

    private static void renderRug(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        addBox(x, 0.5, z, w, 1.5, d, rot, cx, cz, c, vp, l, e);
        // Border in contrasting color
        Color border = new Color(
                Math.max(0, c.getRed() - 40), Math.max(0, c.getGreen() - 30), Math.min(255, c.getBlue() + 30));
        double bw = Math.min(8, w * 0.05);
        addBox(x, 0.5, z, w, 1.8, bw, rot, cx, cz, border, vp, l, e);
        addBox(x, 0.5, z + d - bw, w, 1.8, bw, rot, cx, cz, border, vp, l, e);
    }

    private static void renderLamp(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double poleW = w * 0.15, poleW2 = poleW * 0.5;
        addBox(x + w / 2 - poleW2, 0, z + d / 2 - poleW2, poleW, h * 0.85, poleW, rot, cx, cz, c.darker(), vp, l, e); // pole
        // Shade
        double radius = Math.min(w, d) / 2;
        // Base
        addCylinder(x + w / 2, 0, z + d / 2, radius * 0.8, h * 0.05, 12, rot, cx, cz, c.darker(), vp, l, e);
        // Pole
        addCylinder(x + w / 2, h * 0.05, z + d / 2, 4, h * 0.7, 8, rot, cx, cz, new Color(180, 180, 190), vp, l, e);
        // Shade
        addCylinder(x + w / 2, h * 0.75, z + d / 2, radius, h * 0.25, 16, rot, cx, cz, c, vp, l, e);
        // Glow (fake light source)
        addBox(x + w / 2 - 5, h * 0.82, z + d / 2 - 5, 10, 10, 10, rot, cx, cz, Color.WHITE, vp, l, e);
    }

    private static void renderPlant(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        // Pot
        addCylinder(x + w / 2, 0, z + d / 2, Math.min(w, d) / 3, h * 0.35, 12, rot, cx, cz, new Color(160, 130, 90), vp,
                l, e);
        // Foliage (multiple overlapping boxes for fullness)
        Color green = new Color(60, 140, 60);
        for (int i = 0; i < 3; i++) {
            double off = i * 0.08;
            addBox(x + w * (0.05 + off), h * 0.38 + i * 10, z + d * (0.05 + off),
                    w * (0.9 - off * 2), h * 0.45, d * (0.9 - off * 2),
                    rot, cx, cz, green, vp, l, e);
        }
    }

    private static void addCylinder(double cx_obj, double y, double cz_obj, double r, double h, int sides,
            double rad, double cx, double cz, Color c, Matrix4x4 vp, Vector3 light, Vector3 eye) {
        for (int i = 0; i < sides; i++) {
            double a1 = i * 2 * Math.PI / sides;
            double a2 = (i + 1) * 2 * Math.PI / sides;

            double x1 = cx_obj + Math.cos(a1) * r;
            double z1 = cz_obj + Math.sin(a1) * r;
            double x2 = cx_obj + Math.cos(a2) * r;
            double z2 = cz_obj + Math.sin(a2) * r;

            Vector3 p1 = transform(x1, y, z1, cx, cz, rad);
            Vector3 p2 = transform(x2, y, z2, cx, cz, rad);
            Vector3 p3 = transform(x2, y + h, z2, cx, cz, rad);
            Vector3 p4 = transform(x1, y + h, z1, cx, cz, rad);

            addQuad(p1, p2, p3, p4, vp, c, light, eye, false);

            // Top cap
            Vector3 centerTop = transform(cx_obj, y + h, cz_obj, cx, cz, rad);
            addQuad(centerTop, p3, p4, centerTop, vp, c, light, eye, true);
        }
    }

    private static void renderWardrobe(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        addBox(x, 0, z, w, h, d, rot, cx, cz, c, vp, l, e); // body
        // Door panel lines (darker vertical dividers)
        Color panel = c.darker();
        double thirds = w / 3;
        addBox(x + thirds - 2, 2, z, 3, h - 3, 2, rot, cx, cz, panel, vp, l, e);
        addBox(x + thirds * 2 - 2, 2, z, 3, h - 3, 2, rot, cx, cz, panel, vp, l, e);
        // Handles
        Color handle = new Color(200, 160, 80);
        addBox(x + thirds * 0.5 - 2, h * 0.47, z - 2, 4, h * 0.08, 4, rot, cx, cz, handle, vp, l, e);
        addBox(x + thirds * 1.5 - 2, h * 0.47, z - 2, 4, h * 0.08, 4, rot, cx, cz, handle, vp, l, e);
        addBox(x + thirds * 2.5 - 2, h * 0.47, z - 2, 4, h * 0.08, 4, rot, cx, cz, handle, vp, l, e);
    }

    private static void renderTVStand(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        addBox(x, 0, z, w, h, d, rot, cx, cz, c, vp, l, e); // body
        // Compartment lines
        Color line = c.darker().darker();
        addBox(x + w / 2 - 1, 2, z, 2, h - 3, d, rot, cx, cz, line, vp, l, e);
        // TV screen above
        Color tv = new Color(20, 20, 25);
        addBox(x + w * 0.05, h, z + 2, w * 0.9, h * 1.6, d * 0.12, rot, cx, cz, tv, vp, l, e);
        Color screen = new Color(50, 80, 120);
        addBox(x + w * 0.07, h + 4, z, w * 0.86, h * 1.4, d * 0.06, rot, cx, cz, screen, vp, l, e);
    }

    private static void renderBathtub(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        double wall = 6;
        addBox(x, 0, z, w, h, d, rot, cx, cz, c, vp, l, e); // outer
        addBox(x + wall, h * 0.3, z + wall, w - wall * 2, h * 0.7, d - wall * 2, rot, cx, cz,
                new Color(180, 210, 240), vp, l, e); // water/inner
        // Faucet
        addBox(x + w - 20, h + 2, z + d * 0.45, 8, 20, 8, rot, cx, cz, new Color(200, 200, 210), vp, l, e);
    }

    private static void renderToilet(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        // Tank
        addBox(x + w * 0.1, 0, z, w * 0.8, h, d * 0.35, rot, cx, cz, c, vp, l, e);
        // Bowl
        addBox(x, 0, z + d * 0.3, w, h * 0.6, d * 0.7, rot, cx, cz, c, vp, l, e);
        addBox(x + 4, h * 0.6, z + d * 0.3, w - 8, 4, d * 0.7 - 4, rot, cx, cz, c.brighter(), vp, l, e); // seat
    }

    private static void renderSink(double x, double z, double w, double d, double h,
            double rot, double cx, double cz, Color c, Matrix4x4 vp, Vector3 l, Vector3 e) {
        addBox(x, 0, z, w, h, d, rot, cx, cz, c.darker(), vp, l, e); // cabinet
        addBox(x + 4, h - 8, z + 4, w - 8, 8, d - 8, rot, cx, cz, c, vp, l, e); // basin top
        addBox(x + 8, h * 0.7, z + 8, w - 16, h * 0.3 - 2, d - 16, rot, cx, cz, new Color(200, 220, 240), vp, l, e); // basin
        // Tap
        addBox(x + w / 2 - 3, h + 2, z + 6, 6, 14, 6, rot, cx, cz, new Color(200, 200, 210), vp, l, e);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOX & QUAD HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static void addBox(double x, double y, double z,
            double w, double h, double d,
            double rad, double cx, double cz,
            Color c, Matrix4x4 vp, Vector3 light, Vector3 eye) {
        Vector3 p1 = transform(x, y, z, cx, cz, rad);
        Vector3 p2 = transform(x + w, y, z, cx, cz, rad);
        Vector3 p3 = transform(x + w, y, z + d, cx, cz, rad);
        Vector3 p4 = transform(x, y, z + d, cx, cz, rad);
        Vector3 p5 = transform(x, y + h, z, cx, cz, rad);
        Vector3 p6 = transform(x + w, y + h, z, cx, cz, rad);
        Vector3 p7 = transform(x + w, y + h, z + d, cx, cz, rad);
        Vector3 p8 = transform(x, y + h, z + d, cx, cz, rad);

        addQuad(p5, p6, p7, p8, vp, c, light, eye, true); // Top (specular)
        addQuad(p2, p1, p4, p3, vp, c.darker(), light, eye, false);
        addQuad(p4, p1, p5, p8, vp, c.darker(), light, eye, false);
        addQuad(p2, p3, p7, p6, vp, c.darker(), light, eye, false);
        addQuad(p1, p2, p6, p5, vp, c.darker(), light, eye, false);
        addQuad(p3, p4, p8, p7, vp, c.darker(), light, eye, false);
    }

    private static void addQuad(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4,
            Matrix4x4 vp, Color c, Vector3 light, Vector3 eyePos,
            boolean specular) {
        Vector4 c1 = transformClip(vp, v1);
        Vector4 c2 = transformClip(vp, v2);
        Vector4 c3 = transformClip(vp, v3);
        Vector4 c4 = transformClip(vp, v4);

        // Flat shading normal (world space)
        Vector3 normal = v2.sub(v1).cross(v3.sub(v1)).normalize();

        // Ambient + Diffuse
        double diff = Math.max(0, normal.dot(light));
        double ambient = AMBIENT;
        double totalLight = ambient + diff * DIFFUSE_STR;

        // Specular (Blinn-Phong)
        double spec = 0;
        if (specular && eyePos != null) {
            Vector3 viewDir = eyePos.sub(v1).normalize();
            Vector3 halfVec = light.add(viewDir).normalize();
            spec = Math.pow(Math.max(0, normal.dot(halfVec)), SPECULAR_POW) * SPECULAR_STR;
        }

        totalLight = Math.min(1.0, Math.max(0.0, totalLight + spec));

        int r = clamp((int) (c.getRed() * totalLight));
        int g = clamp((int) (c.getGreen() * totalLight));
        int b = clamp((int) (c.getBlue() * totalLight));
        int color = (r << 16) | (g << 8) | b;

        clipAndRasterize(c1, c2, c3, color);
        clipAndRasterize(c1, c3, c4, color);
    }

    /** Raw quad with fixed color (no lighting — for shadows) */
    private static void addQuadRaw(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4,
            Matrix4x4 vp, Color c) {
        int color = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
        clipAndRasterize(transformClip(vp, v1), transformClip(vp, v2), transformClip(vp, v3), color);
        clipAndRasterize(transformClip(vp, v1), transformClip(vp, v3), transformClip(vp, v4), color);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLIPPING & RASTERIZATION
    // ─────────────────────────────────────────────────────────────────────────

    static class Vector4 {
        double x, y, z, w;

        Vector4(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    private static void clipAndRasterize(Vector4 v1, Vector4 v2, Vector4 v3, int color) {
        List<Vector4> poly = new ArrayList<>();
        poly.add(v1);
        poly.add(v2);
        poly.add(v3);
        poly = clipPoly(poly, new Vector4(0, 0, 1, 1));
        if (poly.size() < 3)
            return;

        Vector3 s1 = toScreen(poly.get(0));
        for (int i = 1; i < poly.size() - 1; i++) {
            rasterizeTriangle(s1, toScreen(poly.get(i)), toScreen(poly.get(i + 1)), color);
        }
    }

    private static List<Vector4> clipPoly(List<Vector4> poly, Vector4 plane) {
        List<Vector4> out = new ArrayList<>();
        if (poly.isEmpty())
            return out;
        Vector4 start = poly.get(poly.size() - 1);
        double startDot = dot(start, plane);
        for (Vector4 end : poly) {
            double endDot = dot(end, plane);
            if (endDot >= 0) {
                if (startDot < 0)
                    out.add(intersect(start, end, startDot, endDot));
                out.add(end);
            } else {
                if (startDot >= 0)
                    out.add(intersect(start, end, startDot, endDot));
            }
            start = end;
            startDot = endDot;
        }
        return out;
    }

    private static Vector4 intersect(Vector4 p1, Vector4 p2, double d1, double d2) {
        double t = d1 / (d1 - d2);
        return new Vector4(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y),
                p1.z + t * (p2.z - p1.z), p1.w + t * (p2.w - p1.w));
    }

    private static double dot(Vector4 v, Vector4 p) {
        return v.x * p.x + v.y * p.y + v.z * p.z + v.w * p.w;
    }

    private static Vector4 transformClip(Matrix4x4 m, Vector3 v) {
        return new Vector4(
                v.x * m.m[0][0] + v.y * m.m[0][1] + v.z * m.m[0][2] + m.m[0][3],
                v.x * m.m[1][0] + v.y * m.m[1][1] + v.z * m.m[1][2] + m.m[1][3],
                v.x * m.m[2][0] + v.y * m.m[2][1] + v.z * m.m[2][2] + m.m[2][3],
                v.x * m.m[3][0] + v.y * m.m[3][1] + v.z * m.m[3][2] + m.m[3][3]);
    }

    private static Vector3 toScreen(Vector4 v) {
        double inv = 1.0 / v.w;
        return new Vector3((v.x * inv + 1) * 0.5 * WIDTH, (1 - (v.y * inv + 1) * 0.5) * HEIGHT, v.z * inv);
    }

    private static void rasterizeTriangle(Vector3 v1, Vector3 v2, Vector3 v3, int color) {
        int minX = (int) Math.max(0, Math.min(v1.x, Math.min(v2.x, v3.x)));
        int maxX = (int) Math.min(WIDTH - 1, Math.max(v1.x, Math.max(v2.x, v3.x)));
        int minY = (int) Math.max(0, Math.min(v1.y, Math.min(v2.y, v3.y)));
        int maxY = (int) Math.min(HEIGHT - 1, Math.max(v1.y, Math.max(v2.y, v3.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double w0 = edge(v2, v3, x, y), w1 = edge(v3, v1, x, y), w2 = edge(v1, v2, x, y);
                boolean front = (w0 >= 0 && w1 >= 0 && w2 >= 0), back = (w0 <= 0 && w1 <= 0 && w2 <= 0);
                if (front || back) {
                    double area = w0 + w1 + w2;
                    if (area == 0)
                        continue;
                    w0 /= area;
                    w1 /= area;
                    w2 /= area;
                    double z = w0 * v1.z + w1 * v2.z + w2 * v3.z;
                    int idx = y * WIDTH + x;
                    if (idx >= 0 && idx < zBuffer.length && z < zBuffer[idx]) {
                        zBuffer[idx] = z;
                        pixelBuffer[idx] = color;
                    }
                }
            }
        }
    }

    private static double edge(Vector3 a, Vector3 b, int px, int py) {
        return (px - a.x) * (b.y - a.y) - (py - a.y) * (b.x - a.x);
    }

    private static Vector3 transform(double x, double y, double z,
            double cx, double cz, double rad) {
        double rx = cx + (x - cx) * Math.cos(rad) - (z - cz) * Math.sin(rad);
        double rz = cz + (x - cx) * Math.sin(rad) + (z - cz) * Math.cos(rad);
        return new Vector3(rx, y, rz);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
