package utils;

import models.Room;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static void saveDesign(models.Room room, String username, String designName) throws IOException {
        // Local backup
        File userDir = new File("data/users/" + username);
        if (!userDir.exists())
            userDir.mkdirs();
        File file = new File(userDir, designName + ".dat");
        saveDesign(room, file);

        // Supabase Sync (placeholder - in a real app would call SupabaseClient)
        // System.out.println("Syncing design to Supabase: " + designName);
    }

    public static List<String> getUserDesigns(String username) {
        File userDir = new File("data/users/" + username);
        if (!userDir.exists())
            return new ArrayList<>();

        String[] files = userDir.list((dir, name) -> name.endsWith(".dat"));
        List<String> designs = new ArrayList<>();
        if (files != null) {
            for (String f : files) {
                designs.add(f.replace(".dat", ""));
            }
        }
        return designs;
    }

    public static Room loadDesign(String username, String designName) throws IOException, ClassNotFoundException {
        File file = new File("data/users/" + username, designName + ".dat");
        return loadDesign(file);
    }

    public static void deleteDesign(String username, String designName) {
        File file = new File("data/users/" + username, designName + ".dat");
        if (file.exists())
            file.delete();
    }

    public static void saveDesign(Room room, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(room);
        }
    }

    public static Room loadDesign(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Room) ois.readObject();
        }
    }
}
