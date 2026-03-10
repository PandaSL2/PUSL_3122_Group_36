package data;

import models.Furniture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Catalog {

    public static List<Furniture> getStandardCatalog() {
        List<Furniture> catalog = new ArrayList<>();

        catalog.add(new Furniture("Standard Chair", 50, 50, 100, new Color(139, 69, 19), Furniture.Type.CHAIR));
        catalog.add(new Furniture("Dining Table", 120, 80, 75, new Color(245, 245, 220), Furniture.Type.TABLE));
        catalog.add(new Furniture("Cozy Sofa", 200, 80, 90, new Color(70, 130, 180), Furniture.Type.SOFA));
        catalog.add(new Furniture("Bed (Queen)", 150, 200, 60, new Color(255, 250, 250), Furniture.Type.BED));
        catalog.add(new Furniture("Wardrobe", 100, 60, 200, new Color(100, 50, 50), Furniture.Type.CABINET));
        catalog.add(new Furniture("Study Desk", 120, 60, 75, new Color(210, 180, 140), Furniture.Type.DESK));
        catalog.add(new Furniture("Bookshelf", 80, 30, 200, new Color(139, 69, 19), Furniture.Type.BOOKSHELF));
        catalog.add(new Furniture("Persian Rug", 200, 150, 1, new Color(178, 34, 34), Furniture.Type.RUG));

        return catalog;
    }
}
