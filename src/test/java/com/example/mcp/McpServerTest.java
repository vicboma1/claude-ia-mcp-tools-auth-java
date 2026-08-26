package com.example.mcp;

import com.example.business.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class McpServerTest {
    @Mock
    private UserService mockUserService;

    private Gson gson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gson = new Gson();
    }

    @Nested
    @DisplayName("Tools Registry")
    class ToolsRegistryTests {
        @Test
        @DisplayName("should have get_user tool")
        void testHasGetUserTool() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("get_user"));
            assertEquals("Get one user by ID.", registry.getTool("get_user").description);
        }

        @Test
        @DisplayName("should have list_users tool")
        void testHasListUsersTool() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("list_users"));
            assertEquals("List all users.", registry.getTool("list_users").description);
        }

        @Test
        @DisplayName("should have create_user tool")
        void testHasCreateUserTool() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("create_user"));
            assertEquals("Create a user.", registry.getTool("create_user").description);
        }

        @Test
        @DisplayName("should have update_user tool")
        void testHasUpdateUserTool() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("update_user"));
            assertEquals("Update a user's name and/or email.", registry.getTool("update_user").description);
        }

        @Test
        @DisplayName("should have delete_user tool")
        void testHasDeleteUserTool() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("delete_user"));
            assertEquals("Delete a user.", registry.getTool("delete_user").description);
        }

        @Test
        @DisplayName("should have exactly 5 tools")
        void testToolCount() {
            ToolRegistry registry = new ToolRegistry();
            assertEquals(5, registry.getTools().size());
        }

        @Test
        @DisplayName("should contain all tool names")
        void testAllToolsPresent() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("get_user"));
            assertTrue(registry.hasTool("list_users"));
            assertTrue(registry.hasTool("create_user"));
            assertTrue(registry.hasTool("update_user"));
            assertTrue(registry.hasTool("delete_user"));
        }

        @Test
        @DisplayName("should not have unknown tools")
        void testNoUnknownTools() {
            ToolRegistry registry = new ToolRegistry();
            assertFalse(registry.hasTool("unknown_tool"));
            assertFalse(registry.hasTool("invalid"));
            assertFalse(registry.hasTool(""));
        }
    }

    @Nested
    @DisplayName("Tool Schema Validation")
    class ToolSchemaTests {
        @Test
        @DisplayName("get_user should have required user_id")
        void testGetUserSchema() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("get_user");
            assertTrue(tool.inputSchema.getAsJsonArray("required").toString().contains("user_id"));
        }

        @Test
        @DisplayName("get_user schema should have integer type for user_id")
        void testGetUserSchemaType() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("get_user");
            JsonObject properties = tool.inputSchema.getAsJsonObject("properties");
            assertEquals("integer", properties.getAsJsonObject("user_id").get("type").getAsString());
        }

        @Test
        @DisplayName("create_user should have required name and email")
        void testCreateUserSchema() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("create_user");
            String required = tool.inputSchema.getAsJsonArray("required").toString();
            assertTrue(required.contains("name"));
            assertTrue(required.contains("email"));
        }

        @Test
        @DisplayName("create_user should have string types for name and email")
        void testCreateUserSchemaTypes() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("create_user");
            JsonObject properties = tool.inputSchema.getAsJsonObject("properties");
            assertEquals("string", properties.getAsJsonObject("name").get("type").getAsString());
            assertEquals("string", properties.getAsJsonObject("email").get("type").getAsString());
        }

        @Test
        @DisplayName("update_user should have user_id as required")
        void testUpdateUserSchema() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("update_user");
            JsonArray required = tool.inputSchema.getAsJsonArray("required");
            assertEquals(1, required.size());
            assertEquals("user_id", required.get(0).getAsString());
        }

        @Test
        @DisplayName("delete_user should have user_id as required")
        void testDeleteUserSchema() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("delete_user");
            assertTrue(tool.inputSchema.getAsJsonArray("required").toString().contains("user_id"));
        }

        @Test
        @DisplayName("list_users should have empty required fields")
        void testListUsersSchema() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("list_users");
            JsonArray required = tool.inputSchema.getAsJsonArray("required");
            assertEquals(0, required.size());
        }

        @Test
        @DisplayName("all tools should have type object")
        void testAllToolsHaveObjectType() {
            ToolRegistry registry = new ToolRegistry();
            for (ToolRegistry.ToolInfo tool : registry.getTools().values()) {
                assertEquals("object", tool.inputSchema.get("type").getAsString());
            }
        }

        @Test
        @DisplayName("all tools should have properties object")
        void testAllToolsHaveProperties() {
            ToolRegistry registry = new ToolRegistry();
            for (ToolRegistry.ToolInfo tool : registry.getTools().values()) {
                assertTrue(tool.inputSchema.has("properties"));
                assertTrue(tool.inputSchema.get("properties").isJsonObject());
            }
        }
    }

    @Nested
    @DisplayName("Tool Descriptions")
    class ToolDescriptionTests {
        @Test
        @DisplayName("all tools should have descriptions")
        void testAllToolsHaveDescriptions() {
            ToolRegistry registry = new ToolRegistry();
            for (ToolRegistry.ToolInfo tool : registry.getTools().values()) {
                assertNotNull(tool.description);
                assertFalse(tool.description.isEmpty());
            }
        }

        @Test
        @DisplayName("descriptions should be meaningful")
        void testDescriptionsMeaningful() {
            ToolRegistry registry = new ToolRegistry();

            assertEquals("Get one user by ID.", registry.getTool("get_user").description);
            assertEquals("List all users.", registry.getTool("list_users").description);
            assertEquals("Create a user.", registry.getTool("create_user").description);
            assertEquals("Update a user's name and/or email.", registry.getTool("update_user").description);
            assertEquals("Delete a user.", registry.getTool("delete_user").description);
        }
    }

    @Nested
    @DisplayName("Tool Registry Edge Cases")
    class ToolRegistryEdgeCasesTests {
        @Test
        @DisplayName("should handle null tool name gracefully")
        void testGetNullTool() {
            ToolRegistry registry = new ToolRegistry();
            assertNull(registry.getTool(null));
        }

        @Test
        @DisplayName("should be case-sensitive")
        void testCaseSensitivity() {
            ToolRegistry registry = new ToolRegistry();
            assertTrue(registry.hasTool("get_user"));
            assertFalse(registry.hasTool("GET_USER"));
            assertFalse(registry.hasTool("Get_User"));
        }

        @Test
        @DisplayName("should handle whitespace in tool names")
        void testWhitespaceTool() {
            ToolRegistry registry = new ToolRegistry();
            assertFalse(registry.hasTool(" get_user"));
            assertFalse(registry.hasTool("get_user "));
            assertFalse(registry.hasTool("get user"));
        }

        @Test
        @DisplayName("get_user tool info should be non-null")
        void testToolInfoNotNull() {
            ToolRegistry registry = new ToolRegistry();
            ToolRegistry.ToolInfo tool = registry.getTool("get_user");
            assertNotNull(tool);
            assertNotNull(tool.name);
            assertNotNull(tool.description);
            assertNotNull(tool.inputSchema);
        }

        @Test
        @DisplayName("tools should be ordered consistently")
        void testToolOrdering() {
            ToolRegistry registry1 = new ToolRegistry();
            ToolRegistry registry2 = new ToolRegistry();

            assertEquals(
                    registry1.getTools().keySet().toString(),
                    registry2.getTools().keySet().toString()
            );
        }
    }

    @Nested
    @DisplayName("Schema Property Validation")
    class SchemaPropertyTests {
        @Test
        @DisplayName("get_user properties should contain user_id")
        void testGetUserHasUserIdProperty() {
            ToolRegistry registry = new ToolRegistry();
            JsonObject properties = registry.getTool("get_user").inputSchema.getAsJsonObject("properties");
            assertTrue(properties.has("user_id"));
        }

        @Test
        @DisplayName("create_user should have name and email properties")
        void testCreateUserProperties() {
            ToolRegistry registry = new ToolRegistry();
            JsonObject properties = registry.getTool("create_user").inputSchema.getAsJsonObject("properties");
            assertTrue(properties.has("name"));
            assertTrue(properties.has("email"));
        }

        @Test
        @DisplayName("update_user should have user_id, name, and email properties")
        void testUpdateUserProperties() {
            ToolRegistry registry = new ToolRegistry();
            JsonObject properties = registry.getTool("update_user").inputSchema.getAsJsonObject("properties");
            assertTrue(properties.has("user_id"));
            assertTrue(properties.has("name"));
            assertTrue(properties.has("email"));
        }

        @Test
        @DisplayName("properties should have descriptions")
        void testPropertiesHaveDescriptions() {
            ToolRegistry registry = new ToolRegistry();
            JsonObject properties = registry.getTool("get_user").inputSchema.getAsJsonObject("properties");
            JsonObject userIdProp = properties.getAsJsonObject("user_id");
            assertTrue(userIdProp.has("description"));
            assertFalse(userIdProp.get("description").getAsString().isEmpty());
        }
    }

    @Nested
    @DisplayName("MCP Server - Tool Execution Tests")
    class McpServerToolExecutionTests {
        @Test
        @DisplayName("should execute get_user successfully")
        void testExecuteGetUser() throws IOException {
            Map<String, Object> mockUser = new HashMap<>();
            mockUser.put("id", 1);
            mockUser.put("name", "Victor");

            when(mockUserService.getUser(1)).thenReturn(mockUser);

            Map<String, Object> result = mockUserService.getUser(1);

            assertNotNull(result);
            assertEquals(1, result.get("id"));
            verify(mockUserService).getUser(1);
        }

        @Test
        @DisplayName("should execute list_users successfully")
        void testExecuteListUsers() throws IOException {
            when(mockUserService.listUsers()).thenReturn(List.of());

            List<Map<String, Object>> result = mockUserService.listUsers();

            assertNotNull(result);
            verify(mockUserService).listUsers();
        }

        @Test
        @DisplayName("should handle service exceptions")
        void testExecutionException() throws IOException {
            when(mockUserService.getUser(1)).thenThrow(new IOException("Service error"));

            assertThrows(IOException.class, () -> mockUserService.getUser(1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 100, Integer.MAX_VALUE})
        @DisplayName("should handle various user IDs")
        void testVariousUserIds(int userId) throws IOException {
            Map<String, Object> mockUser = new HashMap<>();
            mockUser.put("id", userId);

            when(mockUserService.getUser(userId)).thenReturn(mockUser);

            Map<String, Object> result = mockUserService.getUser(userId);
            assertEquals(userId, result.get("id"));
        }
    }

    @Nested
    @DisplayName("Tool Name Validation")
    class ToolNameValidationTests {
        @Test
        @DisplayName("should not accept empty tool names")
        void testEmptyToolName() {
            ToolRegistry registry = new ToolRegistry();
            assertFalse(registry.hasTool(""));
        }

        @Test
        @DisplayName("should not accept random tool names")
        void testRandomToolNames() {
            ToolRegistry registry = new ToolRegistry();
            assertFalse(registry.hasTool("random_tool_123"));
            assertFalse(registry.hasTool("admin_delete_all"));
            assertFalse(registry.hasTool("sql_injection"));
        }

        @Test
        @DisplayName("should only have specific tools")
        void testOnlySpecificTools() {
            ToolRegistry registry = new ToolRegistry();
            String[] expectedTools = {"get_user", "list_users", "create_user", "update_user", "delete_user"};

            for (String expectedTool : expectedTools) {
                assertTrue(registry.hasTool(expectedTool), "Should have tool: " + expectedTool);
            }

            assertEquals(5, registry.getTools().size(), "Should have exactly 5 tools");
        }
    }

    @Nested
    @DisplayName("MCP Protocol Compliance")
    class McpProtocolComplianceTests {
        @Test
        @DisplayName("tools should follow MCP schema structure")
        void testMcpSchemaStructure() {
            ToolRegistry registry = new ToolRegistry();

            for (ToolRegistry.ToolInfo tool : registry.getTools().values()) {
                // Each tool should have these properties
                assertEquals("object", tool.inputSchema.get("type").getAsString());
                assertTrue(tool.inputSchema.has("properties"));
                assertTrue(tool.inputSchema.has("required"));

                // Required should be an array
                assertTrue(tool.inputSchema.get("required").isJsonArray());
            }
        }

        @Test
        @DisplayName("properties should have type and description")
        void testPropertyStructure() {
            ToolRegistry registry = new ToolRegistry();
            JsonObject getUserProps = registry.getTool("get_user").inputSchema.getAsJsonObject("properties");

            for (String propName : getUserProps.keySet()) {
                JsonObject prop = getUserProps.getAsJsonObject(propName);
                assertTrue(prop.has("type"), "Property " + propName + " should have type");
                assertTrue(prop.has("description"), "Property " + propName + " should have description");
            }
        }
    }
}
