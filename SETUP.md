# Setup & Build Guide

## Prerequisites

- Java 11+ (OpenJDK or Oracle JDK)
- Maven 3.6+

## Installation

### 1. Install Maven (if not already installed)

**macOS (Homebrew):**
```bash
brew install maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install maven
```

**Windows:**
Download from https://maven.apache.org/download.cgi and add to PATH

**Or use SDKMAN:**
```bash
sdk install maven
```

### 2. Verify Maven Installation

```bash
mvn --version
```

## Build the Project

```bash
mvn clean compile
```

## Run Tests

```bash
mvn test
```

Output will show:
-  10+ tests passing for UserService
-  5+ tests passing for McpServer/ToolRegistry

## Package as JAR

```bash
mvn package
```

This creates: `target/mcp-users-server-1.0.0.jar`

## Run the MCP Server

### Option 1: Via Maven
```bash
mvn exec:java -Dexec.mainClass="com.example.mcp.McpServer"
```

### Option 2: Via JAR
```bash
java -jar target/mcp-users-server-1.0.0.jar
```

### Option 3: Compiled Classes
```bash
java -cp target/classes:~/.m2/repository/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar:~/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:~/.m2/repository/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar:~/.m2/repository/ch/qos/logback/logback-classic/1.4.8/logback-classic-1.4.8.jar com.example.mcp.McpServer
```

## Test MCP Server

With the server running, send a test request:

```bash
echo '{"jsonrpc":"2.0","method":"initialize","id":1}' | java -jar target/mcp-users-server-1.0.0.jar
```

## IDE Setup

### IntelliJ IDEA
1. Open the project
2. Right-click on `pom.xml` → "Run Maven" → "Reload Maven Project"
3. Tests will be recognized automatically

### VS Code
1. Install "Extension Pack for Java" (Microsoft)
2. Open the project, VS Code will detect Maven automatically
3. Tests can be run from the Test Explorer

### Eclipse
1. Import project as "Maven Project"
2. Right-click → Maven → Update Project

## Troubleshooting

**"Maven command not found"**
- Install Maven following the guide above
- Add Maven `bin` directory to PATH

**"Compilation errors"**
- Verify Java version: `java -version` (should be 11+)
- Run: `mvn clean compile`

**"Tests failing"**
- Ensure all dependencies are downloaded: `mvn dependency:resolve`
- Check internet connection (Maven downloads from central repository)

**"Cannot find symbol" errors**
- Run: `mvn clean compile` to refresh IDE caches
- In IDE, invalidate caches and restart
