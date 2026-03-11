package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * FurnitureCatalog — Singleton with 30+ pre-defined furniture items in
 * categories.
 * MEMBER 3 CONTRIBUTION: Furniture System Expansion
 */
public class FurnitureCatalog {

    public static final String CAT_LIVING = "Living Room";
    public static final String CAT_BEDROOM = "Bedroom";
    public static final String CAT_KITCHEN = "Kitchen";
    public static final String CAT_BATHROOM = "Bathroom";
    public static final String CAT_OFFICE = "Office";
    public static final String CAT_DECOR = "Decor";

    private static FurnitureCatalog instance;
    private final List<CatalogEntry> entries = new ArrayList<>();

    public record CatalogEntry(String category, String name,
            Furniture.Type type, Furniture.Material material,
            double w, double d, double h, Color color) {
        public Furniture create() {
            Furniture f = new Furniture(name, w, d, h, color, type);
            f.setMaterial(material);
            return f;
        }
    }

    private FurnitureCatalog() {
        populate();
    }

    public static FurnitureCatalog getInstance() {
        if (instance == null)
            instance = new FurnitureCatalog();
        return instance;
    }

    private void populate() {
        // ── Living Room ──
        add(CAT_LIVING, "3-Seat Sofa", Furniture.Type.SOFA, Furniture.Material.FABRIC, 220, 90, 85,
                new Color(120, 90, 70));
        add(CAT_LIVING, "Loveseat", Furniture.Type.SOFA, Furniture.Material.FABRIC, 150, 85, 85,
                new Color(90, 110, 140));
        add(CAT_LIVING, "Armchair", Furniture.Type.CHAIR, Furniture.Material.FABRIC, 80, 80, 90,
                new Color(160, 120, 100));
        add(CAT_LIVING, "Coffee Table", Furniture.Type.TABLE, Furniture.Material.WOOD, 100, 55, 45,
                new Color(100, 75, 50));
        add(CAT_LIVING, "Side Table", Furniture.Type.TABLE, Furniture.Material.WOOD, 50, 50, 55,
                new Color(120, 90, 65));
        add(CAT_LIVING, "TV Stand", Furniture.Type.TV_STAND, Furniture.Material.WOOD, 180, 45, 55,
                new Color(60, 40, 30));
        add(CAT_LIVING, "Large Rug", Furniture.Type.RUG, Furniture.Material.FABRIC, 200, 140, 2,
                new Color(160, 100, 60));
        add(CAT_LIVING, "Floor Lamp", Furniture.Type.LAMP, Furniture.Material.METAL, 30, 30, 165,
                new Color(200, 180, 100));
        add(CAT_LIVING, "Bookshelf", Furniture.Type.BOOKSHELF, Furniture.Material.WOOD, 100, 35, 200,
                new Color(130, 100, 70));
        add(CAT_LIVING, "Floor Plant", Furniture.Type.PLANT, Furniture.Material.PLASTIC, 35, 35, 110,
                new Color(60, 120, 60));

        // ── Bedroom ──
        add(CAT_BEDROOM, "Single Bed", Furniture.Type.BED, Furniture.Material.WOOD, 100, 200, 55,
                new Color(180, 160, 200));
        add(CAT_BEDROOM, "Double Bed", Furniture.Type.BED, Furniture.Material.WOOD, 140, 200, 55,
                new Color(160, 140, 180));
        add(CAT_BEDROOM, "Queen Bed", Furniture.Type.BED, Furniture.Material.WOOD, 160, 210, 55,
                new Color(140, 120, 160));
        add(CAT_BEDROOM, "King Bed", Furniture.Type.BED, Furniture.Material.WOOD, 195, 215, 55,
                new Color(120, 100, 150));
        add(CAT_BEDROOM, "Bedside Table", Furniture.Type.DESK, Furniture.Material.WOOD, 55, 50, 65,
                new Color(140, 110, 80));
        add(CAT_BEDROOM, "Wardrobe (2-door)", Furniture.Type.WARDROBE, Furniture.Material.WOOD, 160, 60, 210,
                new Color(160, 130, 100));
        add(CAT_BEDROOM, "Wardrobe (4-door)", Furniture.Type.WARDROBE, Furniture.Material.WOOD, 240, 65, 215,
                new Color(150, 120, 95));
        add(CAT_BEDROOM, "Dressing Table", Furniture.Type.DESK, Furniture.Material.WOOD, 100, 50, 75,
                new Color(200, 180, 160));
        add(CAT_BEDROOM, "Bedroom Rug", Furniture.Type.RUG, Furniture.Material.FABRIC, 180, 120, 2,
                new Color(160, 140, 180));
        add(CAT_BEDROOM, "Table Lamp", Furniture.Type.LAMP, Furniture.Material.METAL, 22, 22, 50,
                new Color(220, 200, 150));

        // ── Kitchen ──
        add(CAT_KITCHEN, "Counter Top", Furniture.Type.CABINET, Furniture.Material.STONE, 200, 60, 90,
                new Color(240, 235, 225));
        add(CAT_KITCHEN, "Kitchen Island", Furniture.Type.TABLE, Furniture.Material.STONE, 120, 75, 90,
                new Color(200, 190, 175));
        add(CAT_KITCHEN, "Bar Stool", Furniture.Type.CHAIR, Furniture.Material.METAL, 45, 45, 75,
                new Color(60, 50, 40));
        add(CAT_KITCHEN, "Refrigerator", Furniture.Type.CABINET, Furniture.Material.METAL, 70, 75, 175,
                new Color(220, 220, 225));
        add(CAT_KITCHEN, "Dining Table", Furniture.Type.TABLE, Furniture.Material.WOOD, 160, 90, 75,
                new Color(140, 100, 70));
        add(CAT_KITCHEN, "Dining Chair", Furniture.Type.CHAIR, Furniture.Material.WOOD, 45, 50, 90,
                new Color(100, 75, 50));

        // ── Bathroom ──
        add(CAT_BATHROOM, "Bathtub", Furniture.Type.BATHTUB, Furniture.Material.STONE, 165, 75, 55,
                new Color(240, 240, 250));
        add(CAT_BATHROOM, "Walk-in Shower", Furniture.Type.BATHTUB, Furniture.Material.GLASS, 90, 90, 210,
                new Color(200, 220, 240));
        add(CAT_BATHROOM, "Toilet", Furniture.Type.TOILET, Furniture.Material.STONE, 65, 75, 75,
                new Color(245, 245, 245));
        add(CAT_BATHROOM, "Vanity Sink", Furniture.Type.SINK, Furniture.Material.STONE, 80, 55, 85,
                new Color(245, 245, 245));
        add(CAT_BATHROOM, "Storage Cabinet", Furniture.Type.CABINET, Furniture.Material.WOOD, 80, 40, 160,
                new Color(240, 235, 225));

        // ── Office ──
        add(CAT_OFFICE, "Executive Desk", Furniture.Type.DESK, Furniture.Material.WOOD, 180, 80, 75,
                new Color(80, 60, 40));
        add(CAT_OFFICE, "Office Chair", Furniture.Type.CHAIR, Furniture.Material.FABRIC, 65, 70, 115,
                new Color(30, 30, 40));
        add(CAT_OFFICE, "Office Bookshelf", Furniture.Type.BOOKSHELF, Furniture.Material.WOOD, 120, 35, 200,
                new Color(100, 80, 60));
        add(CAT_OFFICE, "Filing Cabinet", Furniture.Type.CABINET, Furniture.Material.METAL, 45, 60, 130,
                new Color(160, 160, 165));
        add(CAT_OFFICE, "Meeting Table", Furniture.Type.TABLE, Furniture.Material.WOOD, 200, 100, 75,
                new Color(120, 95, 70));

        // ── Decor ──
        add(CAT_DECOR, "Small Plant", Furniture.Type.PLANT, Furniture.Material.PLASTIC, 25, 25, 40,
                new Color(60, 140, 60));
        add(CAT_DECOR, "Tall Plant", Furniture.Type.PLANT, Furniture.Material.PLASTIC, 35, 35, 130,
                new Color(50, 130, 50));
        add(CAT_DECOR, "Desk Lamp", Furniture.Type.LAMP, Furniture.Material.METAL, 20, 20, 45, new Color(40, 100, 200));
        add(CAT_DECOR, "Ceiling Lamp", Furniture.Type.LAMP, Furniture.Material.METAL, 40, 40, 20,
                new Color(220, 210, 180));
    }

    private void add(String category, String name, Furniture.Type type, Furniture.Material mat,
            double w, double d, double h, Color color) {
        entries.add(new CatalogEntry(category, name, type, mat, w, d, h, color));
    }

    public List<CatalogEntry> getAll() {
        return entries;
    }

    public List<CatalogEntry> getByCategory(String category) {
        List<CatalogEntry> result = new ArrayList<>();
        for (CatalogEntry e : entries) {
            if (e.category().equals(category))
                result.add(e);
        }
        return result;
    }

    public List<CatalogEntry> search(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty())
            return getAll();
        List<CatalogEntry> result = new ArrayList<>();
        for (CatalogEntry e : entries) {
            if (e.name().toLowerCase().contains(q) || e.category().toLowerCase().contains(q))
                result.add(e);
        }
        return result;
    }

    public List<String> getCategories() {
        return List.of(CAT_LIVING, CAT_BEDROOM, CAT_KITCHEN, CAT_BATHROOM, CAT_OFFICE, CAT_DECOR);
    }
}
