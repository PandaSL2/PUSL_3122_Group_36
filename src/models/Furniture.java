package models;

import java.awt.Color;
import java.io.Serializable;

/**
 * Furniture — Data model for a furniture item.
 * Extended with: material field, expanded Type enum (LAMP, PLANT, WARDROBE,
 * etc.)
 * MEMBER 3 CONTRIBUTION: Furniture System Expansion
 */
public class Furniture implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        // Seating & Tables
        CHAIR, TABLE, SOFA, DESK,
        // Storage
        CABINET, BOOKSHELF, WARDROBE,
        // Sleeping
        BED,
        // Flooring
        RUG,
        // Bathroom
        BATHTUB, TOILET, SINK,
        // Accessories
        LAMP, PLANT, TV_STAND, CURTAIN,
        // Structure
        DOOR, WINDOW,
        // Custom
        CUSTOM
    }

    public enum Material {
        WOOD, METAL, FABRIC, GLASS, PLASTIC, STONE
    }

    private String id;
    private String name;
    private double width;
    private double depth;
    private double height;
    private Color color;
    private Type type;
    private Material material;
    private double x, y;
    private double rotation; // degrees

    public Furniture(String name, double width, double depth, double height, Color color, Type type) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.color = color;
        this.type = type;
        this.material = Material.WOOD;
        this.rotation = 0;
    }

    // ── Getters & Setters ──
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    public Type getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material m) {
        material = m;
    }

    public double getX() {
        return x;
    }

    public void setX(double v) {
        x = v;
    }

    public double getY() {
        return y;
    }

    public void setY(double v) {
        y = v;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double r) {
        rotation = r;
    }

    public void setPosition(double px, double py) {
        x = px;
        y = py;
    }

    /** Returns a deep copy of this furniture. */
    public Furniture copy() {
        Furniture f = new Furniture(name, width, depth, height, new Color(color.getRGB()), type);
        f.material = material;
        f.x = x;
        f.y = y;
        f.rotation = rotation;
        return f;
    }
}
