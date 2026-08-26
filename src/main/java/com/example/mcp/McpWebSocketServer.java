package com.example.mcp;

import com.example.api.ApiClient;
import com.example.business.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class McpWebSocketServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(McpWebSocketServer.class);
    private final McpServer mcpServer;
    private final Gson gson = new Gson();
    private static int port;

    public McpWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        McpWebSocketServer.port = port;
        ApiClient apiClient = new ApiClient();
        UserService userService = new UserService(apiClient);
        this.mcpServer = new McpServer(userService);
    }

    @Override
    public void onStart() {
        logger.info("MCP WebSocket Server started on port {}", this.getPort());
        logger.info("WebSocket: ws://localhost:{}", this.getPort());
        logger.info("Health Check: http://localhost:{}/health", this.getPort() + 1);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("Client connected: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Client disconnected: {} ({})", conn.getRemoteSocketAddress(), reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            logger.debug("Received message: {}", message);

            if (message.trim().isEmpty()) {
                return;
            }

            JsonObject request = gson.fromJson(message, JsonObject.class);
            JsonObject response = mcpServer.handleRequest(request);

            conn.send(gson.toJson(response));
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("jsonrpc", "2.0");
            JsonObject error = new JsonObject();
            error.addProperty("code", -32603);
            error.addProperty("message", "Internal error: " + e.getMessage());
            errorResponse.add("error", error);
            conn.send(gson.toJson(errorResponse));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    private static void startHttpHealthCheckServer(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port + 1);
                logger.info("HTTP Health Check Server listening on port {}", port + 1);

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        try (
                            InputStream in = socket.getInputStream();
                            OutputStream out = socket.getOutputStream()
                        ) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            String line = reader.readLine();

                            if (line != null && line.contains("GET /health")) {
                                String response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: 2\r\n" +
                                    "\r\n" +
                                    "OK";
                                out.write(response.getBytes(StandardCharsets.UTF_8));
                                out.flush();
                            }
                        } catch (Exception e) {
                            logger.error("Health check server error: {}", e.getMessage());
                        }
                    }).start();
                }
            } catch (Exception e) {
                logger.error("Failed to start health check server: {}", e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        int wsPort = 8080;
        if (args.length > 0) {
            try {
                wsPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port argument, using default: {}", wsPort);
            }
        }

        logger.info("Starting MCP Server...");
        logger.info("WebSocket Port: {}", wsPort);

        McpWebSocketServer server = new McpWebSocketServer(wsPort);
        try {
            server.start();
            startHttpHealthCheckServer(wsPort);
            logger.info("MCP Server fully operational");
        } catch (Exception e) {
            logger.error("Failed to start server: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
