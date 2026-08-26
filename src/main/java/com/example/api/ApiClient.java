package com.example.api;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public ApiClient() {
        this("https://jsonplaceholder.typicode.com");
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public Map<String, Object> getUser(int userId) throws IOException {
        String url = baseUrl + "/users/" + userId;
        Request request = new Request.Builder().url(url).build();
        return executeRequest(request);
    }

    public List<Map<String, Object>> listUsers() throws IOException {
        String url = baseUrl + "/users";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code());
            }
            String body = response.body().string();
            JsonArray array = gson.fromJson(body, JsonArray.class);
            return gson.fromJson(array, List.class);
        }
    }

    public Map<String, Object> createUser(String name, String email) throws IOException {
        String url = baseUrl + "/users";
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("email", email);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return executeRequest(request);
    }

    public Map<String, Object> updateUser(int userId, String name, String email) throws IOException {
        String url = baseUrl + "/users/" + userId;
        JsonObject json = new JsonObject();

        if (name != null) {
            json.addProperty("name", name);
        }
        if (email != null) {
            json.addProperty("email", email);
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();

        return executeRequest(request);
    }

    public boolean deleteUser(int userId) throws IOException {
        String url = baseUrl + "/users/" + userId;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code());
            }
            return true;
        }
    }

    private Map<String, Object> executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code());
            }
            String body = response.body().string();
            return gson.fromJson(body, Map.class);
        }
    }
}
