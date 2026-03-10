package utils.commands;

import models.Furniture;
import utils.Command;

public class MoveFurnitureCommand implements Command {
    private Furniture furniture;
    private double oldX, oldY;
    private double newX, newY;

    public MoveFurnitureCommand(Furniture furniture, double oldX, double oldY, double newX, double newY) {
        this.furniture = furniture;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute() {
        furniture.setPosition(newX, newY);
    }

    @Override
    public void undo() {
        furniture.setPosition(oldX, oldY);
    }
}
