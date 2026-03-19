package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * SupabaseClient — REST client that uses a custom 'users' table.
 * No email required: just username + password (SHA-256 hashed).
 * No external dependencies.
 */
public class SupabaseClient {
    private static final String URL = "https://urwhdyuugvbadojrxbdh.supabase.co";
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVyd2hkeXV1"
            +
            "Z3ZiYWRvanJ4YmRoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzIzNjM3ODYsImV4cCI6MjA4NzkzOTc4Nn" +
            "0.64RysWWe1jfWf1pYmorzOieIqS9sf0PZs2GOS33ZV2Q";

    private final HttpClient http;

    public SupabaseClient() {
        http = HttpClient.newBuilder().build();
    }

    // ── Register: INSERT a new user row ────────────────────────────────────
    public CompletableFuture<String> register(String username, String password) {
        String hash = sha256(password);
        String json = String.format(
                "{\"username\":\"%s\",\"password_hash\":\"%s\"}",
                escapeJson(username), hash);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/rest/v1/users"))
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer " + ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> {
                    if (r.statusCode() == 201 || r.statusCode() == 200)
                        return r.body();
                    return "ERROR:" + r.statusCode() + " " + r.body();
                });
    }

    // ── Login: SELECT row matching username + password_hash ────────────────
    public CompletableFuture<String> login(String username, String password) {
        String hash = sha256(password);
        String query = String.format(
                "?username=eq.%s&password_hash=eq.%s&select=id,username",
                urlEncode(username), urlEncode(hash));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/rest/v1/users" + query))
                .header("apikey", ANON_KEY)
                .header("Authorization", "Bearer " + ANON_KEY)
                .header("Accept", "application/json")
                .GET()
                .build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // ── SHA-256 hash (no external libs) ───────────────────────────────────
    private String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return input; // fallback (should never happen)
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
