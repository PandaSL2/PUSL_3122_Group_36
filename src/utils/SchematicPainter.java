package utils;

import models.Furniture;
import java.awt.*;

/**
 * SchematicPainter — Professional top-down architectural drawing utility.
 * Programmatically draws furniture icons (AutoCAD style).
 */
public class SchematicPainter {

    public static void paintIcon(Graphics2D g2, Furniture.Type type, int x, int y, int w, int h, Color color,
            boolean selected) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.5f));

        Color mainColor = color;
        Color strokeColor = selected ? new Color(99, 179, 237) : mainColor.darker();
        Color fillColor = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 40);

        int pad = 4;
        int iw = w - pad * 2;
        int ih = h - pad * 2;
        int ix = x + pad;
        int iy = y + pad;

        switch (type) {
            case SOFA:
                drawSofa(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case BED:
                drawBed(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case TABLE:
            case DESK:
                drawTable(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case CHAIR:
                drawChair(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case PLANT:
                drawPlant(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case LAMP:
                drawLamp(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case TOILET:
                drawToilet(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case BATHTUB:
                drawBathtub(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case SINK:
                drawSink(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            case RUG:
                drawRug(g2, ix, iy, iw, ih, strokeColor, fillColor);
                break;
            default:
                g2.setColor(fillColor);
                g2.fillRoundRect(ix, iy, iw, ih, 4, 4);
                g2.setColor(strokeColor);
                g2.drawRoundRect(ix, iy, iw, ih, 4, 4);
                break;
        }
    }

    private static void drawSofa(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRoundRect(x, y, w, h, 6, 6);
        g2.setColor(s);
        g2.drawRoundRect(x, y, w, h, 6, 6);
        // Backrest (assuming front is down)
        g2.drawRoundRect(x, y, w, h / 4, 4, 4);
        // Armrests
        g2.drawRoundRect(x, y, w / 6, h, 4, 4);
        g2.drawRoundRect(x + w - w / 6, y, w / 6, h, 4, 4);
    }

    private static void drawBed(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRect(x, y, w, h);
        g2.setColor(s);
        g2.drawRect(x, y, w, h);
        // Pillows
        int pw = w / 3;
        int ph = h / 5;
        g2.drawRoundRect(x + (w / 2 - pw) / 2, y + 4, pw, ph, 4, 4);
        g2.drawRoundRect(x + w / 2 + (w / 2 - pw) / 2, y + 4, pw, ph, 4, 4);
        // Sheet line
        g2.drawLine(x, y + h / 3, x + w, y + h / 3);
    }

    private static void drawTable(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRect(x, y, w, h);
        g2.setColor(s);
        g2.drawRect(x, y, w, h);
        // Cross lines for surface detail
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawLine(x + 5, y + 5, x + w - 5, y + h - 5);
        g2.drawLine(x + w - 5, y + 5, x + 5, y + h - 5);
    }

    private static void drawChair(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillOval(x, y, w, h);
        g2.setColor(s);
        g2.drawOval(x, y, w, h);
        // Backrest arc
        g2.drawArc(x + 2, y + 2, w - 4, h - 4, 0, 180);
    }

    private static void drawPlant(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillOval(x, y, w, h);
        g2.setColor(s);
        // Leaves (star pattern)
        int cx = x + w / 2;
        int cy = y + h / 2;
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4;
            double r1 = Math.min(w, h) / 2;
            g2.drawLine(cx, cy, (int) (cx + Math.cos(ang) * r1), (int) (cy + Math.sin(ang) * r1));
        }
        g2.drawOval(x + w / 4, y + h / 4, w / 2, h / 2); // Pot circle
    }

    private static void drawLamp(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(new Color(s.getRed(), s.getGreen(), s.getBlue(), 80));
        g2.fillOval(x, y, w, h);
        g2.setColor(s);
        g2.drawOval(x, y, w, h);
        g2.drawOval(x + w / 3, y + h / 3, w / 3, h / 3); // Bulb
        // Rays
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4;
            g2.drawLine((int) (x + w / 2 + Math.cos(ang) * w / 6), (int) (y + h / 2 + Math.sin(ang) * h / 6),
                    (int) (x + w / 2 + Math.cos(ang) * w / 2), (int) (y + h / 2 + Math.sin(ang) * h / 2));
        }
    }

    private static void drawToilet(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRoundRect(x, y, w, h / 3, 4, 4); // Tank
        g2.fillOval(x + w / 6, y + h / 4, w * 2 / 3, h * 3 / 4); // Bowl
        g2.setColor(s);
        g2.drawRoundRect(x, y, w, h / 3, 4, 4);
        g2.drawOval(x + w / 6, y + h / 4, w * 2 / 3, h * 3 / 4);
    }

    private static void drawBathtub(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(s);
        g2.drawRoundRect(x, y, w, h, 12, 12);
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, 8, 8); // Interior rim
        g2.drawOval(x + w - 12, y + h / 2 - 3, 6, 6); // Drain
    }

    private static void drawSink(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRoundRect(x, y, w, h, 6, 6);
        g2.setColor(s);
        g2.drawRoundRect(x, y, w, h, 6, 6);
        g2.drawOval(x + w / 4, y + h / 4, w / 2, h / 2); // Basin
        g2.drawLine(x + w / 2, y, x + w / 2, y + 4); // Handle
    }

    private static void drawRug(Graphics2D g2, int x, int y, int w, int h, Color s, Color f) {
        g2.setColor(f);
        g2.fillRect(x, y, w, h);
        g2.setColor(s);
        g2.drawRect(x, y, w, h);
        // Fringes
        for (int i = 0; i < w; i += 6) {
            g2.drawLine(x + i, y, x + i, y - 2);
            g2.drawLine(x + i, y + h, x + i, y + h + 2);
        }
    }
}
