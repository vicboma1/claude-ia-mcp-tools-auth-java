package com.example.mcp;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, ToolInfo> tools;

    public ToolRegistry() {
        this.tools = new LinkedHashMap<>();
        registerTools();
    }

    private void registerTools() {
        addTool("get_user",
                "Get one user by ID.",
                toolInputSchema(
                    prop("user_id", "integer", "The user ID")
                ),
                new String[]{"user_id"}
        );

        addTool("list_users",
                "List all users.",
                new JsonObject(),
                new String[]{}
        );

        addTool("create_user",
                "Create a user.",
                toolInputSchema(
                    prop("name", "string", "User name"),
                    prop("email", "string", "User email")
                ),
                new String[]{"name", "email"}
        );

        addTool("update_user",
                "Update a user's name and/or email.",
                toolInputSchema(
                    prop("user_id", "integer", "The user ID"),
                    prop("name", "string", "New name (optional)"),
                    prop("email", "string", "New email (optional)")
                ),
                new String[]{"user_id"}
        );

        addTool("delete_user",
                "Delete a user.",
                toolInputSchema(
                    prop("user_id", "integer", "The user ID")
                ),
                new String[]{"user_id"}
        );
    }

    private void addTool(String name, String description, JsonObject inputSchema, String[] required) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", inputSchema);

        JsonArray requiredArray = new JsonArray();
        for (String req : required) {
            requiredArray.add(req);
        }
        schema.add("required", requiredArray);

        tools.put(name, new ToolInfo(name, description, schema));
    }

    private JsonObject toolInputSchema(JsonObject... properties) {
        JsonObject result = new JsonObject();
        for (JsonObject prop : properties) {
            String propName = prop.get("_name").getAsString();
            prop.remove("_name");
            result.add(propName, prop);
        }
        return result;
    }

    private JsonObject prop(String name, String type, String description) {
        JsonObject prop = new JsonObject();
        prop.addProperty("_name", name);
        prop.addProperty("type", type);
        prop.addProperty("description", description);
        return prop;
    }

    public Map<String, ToolInfo> getTools() {
        return tools;
    }

    public ToolInfo getTool(String name) {
        return tools.get(name);
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    public static class ToolInfo {
        public final String name;
        public final String description;
        public final JsonObject inputSchema;

        public ToolInfo(String name, String description, JsonObject inputSchema) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
        }
    }
}
