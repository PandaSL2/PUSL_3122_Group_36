package utils;

import models.Furniture;
import models.Room;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Improvements to user interaction and smoother UI behavior
 * Used to furniture to walls, edges of the room and other nearby furniture items.
 */
public class SnapHelper {

    public static final double SNAP_THRESHOLD = 15.0; // cm
    public static final double WALL_THRESHOLD = 12.0; // cm

    /** Data used to draw guide lines on the canvas */
    public record GuideLine(double x1, double y1, double x2, double y2, Color color) {
    }

    private final List<GuideLine> guideLines = new ArrayList<>();

    public List<GuideLine> getGuideLines() {
        return guideLines;
    }

    /**
     * Snaps furniture to walls or nearby items and returns the guide lines
     */
    public List<GuideLine> snap(Furniture f, Room room) {
        guideLines.clear();

        double x = f.getX(), y = f.getY();
        double w = f.getWidth(), d = f.getDepth();
        double roomW = room.getWidth(), roomD = room.getDepth();

        // Snap furniture to room walls
        // Left wall check
        if (Math.abs(x) < WALL_THRESHOLD) {
            x = 0;
            guideLines.add(new GuideLine(0, 0, 0, roomD, new Color(99, 179, 237, 175)));
        }
        // For the right wall
        if (Math.abs(x + w - roomW) < WALL_THRESHOLD) {
            x = roomW - w;
            guideLines.add(new GuideLine(roomW, 0, roomW, roomD, new Color(99, 179, 237, 175)));
        }
        // For the Top wall
        if (Math.abs(y) < WALL_THRESHOLD) {
            y = 0;
            guideLines.add(new GuideLine(0, 0, roomW, 0, new Color(99, 179, 237, 175)));
        }
        // For the Bottom wall
        if (Math.abs(y + d - roomD) < WALL_THRESHOLD) {
            y = roomD - d;
            guideLines.add(new GuideLine(0, roomD, roomW, roomD, new Color(99, 179, 237, 175)));
        }

        // Align with other furniture
        for (Furniture other : room.getFurnitureList()) {
            if (other == f)
                continue;

            double ox = other.getX(), oy = other.getY();
            double ow = other.getWidth(), od = other.getDepth();

            // Left edge alignment
            if (Math.abs(x - ox) < SNAP_THRESHOLD) {
                x = ox;
                guideLines.add(new GuideLine(ox, Math.min(y, oy), ox, Math.max(y + d, oy + od),
                        new Color(236, 72, 153, 175)));
            }
            // right edge alignment
            if (Math.abs(x + w - (ox + ow)) < SNAP_THRESHOLD) {
                x = ox + ow - w;
                guideLines.add(new GuideLine(ox + ow, Math.min(y, oy), ox + ow, Math.max(y + d, oy + od),
                        new Color(236, 72, 153, 175)));
            }
            // top edge alignment
            if (Math.abs(y - oy) < SNAP_THRESHOLD) {
                y = oy;
                guideLines.add(new GuideLine(Math.min(x, ox), oy, Math.max(x + w, ox + ow), oy,
                        new Color(236, 72, 153, 175)));
            }
            // bottom edge alignment
            if (Math.abs(y + d - (oy + od)) < SNAP_THRESHOLD) {
                y = oy + od - d;
                guideLines.add(new GuideLine(Math.min(x, ox), oy + od, Math.max(x + w, ox + ow), oy + od,
                        new Color(236, 72, 153, 175)));
            }
            // Snap to right side of other furniture
            if (Math.abs(x - (ox + ow)) < SNAP_THRESHOLD) {
                x = ox + ow;
                guideLines.add(new GuideLine(ox + ow, Math.min(y, oy), ox + ow, Math.max(y + d, oy + od),
                        new Color(99, 220, 130, 175)));
            }
            // Snap to left side of other furniture
            if (Math.abs(x + w - ox) < SNAP_THRESHOLD) {
                x = ox - w;
                guideLines.add(new GuideLine(ox, Math.min(y, oy), ox, Math.max(y + d, oy + od),
                        new Color(99, 220, 130, 175)));
            }
        }

        // Make sure furniture stays inside the room
        x = Math.max(0, Math.min(roomW - w, x));
        y = Math.max(0, Math.min(roomD - d, y));

        f.setX(x);
        f.setY(y);
        return guideLines;
    }
}
