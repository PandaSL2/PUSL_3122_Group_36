package utils.commands;

import models.Furniture;
import utils.Command;

public class RotateFurnitureCommand implements Command {
    private Furniture furniture;
    private double oldRotation;
    private double newRotation;

    public RotateFurnitureCommand(Furniture furniture, double oldRotation, double newRotation) {
        this.furniture = furniture;
        this.oldRotation = oldRotation;
        this.newRotation = newRotation;
    }

    @Override
    public void execute() {
        furniture.setRotation(newRotation);
    }

    @Override
    public void undo() {
        furniture.setRotation(oldRotation);
    }
}
