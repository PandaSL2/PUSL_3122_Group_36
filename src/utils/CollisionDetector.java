package utils;

import models.Furniture;
import models.Room;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Improvements to user interaction and smoother UI behavior
 * Checks for overlap between furniture items.
 */
public class CollisionDetector {

    /**
     * Checks overlapping furniture with the moving item
     * using rotated bounding boxes
     */
    public static List<Furniture> getCollisions(Furniture moving, Room room) {
        List<Furniture> collisions = new ArrayList<>();
        Area movingArea = getArea(moving);

        for (Furniture f : room.getFurnitureList()) {
            if (f == moving)
                continue;
            Area other = getArea(f);
            Area intersection = new Area(movingArea);
            intersection.intersect(other);
            if (!intersection.isEmpty()) {
                collisions.add(f);
            }
        }
        return collisions;
    }

    /**
     * Returns true if `moving` overlaps any other furniture.
     */
    public static boolean hasCollision(Furniture moving, Room room) {
        return !getCollisions(moving, room).isEmpty();
    }

    /**
     * Checks if the moving item overlaps with other furniture
     */
    public static boolean isWithinRoom(Furniture f, Room room) {
        Area fa = getArea(f);
        Area roomArea = new Area(new Rectangle2D.Double(0, 0, room.getWidth(), room.getDepth()));
        Area combined = new Area(fa);
        combined.subtract(roomArea);
        return combined.isEmpty();
    }

    private static Area getArea(Furniture f) {
        Rectangle2D.Double rect = new Rectangle2D.Double(
                f.getX(), f.getY(), f.getWidth(), f.getDepth());
        Area area = new Area(rect);

        if (f.getRotation() != 0) {
            double cx = f.getX() + f.getWidth() / 2.0;
            double cy = f.getY() + f.getDepth() / 2.0;
            AffineTransform rot = AffineTransform.getRotateInstance(
                    Math.toRadians(f.getRotation()), cx, cy);
            area = area.createTransformedArea(rot);
        }
        return area;
    }
}
