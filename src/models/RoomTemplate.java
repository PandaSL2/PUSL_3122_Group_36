package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomTemplate — Pre-defined room configurations with furniture layouts.
 * MEMBER 4 CONTRIBUTION: Room Templates & Multi-Room Support
 */
public class RoomTemplate {

    public final String name;
    public final String displayName;
    public final double width;
    public final double depth;
    public final double height;
    public final Color wallColor;
    public final Color floorColor;
    public final Color ceilingColor;
    public final List<FurniturePlacement> placements;

    public record FurniturePlacement(Furniture.Type type, String label,
            double x, double y,
            double w, double d, double h,
            Color color, double rotation) {
    }

    private RoomTemplate(String name, String displayName,
            double width, double depth, double height,
            Color wall, Color floor, Color ceiling,
            List<FurniturePlacement> placements) {
        this.name = name;
        this.displayName = displayName;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.wallColor = wall;
        this.floorColor = floor;
        this.ceilingColor = ceiling;
        this.placements = placements;
    }

    /** Apply this template to a Room, clearing and populating furniture. */
    public Room createRoom() {
        Room room = new Room(width, depth, height);
        room.setRoomType(name);
        room.setWallColor(wallColor);
        room.setFloorColor(floorColor);
        room.setCeilingColor(ceilingColor);
        room.setName(displayName);
        for (FurniturePlacement p : placements) {
            Furniture f = new Furniture(p.label(), p.w(), p.d(), p.h(), p.color(), p.type());
            f.setPosition(p.x(), p.y());
            f.setRotation(p.rotation());
            room.addFurniture(f);
        }
        return room;
    }

    // ──────────────────────────────────────────────
    // Pre-Defined Templates
    // ──────────────────────────────────────────────

    public static List<RoomTemplate> getAllTemplates() {
        List<RoomTemplate> list = new ArrayList<>();
        list.add(livingRoom());
        list.add(masterBedroom());
        list.add(homeOffice());
        list.add(kitchen());
        list.add(bathroom());
        return list;
    }

    public static RoomTemplate livingRoom() {
        List<FurniturePlacement> p = new ArrayList<>();
        p.add(new FurniturePlacement(Furniture.Type.SOFA, "Sofa", 80, 50, 230, 90, 85, new Color(120, 90, 70), 0));
        p.add(new FurniturePlacement(Furniture.Type.TABLE, "Coffee Table", 150, 160, 100, 60, 45,
                new Color(100, 75, 50), 0));
        p.add(new FurniturePlacement(Furniture.Type.CHAIR, "Armchair", 60, 210, 75, 75, 90, new Color(90, 120, 160),
                0));
        p.add(new FurniturePlacement(Furniture.Type.TV_STAND, "TV Stand", 130, 320, 150, 45, 55, new Color(60, 40, 30),
                0));
        p.add(new FurniturePlacement(Furniture.Type.RUG, "Rug", 100, 130, 200, 140, 2, new Color(180, 120, 80), 0));
        p.add(new FurniturePlacement(Furniture.Type.PLANT, "Floor Plant", 30, 30, 35, 35, 110, new Color(60, 120, 60),
                0));
        p.add(new FurniturePlacement(Furniture.Type.LAMP, "Floor Lamp", 340, 40, 30, 30, 150, new Color(200, 180, 100),
                0));
        return new RoomTemplate("LIVING_ROOM", "Modern Living Room", 500, 400, 250,
                new Color(245, 240, 235), new Color(185, 155, 120), new Color(252, 252, 252), p);
    }

    public static RoomTemplate masterBedroom() {
        List<FurniturePlacement> p = new ArrayList<>();
        p.add(new FurniturePlacement(Furniture.Type.BED, "Queen Bed", 100, 60, 160, 210, 55, new Color(180, 160, 200),
                0));
        p.add(new FurniturePlacement(Furniture.Type.DESK, "Bedside Table L", 60, 60, 55, 55, 65,
                new Color(140, 110, 85), 0));
        p.add(new FurniturePlacement(Furniture.Type.DESK, "Bedside Table R", 275, 60, 55, 55, 65,
                new Color(140, 110, 85), 0));
        p.add(new FurniturePlacement(Furniture.Type.WARDROBE, "Wardrobe", 20, 300, 200, 60, 210,
                new Color(160, 130, 100), 0));
        p.add(new FurniturePlacement(Furniture.Type.DESK, "Dressing Table", 300, 280, 100, 50, 75,
                new Color(200, 180, 160), 0));
        p.add(new FurniturePlacement(Furniture.Type.CHAIR, "Accent Chair", 330, 180, 65, 65, 80,
                new Color(200, 180, 200), 0));
        p.add(new FurniturePlacement(Furniture.Type.LAMP, "Table Lamp L", 68, 60, 20, 20, 55, new Color(220, 200, 150),
                0));
        p.add(new FurniturePlacement(Furniture.Type.RUG, "Bedroom Rug", 90, 240, 220, 120, 2, new Color(160, 140, 180),
                0));
        return new RoomTemplate("BEDROOM", "Master Bedroom", 420, 400, 260,
                new Color(235, 228, 240), new Color(195, 175, 150), new Color(255, 255, 255), p);
    }

    public static RoomTemplate homeOffice() {
        List<FurniturePlacement> p = new ArrayList<>();
        p.add(new FurniturePlacement(Furniture.Type.DESK, "Main Desk", 50, 50, 180, 75, 75, new Color(100, 80, 60), 0));
        p.add(new FurniturePlacement(Furniture.Type.CHAIR, "Office Chair", 115, 140, 65, 70, 115, new Color(40, 40, 50),
                0));
        p.add(new FurniturePlacement(Furniture.Type.BOOKSHELF, "Bookshelf", 280, 40, 100, 35, 200,
                new Color(120, 95, 70), 0));
        p.add(new FurniturePlacement(Furniture.Type.BOOKSHELF, "Side Shelf", 40, 220, 35, 120, 180,
                new Color(120, 95, 70), 90));
        p.add(new FurniturePlacement(Furniture.Type.LAMP, "Desk Lamp", 200, 55, 20, 20, 45, new Color(40, 40, 200), 0));
        p.add(new FurniturePlacement(Furniture.Type.PLANT, "Desk Plant", 260, 55, 25, 25, 40, new Color(60, 140, 60),
                0));
        return new RoomTemplate("OFFICE", "Home Office", 380, 340, 250,
                new Color(235, 235, 245), new Color(170, 140, 110), new Color(252, 252, 255), p);
    }

    public static RoomTemplate kitchen() {
        List<FurniturePlacement> p = new ArrayList<>();
        p.add(new FurniturePlacement(Furniture.Type.CABINET, "Counter Top L", 30, 30, 200, 60, 90,
                new Color(240, 235, 225), 0));
        p.add(new FurniturePlacement(Furniture.Type.CABINET, "Counter Top R", 30, 200, 60, 200, 90,
                new Color(240, 235, 225), 0));
        p.add(new FurniturePlacement(Furniture.Type.TABLE, "Kitchen Island", 150, 130, 120, 70, 90,
                new Color(200, 190, 175), 0));
        p.add(new FurniturePlacement(Furniture.Type.CHAIR, "Bar Stool 1", 155, 215, 45, 45, 75, new Color(80, 60, 40),
                0));
        p.add(new FurniturePlacement(Furniture.Type.CHAIR, "Bar Stool 2", 215, 215, 45, 45, 75, new Color(80, 60, 40),
                0));
        return new RoomTemplate("KITCHEN", "Modern Kitchen", 360, 320, 250,
                new Color(240, 240, 240), new Color(210, 200, 185), new Color(255, 255, 255), p);
    }

    public static RoomTemplate bathroom() {
        List<FurniturePlacement> p = new ArrayList<>();
        p.add(new FurniturePlacement(Furniture.Type.BATHTUB, "Bathtub", 30, 30, 160, 75, 55, new Color(240, 240, 250),
                0));
        p.add(new FurniturePlacement(Furniture.Type.TOILET, "Toilet", 210, 30, 65, 75, 75, new Color(245, 245, 245),
                0));
        p.add(new FurniturePlacement(Furniture.Type.SINK, "Vanity Sink", 30, 165, 80, 55, 85, new Color(245, 245, 245),
                0));
        p.add(new FurniturePlacement(Furniture.Type.CABINET, "Storage Cabinet", 150, 165, 80, 40, 160,
                new Color(240, 235, 225), 0));
        return new RoomTemplate("BATHROOM", "Modern Bathroom", 300, 250, 240,
                new Color(240, 240, 245), new Color(220, 220, 225), new Color(255, 255, 255), p);
    }
}
