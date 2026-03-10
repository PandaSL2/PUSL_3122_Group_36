package data;

import models.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;
    private List<User> users;
    private final String DATA_FILE = "data/users.json";

    private DataStore() {
        users = new ArrayList<>();
        loadUsers();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public User getUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // Simple manual JSON parser/writer
    private void loadUsers() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);

            String content = json.toString().trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1); // Remove outer brackets
                if (content.isEmpty()) return;
                
                // Very basic split by objects
                String[] objects = content.split("\\},\\{"); // Not robust but works for simple case
                
                for (String obj : objects) {
                    obj = obj.replace("{", "").replace("}", "").replace("\"", "");
                    String[] fields = obj.split(",");
                    String username = "";
                    String password = "";

                    for (String field : fields) {
                        String[] pair = field.split(":");
                        if (pair.length == 2) {
                            if (pair[0].trim().equals("username")) username = pair[1].trim();
                            if (pair[0].trim().equals("password")) password = pair[1].trim();
                        }
                    }
                    if (!username.isEmpty()) users.add(new User(username, password));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            writer.write("[");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                writer.write(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", u.getUsername(), u.getPassword()));
                if (i < users.size() - 1) writer.write(",");
            }
            writer.write("]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
