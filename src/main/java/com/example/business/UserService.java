package com.example.business;

import com.example.api.ApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private final ApiClient api;

    public UserService(ApiClient api) {
        this.api = api;
    }

    public Map<String, Object> getUser(int userId) throws IOException {
        if (userId <= 0) {
            throw new IllegalArgumentException("user_id must be greater than zero");
        }
        Map<String, Object> user = api.getUser(userId);
        return normalizeUser(user);
    }

    public List<Map<String, Object>> listUsers() throws IOException {
        List<Map<String, Object>> users = api.listUsers();
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> user : users) {
            normalized.add(normalizeUser(user));
        }
        return normalized;
    }

    public Map<String, Object> createUser(String name, String email) throws IOException {
        name = name.trim();
        email = email.trim().toLowerCase();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("email is invalid");
        }

        Map<String, Object> user = api.createUser(name, email);
        return normalizeUser(user);
    }

    public Map<String, Object> updateUser(int userId, String name, String email) throws IOException {
        if (userId <= 0) {
            throw new IllegalArgumentException("user_id must be greater than zero");
        }
        if (name == null && email == null) {
            throw new IllegalArgumentException("at least one field must be supplied");
        }

        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name cannot be empty");
            }
        }

        if (email != null) {
            email = email.trim().toLowerCase();
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("email is invalid");
            }
        }

        Map<String, Object> user = api.updateUser(userId, name, email);
        return normalizeUser(user);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || email.contains(" ")) {
            return false;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return false;
        }
        int lastAtIndex = email.lastIndexOf('@');
        if (atIndex != lastAtIndex) {
            return false;
        }
        String domain = email.substring(atIndex + 1);
        return domain.contains(".");
    }

    public Map<String, Object> deleteUser(int userId) throws IOException {
        if (userId <= 0) {
            throw new IllegalArgumentException("user_id must be greater than zero");
        }
        api.deleteUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("user_id", userId);
        result.put("deleted", true);
        return result;
    }

    private Map<String, Object> normalizeUser(Map<String, Object> user) {
        Map<String, Object> normalized = new HashMap<>();
        normalized.put("id", user.get("id"));
        normalized.put("name", user.get("name"));
        normalized.put("email", user.get("email"));
        normalized.put("username", user.get("username"));
        return normalized;
    }
}
