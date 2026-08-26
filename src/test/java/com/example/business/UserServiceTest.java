package com.example.business;

import com.example.api.ApiClient;
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

class UserServiceTest {
    private UserService userService;

    @Mock
    private ApiClient mockApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(mockApiClient);
    }

    private Map<String, Object> createTestUser(int id, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("username", name.toLowerCase().replace(" ", ""));
        return user;
    }

    @Nested
    @DisplayName("getUser")
    class GetUserTests {
        @Test
        @DisplayName("should return normalized user when successful")
        void testGetUserSuccess() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Victor", "victor@example.com");

            when(mockApiClient.getUser(1)).thenReturn(apiResponse);

            Map<String, Object> result = userService.getUser(1);

            assertEquals(1, result.get("id"));
            assertEquals("Victor", result.get("name"));
            assertEquals("victor@example.com", result.get("email"));
            verify(mockApiClient, times(1)).getUser(1);
        }

        @Test
        @DisplayName("should normalize output to exact fields")
        void testGetUserNormalization() throws IOException {
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("id", 1);
            apiResponse.put("name", "Victor");
            apiResponse.put("email", "victor@example.com");
            apiResponse.put("username", "victor");
            apiResponse.put("phone", "123456789"); // Extra field not in schema
            apiResponse.put("address", "123 Main St"); // Extra field not in schema

            when(mockApiClient.getUser(1)).thenReturn(apiResponse);

            Map<String, Object> result = userService.getUser(1);

            // Should only have 4 fields
            assertEquals(4, result.size());
            assertTrue(result.containsKey("id"));
            assertTrue(result.containsKey("name"));
            assertTrue(result.containsKey("email"));
            assertTrue(result.containsKey("username"));
            assertFalse(result.containsKey("phone"));
            assertFalse(result.containsKey("address"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
        @DisplayName("should throw IllegalArgumentException for invalid IDs")
        void testGetUserInvalidIds(int invalidId) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.getUser(invalidId));
            assertTrue(ex.getMessage().contains("greater than zero"));
        }

        @Test
        @DisplayName("should throw IOException when API throws IOException")
        void testGetUserApiIOException() throws IOException {
            when(mockApiClient.getUser(999)).thenThrow(new IOException("User not found"));

            assertThrows(IOException.class, () -> userService.getUser(999));
        }

        @Test
        @DisplayName("should throw RuntimeException when API throws RuntimeException")
        void testGetUserApiRuntimeException() throws IOException {
            when(mockApiClient.getUser(999)).thenThrow(new RuntimeException("Server error"));

            assertThrows(RuntimeException.class, () -> userService.getUser(999));
        }

        @Test
        @DisplayName("should handle large user IDs")
        void testGetUserLargeId() throws IOException {
            Map<String, Object> apiResponse = createTestUser(Integer.MAX_VALUE, "Big", "big@example.com");

            when(mockApiClient.getUser(Integer.MAX_VALUE)).thenReturn(apiResponse);

            Map<String, Object> result = userService.getUser(Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, result.get("id"));
        }
    }

    @Nested
    @DisplayName("listUsers")
    class ListUsersTests {
        @Test
        @DisplayName("should return list of normalized users")
        void testListUsersSuccess() throws IOException {
            Map<String, Object> user1 = createTestUser(1, "Victor", "victor@example.com");
            Map<String, Object> user2 = createTestUser(2, "John", "john@example.com");

            when(mockApiClient.listUsers()).thenReturn(List.of(user1, user2));

            List<Map<String, Object>> result = userService.listUsers();

            assertEquals(2, result.size());
            assertEquals("Victor", result.get(0).get("name"));
            assertEquals("John", result.get(1).get("name"));
            verify(mockApiClient, times(1)).listUsers();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void testListUsersEmpty() throws IOException {
            when(mockApiClient.listUsers()).thenReturn(List.of());

            List<Map<String, Object>> result = userService.listUsers();

            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("should handle large number of users")
        void testListUsersLarge() throws IOException {
            List<Map<String, Object>> users = new java.util.ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                users.add(createTestUser(i, "User" + i, "user" + i + "@example.com"));
            }

            when(mockApiClient.listUsers()).thenReturn(users);

            List<Map<String, Object>> result = userService.listUsers();

            assertEquals(1000, result.size());
            assertEquals("User1", result.get(0).get("name"));
            assertEquals("User1000", result.get(999).get("name"));
        }

        @Test
        @DisplayName("should normalize each user independently")
        void testListUsersNormalization() throws IOException {
            Map<String, Object> user1 = new HashMap<>();
            user1.put("id", 1);
            user1.put("name", "Victor");
            user1.put("email", "victor@example.com");
            user1.put("username", "victor");
            user1.put("extra_field", "should be removed");

            Map<String, Object> user2 = createTestUser(2, "John", "john@example.com");

            when(mockApiClient.listUsers()).thenReturn(List.of(user1, user2));

            List<Map<String, Object>> result = userService.listUsers();

            assertEquals(2, result.size());
            assertEquals(4, result.get(0).size()); // Normalized to 4 fields
            assertFalse(result.get(0).containsKey("extra_field"));
        }

        @Test
        @DisplayName("should throw IOException when API fails")
        void testListUsersApiFailure() throws IOException {
            when(mockApiClient.listUsers()).thenThrow(new IOException("Connection failed"));

            assertThrows(IOException.class, () -> userService.listUsers());
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {
        @Test
        @DisplayName("should create user with normalized inputs")
        void testCreateUserSuccess() throws IOException {
            Map<String, Object> apiResponse = createTestUser(3, "Alice", "alice@example.com");

            when(mockApiClient.createUser("Alice", "alice@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser("Alice", "alice@example.com");

            assertEquals("Alice", result.get("name"));
            assertEquals("alice@example.com", result.get("email"));
            verify(mockApiClient, times(1)).createUser("Alice", "alice@example.com");
        }

        @Test
        @DisplayName("should trim and lowercase email")
        void testCreateUserNormalizeInput() throws IOException {
            Map<String, Object> apiResponse = createTestUser(3, "Bob", "bob@example.com");

            when(mockApiClient.createUser("Bob", "bob@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser("  Bob  ", "BOB@EXAMPLE.COM");

            verify(mockApiClient).createUser("Bob", "bob@example.com");
        }

        @Test
        @DisplayName("should preserve spaces inside name")
        void testCreateUserPreserveNameSpaces() throws IOException {
            Map<String, Object> apiResponse = createTestUser(3, "John Doe", "johndoe@example.com");

            when(mockApiClient.createUser("John Doe", "johndoe@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser("John Doe", "johndoe@example.com");

            assertEquals("John Doe", result.get("name"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("should throw for empty/whitespace names")
        void testCreateUserEmptyName(String emptyName) {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(emptyName, "test@example.com"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("should throw for empty/whitespace emails")
        void testCreateUserEmptyEmail(String emptyEmail) {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser("Charlie", emptyEmail));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "no-at-sign", "@nodomain", "spaces in@email.com", "double@@at.com"})
        @DisplayName("should throw for invalid emails (no @)")
        void testCreateUserInvalidEmail(String invalidEmail) {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser("Dave", invalidEmail));
        }

        @Test
        @DisplayName("should accept valid emails with special chars")
        void testCreateUserSpecialCharEmail() throws IOException {
            Map<String, Object> apiResponse = createTestUser(3, "José", "jose+test@example.co.uk");

            when(mockApiClient.createUser("José", "jose+test@example.co.uk")).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser("José García", "jose+test@example.co.uk");

            assertNotNull(result);
            verify(mockApiClient).createUser("José García", "jose+test@example.co.uk");
        }

        @Test
        @DisplayName("should accept very long names")
        void testCreateUserLongName() throws IOException {
            String longName = "A".repeat(1000);
            Map<String, Object> apiResponse = createTestUser(3, longName, "test@example.com");

            when(mockApiClient.createUser(longName, "test@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser(longName, "test@example.com");

            assertEquals(longName, result.get("name"));
        }

        @Test
        @DisplayName("should accept very long emails")
        void testCreateUserLongEmail() throws IOException {
            String longEmail = "a".repeat(100) + "@example.com";
            Map<String, Object> apiResponse = createTestUser(3, "User", longEmail);

            when(mockApiClient.createUser("User", longEmail)).thenReturn(apiResponse);

            Map<String, Object> result = userService.createUser("User", longEmail);

            assertEquals(longEmail, result.get("email"));
        }

        @Test
        @DisplayName("should propagate API errors")
        void testCreateUserApiError() throws IOException {
            when(mockApiClient.createUser("Eve", "eve@example.com"))
                    .thenThrow(new IOException("Database error"));

            assertThrows(IOException.class, () -> userService.createUser("Eve", "eve@example.com"));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {
        @Test
        @DisplayName("should update only name")
        void testUpdateUserNameOnly() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Victor Updated", "victor@example.com");

            when(mockApiClient.updateUser(1, "Victor Updated", null)).thenReturn(apiResponse);

            Map<String, Object> result = userService.updateUser(1, "Victor Updated", null);

            assertEquals("Victor Updated", result.get("name"));
            assertEquals("victor@example.com", result.get("email"));
        }

        @Test
        @DisplayName("should update only email")
        void testUpdateUserEmailOnly() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Victor", "newemail@example.com");

            when(mockApiClient.updateUser(1, null, "newemail@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.updateUser(1, null, "newemail@example.com");

            assertEquals("Victor", result.get("name"));
            assertEquals("newemail@example.com", result.get("email"));
        }

        @Test
        @DisplayName("should update both name and email")
        void testUpdateUserBoth() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "New Name", "newemail@example.com");

            when(mockApiClient.updateUser(1, "New Name", "newemail@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.updateUser(1, "New Name", "newemail@example.com");

            assertEquals("New Name", result.get("name"));
            assertEquals("newemail@example.com", result.get("email"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
        @DisplayName("should throw for invalid user IDs")
        void testUpdateUserInvalidIds(int invalidId) {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(invalidId, "Name", "email@example.com"));
        }

        @Test
        @DisplayName("should throw when no fields supplied")
        void testUpdateUserNoFields() {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(1, null, null));
        }

        @Test
        @DisplayName("should throw for empty name after trimming")
        void testUpdateUserEmptyName() {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(1, "   ", "test@example.com"));
        }

        @Test
        @DisplayName("should throw for invalid email without @")
        void testUpdateUserInvalidEmail() {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(1, "Test", "invalid-email"));
        }

        @Test
        @DisplayName("should normalize name and email")
        void testUpdateUserNormalization() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Alice", "alice@example.com");

            when(mockApiClient.updateUser(1, "Alice", "alice@example.com")).thenReturn(apiResponse);

            Map<String, Object> result = userService.updateUser(1, "  Alice  ", "ALICE@EXAMPLE.COM");

            verify(mockApiClient).updateUser(1, "Alice", "alice@example.com");
        }

        @Test
        @DisplayName("should allow null email when updating name only")
        void testUpdateUserNameNullEmail() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Updated", "victor@example.com");

            when(mockApiClient.updateUser(1, "Updated", null)).thenReturn(apiResponse);

            assertDoesNotThrow(() -> userService.updateUser(1, "Updated", null));
        }

        @Test
        @DisplayName("should allow null name when updating email only")
        void testUpdateUserNullNameEmail() throws IOException {
            Map<String, Object> apiResponse = createTestUser(1, "Victor", "new@example.com");

            when(mockApiClient.updateUser(1, null, "new@example.com")).thenReturn(apiResponse);

            assertDoesNotThrow(() -> userService.updateUser(1, null, "new@example.com"));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {
        @Test
        @DisplayName("should delete user successfully")
        void testDeleteUserSuccess() throws IOException {
            when(mockApiClient.deleteUser(1)).thenReturn(true);

            Map<String, Object> result = userService.deleteUser(1);

            assertEquals(1, result.get("user_id"));
            assertEquals(true, result.get("deleted"));
            verify(mockApiClient, times(1)).deleteUser(1);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
        @DisplayName("should throw for invalid user IDs")
        void testDeleteUserInvalidIds(int invalidId) {
            assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(invalidId));
        }

        @Test
        @DisplayName("should throw when API throws IOException")
        void testDeleteUserApiError() throws IOException {
            when(mockApiClient.deleteUser(999)).thenThrow(new IOException("User not found"));

            assertThrows(IOException.class, () -> userService.deleteUser(999));
        }

        @Test
        @DisplayName("should return correct structure")
        void testDeleteUserResponseStructure() throws IOException {
            when(mockApiClient.deleteUser(5)).thenReturn(true);

            Map<String, Object> result = userService.deleteUser(5);

            assertEquals(2, result.size());
            assertTrue(result.containsKey("user_id"));
            assertTrue(result.containsKey("deleted"));
            assertEquals(5, result.get("user_id"));
            assertTrue((Boolean) result.get("deleted"));
        }

        @Test
        @DisplayName("should handle large user IDs")
        void testDeleteUserLargeId() throws IOException {
            when(mockApiClient.deleteUser(Integer.MAX_VALUE)).thenReturn(true);

            Map<String, Object> result = userService.deleteUser(Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, result.get("user_id"));
        }
    }
}
