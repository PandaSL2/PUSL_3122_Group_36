package controllers;

import models.Room;
import models.Furniture;
import utils.CommandManager;
import utils.commands.AddFurnitureCommand;
import utils.commands.RemoveFurnitureCommand;
import utils.Command;

public class DesignController {
    private Room currentRoom;
    private CommandManager commandManager;

    public DesignController() {
        this.commandManager = new CommandManager();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void createNewRoom(double width, double depth, double height) {
        this.currentRoom = new Room(width, depth, height);
        // Clear history on new room
        // commandManager = new CommandManager(); // Or clear()
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void addFurniture(Furniture f) {
        if (currentRoom != null) {
            Command cmd = new AddFurnitureCommand(currentRoom, f);
            commandManager.executeCommand(cmd);
        }
    }

    public void removeFurniture(Furniture f) {
        if (currentRoom != null) {
            Command cmd = new RemoveFurnitureCommand(currentRoom, f);
            commandManager.executeCommand(cmd);
        }
    }

    public void executeCommand(Command cmd) {
        commandManager.executeCommand(cmd);
    }

    public void undo() {
        commandManager.undo();
    }

    public void redo() {
        commandManager.redo();
    }
}
