# Project Guidelines

## Overview

Spring Boot 4.0.5 + Spring AI 2.0.0-M4 MCP server (Java 21) that dynamically exposes database tables as MCP tools. Tools are defined as SQL queries stored in an H2 in-memory database and registered with the MCP server at runtime.

## Build and Test

```bash
# Build
mvnw.cmd compile

# Run tests
mvnw.cmd test

# Package JAR
mvnw.cmd package

# Run application (port 8080)
mvnw.cmd spring-boot:run
```

## Architecture

```
config/          # Spring config + REST API controller
  SecurityConfig       - Permits /mcp/**, /h2-console/**, disables CSRF
  JacksonConfig        - ObjectMapper bean
  ToolRefreshController - REST API at /api/** for tool/table CRUD + dashboard

mcp/             # MCP protocol layer
  DynamicToolManager   - Loads tools from TOOL_REGISTRY on startup, runtime refresh
  DatabasePrompts      - @McpPrompt: data-analysis, query-helper
  DatabaseResources    - @McpResource: db://tables, db://table/{name}/schema|data
  SqlValidator         - Blocks non-SELECT queries, H2-specific functions, query stacking

service/         # Business logic
  ToolRegistryService  - CRUD for tool definitions in TOOL_REGISTRY + TOOL_PARAMETER
  TableRegistryService - Manages TABLE_REGISTRY + COLUMN_REGISTRY, DDL execution
  DataQueryService     - Safe parameterized queries on registered tables only
```

**Key endpoints:**
- MCP: `http://localhost:8080/mcp`
- Admin UI: `http://localhost:8080/admin.html`
- REST API: `http://localhost:8080/api/**`
- H2 Console: `http://localhost:8080/h2-console` (user: `sa`, no password)

## Conventions

- **DTOs as Java records**: Use `record` types for immutable data (e.g., `ToolDefinition`, `ColumnDefinition`, `RefreshResult`)
- **Database access**: `JdbcTemplate` and `NamedParameterJdbcTemplate` — no JPA/Hibernate
- **Naming**: camelCase for Java, UPPERCASE for SQL tables/columns, kebab-case for MCP tool names
- **Annotations**: `@Service`, `@Component`, `@Configuration`, `@RestController`, `@McpPrompt`, `@McpResource`
- **Transactions**: `@Transactional` for multi-step database operations
- **Logging**: SLF4J `Logger` (via `LoggerFactory`)
- **Error handling**: Custom exceptions (`SqlValidationException`) with descriptive messages; MCP tool errors returned as text content, not thrown

## Security — Critical

SQL injection prevention is enforced at multiple layers. **Do not bypass these:**

1. **SqlValidator**: Only `SELECT` queries allowed. Blocks INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, TRUNCATE, MERGE, EXEC, CALL, SET, GRANT, REVOKE, COMMIT, ROLLBACK. Blocks H2 functions: FILE_READ, FILE_WRITE, CSVREAD, CSVWRITE, LINK_SCHEMA. No semicolons (query stacking).
2. **Name validation**: Table/column names validated against `^[A-Za-z][A-Za-z0-9_]{0,127}$`
3. **Parameterized queries**: All user input bound via `NamedParameterJdbcTemplate` — never concatenated into SQL
4. **Operator whitelist**: Only `=`, `!=`, `>`, `<`, `>=`, `<=`, `LIKE`
5. **Query timeout**: 10 seconds max
6. **Row limit**: Max 1000 rows returned

## Database Schema

Six tables: `TABLE_REGISTRY`, `COLUMN_REGISTRY`, `TOOL_REGISTRY`, `TOOL_PARAMETER` (metadata), `CUSTOMERS`, `ORDERS` (example data). See [schema.sql](../src/main/resources/schema.sql) and [data.sql](../src/main/resources/data.sql).

## Documentation

- [README.md](../README.md) — Setup, build commands, MCP client config examples (German)
- [DOCS.md](../DOCS.md) — Architecture deep-dive, class documentation (German)
- [HELP.md](../HELP.md) — Spring Boot reference links
