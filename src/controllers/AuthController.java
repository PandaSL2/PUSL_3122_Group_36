package controllers;

import utils.SessionManager;
import utils.SupabaseClient;
import models.User;
import java.util.concurrent.CompletableFuture;

public class AuthController {
    private final SupabaseClient supabase;

    public AuthController() {
        this.supabase = new SupabaseClient();
    }

    /**
     * Login: query 'users' table for matching username + hashed password.
     * Returns true if exactly one row found.
     */
    public CompletableFuture<Boolean> login(String username, String password) {
        return supabase.login(username, password).thenApply(json -> {
            // A non-empty JSON array means the user was found
            if (json != null && json.startsWith("[") && json.contains("\"id\"")) {
                // Extract the user id (optional, just for session)
                String id = extractValue(json, "id");
                User user = new User(username, "");
                user.setToken(id != null ? id : username);
                SessionManager.getInstance().login(user);
                return true;
            }
            return false;
        });
    }

    /**
     * Register: insert a new row in 'users' table with username + hashed password.
     * Returns true on success.
     */
    public CompletableFuture<Boolean> register(String username, String password) {
        return supabase.register(username, password).thenApply(json -> {
            // Success response is a JSON array or object with the inserted row
            return json != null && !json.startsWith("ERROR") && json.contains("\"id\"");
        });
    }

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1)
            return null;
        start += search.length();
        // Handle quoted and unquoted values
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start);
            if (end == -1)
                end = json.indexOf("}", start);
            if (end == -1)
                return null;
            return json.substring(start, end).trim();
        }
    }
}
