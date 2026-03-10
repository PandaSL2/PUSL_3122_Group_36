package utils.commands;

import models.Furniture;
import models.Room;
import utils.Command;

public class RemoveFurnitureCommand implements Command {
    private Room room;
    private Furniture furniture;

    public RemoveFurnitureCommand(Room room, Furniture furniture) {
        this.room = room;
        this.furniture = furniture;
    }

    @Override
    public void execute() {
        room.removeFurniture(furniture);
    }

    @Override
    public void undo() {
        room.addFurniture(furniture);
    }
}
