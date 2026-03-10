package utils.commands;

import models.Furniture;
import models.Room;
import utils.Command;

public class AddFurnitureCommand implements Command {
    private Room room;
    private Furniture furniture;

    public AddFurnitureCommand(Room room, Furniture furniture) {
        this.room = room;
        this.furniture = furniture;
    }

    @Override
    public void execute() {
        room.addFurniture(furniture);
    }

    @Override
    public void undo() {
        room.removeFurniture(furniture);
    }
}
