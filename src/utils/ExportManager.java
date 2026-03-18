package utils;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.imageio.ImageIO;
import models.Furniture;
import models.Room;


/**
 * Utility class responsible for exporting room designs from the RoomCraft application.
 * <p>
 * Provides two primary export features:
 * <ul>
 *   <li>High-resolution PNG rendering of the 2D floor plan with measurements and labels</li>
 *   <li>HTML-formatted Bill of Materials (BOM) report listing all placed furniture</li>
 * </ul>
 * 
 * @author [10953373 Udawaththa Wijegunawardhana]
 * @version 2.0
 * @since PUSL3122 Group Project
 */
public class ExportManager {

    /**
     * Exports the current room layout as a high-resolution PNG image.
     * <p>
     * The generated image includes:
     * <ul>
     *   <li>Branded title bar with room dimensions and area</li>
     *   <li>Scaled and centered room floor with selected color</li>
     *   <li>50 cm reference grid</li>
     *   <li>Furniture items with rotation support and centered labels</li>
     *   <li>Dimension lines with arrows and labels for width and depth</li>
     * </ul>
     *
     * @param room the Room object containing layout and furniture information
     * @param file target file for saving the PNG image
     * @throws IOException if an error occurs during image writing
     */
    public static void exportToPNG(Room room, File file) throws IOException {


        // Export resolution (chosen to balance quality and file size)
        int W = 1600, H = 1200;

        
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        // Enable high-quality rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background 
        g2.setColor(new Color(248, 248, 252));
        g2.fillRect(0, 0, W, H);

        // Thin outer frame
        g2.setColor(new Color(200, 205, 220));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(2, 2, W - 4, H - 4);

        // Header section
        g2.setColor(new Color(20, 30, 65));
        g2.fillRect(0, 0, W, 55);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.drawString("RoomCraft Designer — Floor Plan", 20, 36);

        // Room metadata
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        String info = String.format("Room: %.1fm × %.1fm  |  Area: %.1f m²",
                room.getWidth() / 100.0, room.getDepth() / 100.0, room.getAreaM2());
        g2.drawString(info, W - g2.getFontMetrics().stringWidth(info) - 20, 36);

        // Compute scaling to fit room inside safe drawing area
        int pad = 80;
        double maxW = W - pad * 2;
        double maxH = H - 55 - pad * 2;
        double scale = Math.min(maxW / room.getWidth(), maxH / room.getDepth());

        double offX = (W - room.getWidth() * scale) / 2.0;
        double offY = 55 + (H - 55 - room.getDepth() * scale) / 2.0;

        // Apply view transformation
        g2.translate(offX, offY);
        g2.scale(scale, scale);

        //  Draw floor 
        g2.setColor(room.getFloorColor());
        g2.fillRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // 50 cm reference grid
        g2.setColor(new Color(0, 0, 0, 18));
        g2.setStroke(new BasicStroke(0.5f));
        for (double x = 0; x <= room.getWidth(); x += 50) {
            g2.draw(new Line2D.Double(x, 0, x, room.getDepth()));
        }
        for (double y = 0; y <= room.getDepth(); y += 50) {
            g2.draw(new Line2D.Double(0, y, room.getWidth(), y));
        }

        // Room boundary
        g2.setColor(new Color(30, 40, 80));
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(0, 0, (int) room.getWidth(), (int) room.getDepth());

        // Render each furniture item
        for (Furniture f : room.getFurnitureList()) {
            double cx = f.getX() + f.getWidth() / 2.0;
            double cy = f.getY() + f.getDepth() / 2.0;

            AffineTransform old = g2.getTransform();

            g2.rotate(Math.toRadians(f.getRotation()), cx, cy);

            // Furniture fill
            g2.setColor(f.getColor());
            g2.fill(new Rectangle2D.Double(f.getX(), f.getY(), f.getWidth(), f.getDepth()));

            // Furniture outline
            g2.setColor(f.getColor().darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Rectangle2D.Double(f.getX(), f.getY(), f.getWidth(), f.getDepth()));

            // Centered label
            g2.setColor(Color.WHITE);
            Font labelFont = new Font("Segoe UI", Font.BOLD, (int) Math.max(6, Math.min(14, f.getWidth() / 12)));
            g2.setFont(labelFont);

            FontMetrics fm = g2.getFontMetrics();
            String label = f.getName();

            g2.drawString(label,
                    (float) (f.getX() + (f.getWidth() - fm.stringWidth(label)) / 2.0),
                    (float) (f.getY() + f.getDepth() / 2.0 + fm.getAscent() / 2.0 - fm.getDescent()));
            
            g2.setTransform(old);
        }

        // Restore identity + view transform to draw dimension annotations
        g2.setTransform(new AffineTransform());
        g2.translate(offX, offY);
        g2.scale(scale, scale);

        // Horizontal dimension (top)
        drawDimArrow(g2, 0, -30, room.getWidth(), -30,
                String.format("%.0f cm (%.1f m)", room.getWidth(), room.getWidth() / 100.0), true);
        
        // Vertical dimension (left)
        drawDimArrow(g2, -30, 0, -30, room.getDepth(),
                String.format("%.0f cm", room.getDepth()), false);

        g2.dispose();
        ImageIO.write(img, "PNG", file);
    }

    /**
     * Draws a dimension line with arrowheads and centered label.
     * Supports both horizontal and vertical orientations.
     *
     * @param g2d    graphics context
     * @param x1     start x
     * @param y1     start y
     * @param x2     end x
     * @param y2     end y
     * @param label  measurement text
     * @param isHorizontal whether the dimension is horizontal
     */

    private static void drawDimArrow(Graphics2D g2, double x1, double y1,
            double x2, double y2, String label, boolean horiz) {
        g2.setColor(new Color(50, 100, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Line2D.Double(x1, y1, x2, y2));


        // Arrowheads
        if (horiz) {
            g2.draw(new Line2D.Double(x1, y1, x1 + 8, y1 - 4));
            g2.draw(new Line2D.Double(x1, y1, x1 + 8, y1 + 4));
            g2.draw(new Line2D.Double(x2, y2, x2 - 8, y2 - 4));
            g2.draw(new Line2D.Double(x2, y2, x2 - 8, y2 + 4));
        } else {
            g2.draw(new Line2D.Double(x1, y1, x1 - 4, y1 + 8));
            g2.draw(new Line2D.Double(x1, y1, x1 + 4, y1 + 8));
            g2.draw(new Line2D.Double(x2, y2, x2 - 4, y2 - 8));
            g2.draw(new Line2D.Double(x2, y2, x2 + 4, y2 - 8));
        }
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        if (horiz)
            g2.drawString(label, (float) ((x1 + x2) / 2 - fm.stringWidth(label) / 2.0), (float) (y1 - 4));
        else {
            AffineTransform old = g2.getTransform();
            g2.rotate(-Math.PI / 2, (x1 + x2) / 2, (y1 + y2) / 2);
            g2.drawString(label, (float) ((x1 + x2) / 2 - fm.stringWidth(label) / 2.0), (float) ((y1 + y2) / 2 - 4));
            g2.setTransform(old);
        }
    }

    /**
     * Exports a Bill of Materials (BOM) report in well-formatted HTML.
     * <p>
     * The report includes:
     * <ul>
     *   <li>Room dimensions and calculated floor area</li>
     *   <li>Tabular list of all furniture items with type, material, size, position, rotation</li>
     *   <li>Color preview swatch for each item</li>
     *   <li>Total item count</li>
     * </ul>
     *
     * @param room room model containing the furniture list
     * @param file destination HTML file
     * @throws IOException if file writing fails
     */
    public static void exportBillOfMaterials(Room room, File file) throws IOException {
        List<Furniture> furniture = room.getFurnitureList();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang='en'><head><meta charset='UTF-8'>");
            pw.println("<title>RoomCraft — Bill of Materials</title>");
            pw.println("<style>");
            pw.println("body{font-family:'Segoe UI',sans-serif;margin:40px;color:#222;background:#f8f8fc}");
            pw.println("h1{color:#2c3e6b;border-bottom:2px solid #6cb3e0;padding-bottom:10px}");
            pw.println(".meta{color:#666;margin-bottom:24px}");
            pw.println(
                    "table{border-collapse:collapse;width:100%;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px #0001}");
            pw.println("th{background:#2c3e6b;color:#fff;padding:12px 16px;text-align:left}");
            pw.println("td{padding:10px 16px;border-bottom:1px solid #eee}");
            pw.println("tr:last-child td{border-bottom:none}");
            pw.println("tr:nth-child(even) td{background:#f4f6fb}");
            pw.println(".total{font-weight:bold;color:#2c3e6b;margin-top:16px}");
            pw.println("</style></head><body>");

            pw.println("<h1>\uD83C\uDFE0 RoomCraft Designer — Bill of Materials</h1>");
            pw.printf("<div class='meta'><b>Room:</b> %.0f cm &times; %.0f cm &times; %.0f cm &nbsp;|&nbsp; " +
                    "<b>Area:</b> %.1f m&sup2;</div>%n",
                    room.getWidth(), room.getDepth(), room.getHeight(), room.getAreaM2());

            pw.println("<table>");
            pw.println("<tr><th>#</th><th>Item</th><th>Type</th><th>Material</th>" +
                    "<th>W &times; D &times; H (cm)</th><th>Position (cm)</th><th>Rotation</th></tr>");

            int i = 1;
            for (Furniture f : furniture) {
                String color = String.format("#%02x%02x%02x",
                        f.getColor().getRed(), f.getColor().getGreen(), f.getColor().getBlue());
                pw.printf(
                        "<tr><td>%d</td><td><span style='display:inline-block;width:12px;height:12px;background:%s;border-radius:2px;margin-right:8px'></span>%s</td>"
                                +
                                "<td>%s</td><td>%s</td><td>%.0f &times; %.0f &times; %.0f</td><td>%.0f, %.0f</td><td>%.0f&deg;</td></tr>%n",
                        i++, color, f.getName(),
                        f.getType().name().charAt(0) + f.getType().name().toLowerCase().substring(1).replace("_", " "),
                        f.getMaterial().name().charAt(0) + f.getMaterial().name().toLowerCase().substring(1),
                        f.getWidth(), f.getDepth(), f.getHeight(),
                        f.getX(), f.getY(), f.getRotation());
            }
            pw.println("</table>");
            pw.printf("<p class='total'>Total items: %d</p>%n", furniture.size());
            pw.println(
                    "<p style='color:#aaa;margin-top:40px;font-size:12px'>Generated by RoomCraft Designer 2.0 &mdash; PUSL3122 Group Project</p>");
            pw.println("</body></html>");
        }
    }
}
