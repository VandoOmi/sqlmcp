package com.vando.mcp_test.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import com.vando.mcp_test.service.ToolRegistryService;
import com.vando.mcp_test.service.ToolRegistryService.ToolDefinition;
import com.vando.mcp_test.service.ToolRegistryService.ToolParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicToolManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicToolManager.class);
    private static final int DEFAULT_LIMIT = 1000;
    private static final int QUERY_TIMEOUT_SECONDS = 10;

    private final McpSyncServer mcpServer;
    private final ToolRegistryService toolRegistry;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;
    private final Set<String> registeredToolNames = new HashSet<>();

    public DynamicToolManager(McpSyncServer mcpServer,
                              ToolRegistryService toolRegistry,
                              @Qualifier("queryNamedJdbcTemplate") NamedParameterJdbcTemplate namedJdbc,
                              ObjectMapper objectMapper) {
        this.mcpServer = mcpServer;
        this.toolRegistry = toolRegistry;
        this.namedJdbc = namedJdbc;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshTools();
    }

    public RefreshResult refreshTools() {
        List<ToolDefinition> desiredTools = toolRegistry.loadActiveTools();
        Set<String> desiredNames = desiredTools.stream()
                .map(ToolDefinition::name)
                .collect(Collectors.toSet());

        int added = 0;
        int removed = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        // Remove tools that are no longer in the DB
        Set<String> toRemove = new HashSet<>(registeredToolNames);
        toRemove.removeAll(desiredNames);
        for (String name : toRemove) {
            mcpServer.removeTool(name);
            registeredToolNames.remove(name);
            removed++;
            log.info("Removed dynamic tool: {}", name);
        }

        // Add or update tools from DB
        for (ToolDefinition tool : desiredTools) {
            try {
                SqlValidator.validate(tool.sqlQuery());
                McpServerFeatures.SyncToolSpecification spec = buildToolSpec(tool);
                mcpServer.addTool(spec);
                registeredToolNames.add(tool.name());
                added++;
                log.info("Registered dynamic tool: {}", tool.name());
            } catch (SqlValidator.SqlValidationException e) {
                skipped++;
                String error = "Skipped tool '" + tool.name() + "': " + e.getMessage();
                errors.add(error);
                log.warn(error);
            }
        }

        log.info("Tool refresh complete: {} added/updated, {} removed, {} skipped", added, removed, skipped);
        return new RefreshResult(added, removed, skipped, errors);
    }

    private McpServerFeatures.SyncToolSpecification buildToolSpec(ToolDefinition tool) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ToolParameter param : tool.parameters()) {
            Map<String, Object> paramSchema = new LinkedHashMap<>();
            paramSchema.put("type", mapParamType(param.type()));
            if (param.description() != null) {
                paramSchema.put("description", param.description());
            }
            properties.put(param.name(), paramSchema);
            if (param.required()) {
                required.add(param.name());
            }
        }

        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object", properties, required, false, null, null);

        McpSchema.Tool mcpTool = McpSchema.Tool.builder()
                .name(tool.name())
                .description(tool.description())
                .inputSchema(inputSchema)
                .build();

        String sqlQuery = tool.sqlQuery();
        List<ToolParameter> paramDefs = tool.parameters();

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(mcpTool)
                .callHandler((exchange, request) -> executeTool(sqlQuery, paramDefs, request))
                .build();
    }

    private McpSchema.CallToolResult executeTool(String sqlQuery,
                                                  List<ToolParameter> paramDefs,
                                                  McpSchema.CallToolRequest request) {
        try {
            Map<String, Object> arguments = request.arguments() != null ? request.arguments() : Map.of();
            Map<String, Object> validatedParams = validateAndConvertParameters(paramDefs, arguments);

            String effectiveSql = ensureLimit(sqlQuery);

            namedJdbc.getJdbcTemplate().setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            List<Map<String, Object>> results = namedJdbc.queryForList(effectiveSql, validatedParams);

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
            if (results.isEmpty()) {
                json = "No data found.";
            }
            return McpSchema.CallToolResult.builder()
                    .addTextContent(json)
                    .build();
        } catch (Exception e) {
            log.error("Error executing dynamic tool query: {}", e.getMessage(), e);
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Error: " + e.getMessage())
                    .isError(true)
                    .build();
        }
    }

    private Map<String, Object> validateAndConvertParameters(List<ToolParameter> paramDefs,
                                                              Map<String, Object> arguments) {
        Set<String> definedNames = paramDefs.stream()
                .map(ToolParameter::name)
                .collect(Collectors.toSet());

        Map<String, Object> result = new LinkedHashMap<>();

        for (ToolParameter param : paramDefs) {
            Object value = arguments.get(param.name());

            if (value == null && param.required()) {
                throw new IllegalArgumentException("Missing required parameter: " + param.name());
            }

            if (value == null) {
                continue;
            }

            result.put(param.name(), convertValue(param, value));
        }

        return result;
    }

    private Object convertValue(ToolParameter param, Object value) {
        return switch (param.type().toLowerCase()) {
            case "integer" -> {
                if (value instanceof Number n) {
                    yield n.intValue();
                }
                try {
                    yield Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Parameter '" + param.name() + "' must be an integer, got: " + value);
                }
            }
            case "number" -> {
                if (value instanceof Number n) {
                    yield n.doubleValue();
                }
                try {
                    yield Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Parameter '" + param.name() + "' must be a number, got: " + value);
                }
            }
            default -> value.toString();
        };
    }

    private String ensureLimit(String sql) {
        if (!sql.toUpperCase().contains("LIMIT")) {
            return sql + " LIMIT " + DEFAULT_LIMIT;
        }
        return sql;
    }

    private String mapParamType(String paramType) {
        return switch (paramType.toLowerCase()) {
            case "integer" -> "integer";
            case "number" -> "number";
            case "boolean" -> "boolean";
            default -> "string";
        };
    }

    public record RefreshResult(int added, int removed, int skipped, List<String> errors) {}
}
