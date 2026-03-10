package utils.commands;

import models.Furniture;
import utils.Command;
import java.awt.Color;

public class PropertyChangeCommand implements Command {
    private Furniture furniture;
    // Old state
    private double oldW, oldD, oldH;
    private Color oldColor;
    // New state
    private double newW, newD, newH;
    private Color newColor;

    public PropertyChangeCommand(Furniture f, double oldW, double oldD, double oldH, Color oldColor,
            double newW, double newD, double newH, Color newColor) {
        this.furniture = f;
        this.oldW = oldW;
        this.oldD = oldD;
        this.oldH = oldH;
        this.oldColor = oldColor;
        this.newW = newW;
        this.newD = newD;
        this.newH = newH;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        furniture.setWidth(newW);
        furniture.setDepth(newD);
        furniture.setHeight(newH);
        furniture.setColor(newColor);
    }

    @Override
    public void undo() {
        furniture.setWidth(oldW);
        furniture.setDepth(oldD);
        furniture.setHeight(oldH);
        furniture.setColor(oldColor);
    }
}
