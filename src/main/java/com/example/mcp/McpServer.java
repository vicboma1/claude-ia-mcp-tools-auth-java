package com.example.mcp;

import com.example.api.ApiClient;
import com.example.auth.OAuthMiddleware;
import com.example.business.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class McpServer {
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);
    private static final Gson gson = new Gson();
    private final UserService userService;
    private final ToolRegistry toolRegistry;
    private final OAuthMiddleware oauthMiddleware;

    public McpServer(UserService userService) {
        this.userService = userService;
        this.toolRegistry = new ToolRegistry();
        this.oauthMiddleware = new OAuthMiddleware();
    }

    public void start() {
        logger.info("MCP Server starting on stdio...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    JsonObject request = gson.fromJson(line, JsonObject.class);
                    JsonObject response = handleRequest(request);
                    writer.write(gson.toJson(response));
                    writer.newLine();
                    writer.flush();
                } catch (com.google.gson.JsonSyntaxException e) {
                    logger.error("Invalid JSON: {}", e.getMessage());
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("jsonrpc", "2.0");
                    JsonObject error = new JsonObject();
                    error.addProperty("code", -32700);
                    error.addProperty("message", "Parse error");
                    errorResponse.add("error", error);
                    writer.write(gson.toJson(errorResponse));
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    logger.error("Unexpected error: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : null;
        int id = request.has("id") ? request.get("id").getAsInt() : 0;

        if ("initialize".equals(method)) {
            return handleInitialize(id);
        } else if ("tools/list".equals(method)) {
            return handleToolsList(id);
        } else if ("tools/call".equals(method)) {
            return handleToolCall(request, id);
        } else {
            return errorResponse(id, -32601, "Unknown method: " + method);
        }
    }

    private JsonObject handleInitialize(int id) {
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2024-11-05");

        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        result.add("capabilities", capabilities);

        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "example-users");
        serverInfo.addProperty("version", "1.0.0");
        result.add("serverInfo", serverInfo);

        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("result", result);
        response.addProperty("id", id);

        return response;
    }

    private JsonObject handleToolsList(int id) {
        JsonArray tools = new JsonArray();
        for (ToolRegistry.ToolInfo tool : toolRegistry.getTools().values()) {
            JsonObject toolObj = new JsonObject();
            toolObj.addProperty("name", tool.name);
            toolObj.addProperty("description", tool.description);
            toolObj.add("inputSchema", tool.inputSchema);
            tools.add(toolObj);
        }

        JsonObject result = new JsonObject();
        result.add("tools", tools);

        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("result", result);
        response.addProperty("id", id);

        return response;
    }

    private JsonObject handleToolCall(JsonObject request, int id) {
        JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();
        String toolName = params.has("name") ? params.get("name").getAsString() : null;
        JsonObject arguments = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();

        if (!toolRegistry.hasTool(toolName)) {
            return errorResponse(id, -32601, "Unknown tool: " + toolName);
        }

        try {
            Object result = callTool(toolName, arguments);
            JsonObject content = new JsonObject();
            content.addProperty("type", "text");
            content.addProperty("text", gson.toJson(result));

            JsonArray contentArray = new JsonArray();
            contentArray.add(content);

            JsonObject resultObj = new JsonObject();
            resultObj.add("content", contentArray);

            JsonObject response = new JsonObject();
            response.addProperty("jsonrpc", "2.0");
            response.add("result", resultObj);
            response.addProperty("id", id);

            return response;
        } catch (Exception e) {
            logger.error("Error calling tool {}: {}", toolName, e.getMessage(), e);
            return errorResponse(id, -32603, "Error: " + e.getMessage());
        }
    }

    private Object callTool(String toolName, JsonObject arguments) throws Exception {
        switch (toolName) {
            case "get_user": {
                int userId = arguments.get("user_id").getAsInt();
                return userService.getUser(userId);
            }
            case "list_users":
                return userService.listUsers();
            case "create_user": {
                String name = arguments.get("name").getAsString();
                String email = arguments.get("email").getAsString();
                return userService.createUser(name, email);
            }
            case "update_user": {
                int userId = arguments.get("user_id").getAsInt();
                String name = arguments.has("name") ? arguments.get("name").getAsString() : null;
                String email = arguments.has("email") ? arguments.get("email").getAsString() : null;
                return userService.updateUser(userId, name, email);
            }
            case "delete_user": {
                int userId = arguments.get("user_id").getAsInt();
                return userService.deleteUser(userId);
            }
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private JsonObject errorResponse(int id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("error", error);
        response.addProperty("id", id);

        return response;
    }

    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient();
        UserService userService = new UserService(apiClient);
        McpServer server = new McpServer(userService);
        server.start();
    }
}
