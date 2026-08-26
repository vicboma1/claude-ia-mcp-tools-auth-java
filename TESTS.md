# Test Suite Documentation

Complete test coverage for the MCP Users Server with 50+ test cases covering success paths, edge cases, and error scenarios.

## Test Files

### 1. UserServiceTest.java (30+ tests)

Business logic validation layer tests using Mockito for API client mocking.

#### getUser Tests (5 tests)
-  Success: Returns normalized user
-  Normalization: Removes extra fields, keeps only id/name/email/username
-  Edge Case: User ID = 0 → throws IllegalArgumentException
-  Edge Case: Negative user IDs → throws IllegalArgumentException
-  Edge Case: MAX_VALUE user ID → handled correctly
-  Error: IOException from API → propagated

#### listUsers Tests (4 tests)
-  Success: Returns list of normalized users
-  Empty: Returns empty list when no users
-  Large: Handles 1000+ users efficiently
-  Normalization: Each user normalized independently
-  Error: IOException from API → propagated

#### createUser Tests (10 tests)
-  Success: Creates user with normalized inputs
-  Input Normalization:
  - Trims leading/trailing whitespace
  - Converts email to lowercase
  - Preserves spaces inside names
-  Empty/Whitespace Names:
  - Empty string → throws
  - Spaces only → throws
  - Tabs/newlines → throws
-  Empty/Whitespace Emails:
  - Empty string → throws
  - Whitespace only → throws
-  Invalid Emails:
  - No @ symbol → throws
  - Multiple @ symbols → throws
  - @ with no domain → throws
-  Special Characters: Accepts José, accents, etc.
-  Long Names: Accepts 1000+ character names
-  Long Emails: Accepts very long email addresses
-  Error Propagation: API errors → propagated

#### updateUser Tests (9 tests)
-  Update Name Only: Updates name, keeps email
-  Update Email Only: Updates email, keeps name
-  Update Both: Updates both fields simultaneously
-  Validation:
  - Zero ID → throws
  - Negative ID → throws
  - No fields supplied (both null) → throws
  - Empty name after trim → throws
  - Invalid email without @ → throws
-  Normalization: Trims and lowercases inputs
-  Null Handling: Allows null fields for partial updates
-  Edge Cases:
  - MAX_VALUE user ID → handled
  - Very long strings → handled

#### deleteUser Tests (6 tests)
-  Success: Deletes user, returns {user_id, deleted: true}
-  Validation:
  - Zero ID → throws
  - Negative ID → throws
  - MAX_VALUE ID → handled
-  Response Structure: Always returns user_id and deleted fields
-  Error Handling: IOException from API → propagated

### 2. McpServerTest.java (20+ tests)

MCP protocol layer and tool registry validation tests.

#### Tools Registry Tests (7 tests)
-  Presence: All 5 tools exist (get_user, list_users, create_user, update_user, delete_user)
-  Count: Exactly 5 tools
-  Unknown Tools: Returns false for non-existent tools
-  Tool Descriptions: All tools have meaningful descriptions
-  Case Sensitivity: Tools are case-sensitive
-  Whitespace Handling: Rejects tools with leading/trailing spaces
-  Ordering: Tools in consistent order

#### Tool Schema Validation Tests (8 tests)
-  Type Structure:
  - Each tool has "object" type
  - Properties are objects
  - Required is an array
-  Get User Schema:
  - Has required "user_id"
  - user_id is integer type
-  Create User Schema:
  - Has required "name" and "email"
  - name and email are string types
-  Update User Schema:
  - Has required "user_id"
  - name and email are optional
-  Delete User Schema:
  - Has required "user_id"
-  List Users Schema:
  - No required fields
-  Property Descriptions: All properties have descriptions

#### Tool Description Tests (1 test)
-  Descriptions: All tools have non-empty, meaningful descriptions

#### MCP Protocol Compliance Tests (2 tests)
-  Schema Structure: Follows MCP specification
-  Property Structure: Each property has type and description

#### Tool Execution Tests (3 tests)
-  get_user execution
-  list_users execution
-  Exception handling

### 3. ApiClientTest.java (20+ tests)

HTTP client layer tests covering initialization, error handling, and edge cases.

#### Initialization Tests (4 tests)
-  Default Base URL: Uses JSONPlaceholder
-  Custom Base URL: Accepts custom endpoints
-  Trailing Slash: Strips trailing slash from URL
-  No Trailing Slash: Handles URLs without slash

#### URL Construction Tests
-  Proper URL formation for each endpoint
-  Handles various user IDs

#### Timeout Configuration Tests
-  Configured timeouts (10 seconds)
-  Handles requests within timeout

#### Error Handling Tests
-  Invalid endpoints → IOException
-  Null URL → Exception
-  Connection timeouts
-  Read timeouts
-  Write timeouts

#### HTTP Methods Tests
-  GET for retrieves
-  POST for creates
-  PATCH for updates
-  DELETE for deletes

#### Payload Validation Tests
-  Valid create payloads
-  Partial updates
-  Null values
-  Empty strings
-  Special characters (José, accents)
-  Unicode characters
-  Very long strings

#### Response Parsing Tests
-  User object parsing
-  User list parsing
-  Nested JSON handling
-  Null fields in response
-  Field preservation

#### Concurrency Tests
-  Thread-safe client
-  Concurrent requests

## Test Statistics

| Category | Count | Coverage |
|----------|-------|----------|
| **Unit Tests** | 30+ | UserService business logic |
| **Integration Tests** | 20+ | MCP server & tool registry |
| **API Client Tests** | 20+ | HTTP client & edge cases |
| **Total Test Cases** | **70+** | **Comprehensive coverage** |

## Corner Cases Covered

### Input Validation
-  Empty strings
-  Whitespace-only strings
-  Null values
-  Very long inputs (1000+ chars)
-  Special characters (accents, unicode)
-  Case sensitivity

### ID Validation
-  ID = 0
-  Negative IDs
-  MAX_VALUE IDs
-  Large IDs (999999999+)

### Email Validation
-  Missing @ symbol
-  Multiple @ symbols
-  No domain
-  Mixed case (UPPERCASE)
-  Plus addressing (user+tag@example.com)
-  Subdomains (a.b.c@example.com)

### Error Scenarios
-  IOException from API
-  RuntimeException from API
-  Connection timeouts
-  Invalid endpoints
-  Malformed JSON

### Data Integrity
-  Field normalization
-  Extra field removal
-  Data type preservation
-  Large numeric values
-  Concurrent requests

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
mvn test -Dtest=McpServerTest
mvn test -Dtest=ApiClientTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=UserServiceTest#testCreateUserSuccess
```

### Run with Coverage Report
```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

### Run Tests in IDE
- **IntelliJ IDEA**: Right-click test class → Run or Debug
- **VS Code**: Click "Run Test" above test methods
- **Eclipse**: Right-click → Run As → JUnit Test

## Test Patterns Used

### 1. Nested Test Organization
```java
@Nested
@DisplayName("getUser")
class GetUserTests { ... }
```

### 2. Parameterized Tests
```java
@ParameterizedTest
@ValueSource(ints = {0, -1, -100})
void testInvalidIds(int id) { ... }
```

### 3. Mock Objects
```java
@Mock
private ApiClient mockApiClient;

when(mockApiClient.getUser(1)).thenReturn(user);
```

### 4. Assertion Methods
```java
assertEquals(expected, actual);
assertTrue(condition);
assertThrows(Exception.class, () -> code);
```

## Test Quality Metrics

-  **Coverage**: Business logic fully covered
-  **Edge Cases**: 40+ corner cases tested
-  **Error Paths**: All error scenarios validated
-  **Isolation**: Tests use mocks, no external dependencies
-  **Clarity**: Descriptive test names and display names
-  **Maintainability**: Well-organized with nested classes
-  **Parameterization**: Reduces code duplication
-  **Documentation**: Comprehensive @DisplayName annotations

## Continuous Integration

All tests run in CI/CD pipeline:
```yaml
test:
  script:
    - mvn clean test
    - mvn jacoco:report
  coverage: '/Coverage: \d+\.\d+%/'
```

## Known Limitations

1. **Integration Tests**: ApiClientTest uses mock assertions; real API integration requires network
2. **Performance Tests**: Not included; use JMH for benchmarking
3. **Load Tests**: Not included; use JMeter or Gatling for load testing
4. **Security Tests**: Basic validation covered; use OWASP ZAP for security scanning
