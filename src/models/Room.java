package models;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Room — Data model for a room design.
 * Extended with: wallColor, ceilingColor, roomType.
 * MEMBER 4 CONTRIBUTION: Room Templates & Multi-Room Support
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Shape {
        RECTANGLE, L_SHAPE
    }

    private double width;
    private double depth;
    private double height;
    private Shape shape;
    private Color wallColor;
    private Color floorColor;
    private Color ceilingColor;
    private String roomType; // e.g. "LIVING_ROOM", "BEDROOM", "KITCHEN", "BATHROOM", "OFFICE"
    private List<Furniture> furnitureList;
    private String name;

    public Room(double width, double depth, double height) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.shape = Shape.RECTANGLE;
        this.furnitureList = new ArrayList<>();
        this.wallColor = new Color(230, 230, 240);
        this.floorColor = new Color(200, 180, 150);
        this.ceilingColor = new Color(250, 250, 255);
        this.roomType = "CUSTOM";
        this.name = "Untitled Room";
    }

    public void addFurniture(Furniture f) {
        furnitureList.add(f);
    }

    public void removeFurniture(Furniture f) {
        furnitureList.remove(f);
    }

    // ── Getters & Setters ──
    public double getWidth() {
        return width;
    }

    public void setWidth(double v) {
        width = v;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double v) {
        depth = v;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double v) {
        height = v;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape s) {
        shape = s;
    }

    public Color getWallColor() {
        return wallColor;
    }

    public void setWallColor(Color c) {
        wallColor = c;
    }

    public Color getFloorColor() {
        return floorColor;
    }

    public void setFloorColor(Color c) {
        floorColor = c;
    }

    public Color getCeilingColor() {
        return ceilingColor;
    }

    public void setCeilingColor(Color c) {
        ceilingColor = c;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String t) {
        roomType = t;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public List<Furniture> getFurnitureList() {
        return furnitureList;
    }

    /** Area in m² */
    public double getAreaM2() {
        return (width / 100.0) * (depth / 100.0);
    }
}
