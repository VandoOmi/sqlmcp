package com.vando.mcp_test.mcp;

import tools.jackson.databind.ObjectMapper;
import com.vando.mcp_test.service.DataQueryService;
import com.vando.mcp_test.service.TableRegistryService;
import com.vando.mcp_test.service.TableRegistryService.ColumnDefinition;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseTools {

    private final TableRegistryService registry;
    private final DataQueryService queryService;
    private final ObjectMapper objectMapper;

    public DatabaseTools(TableRegistryService registry, DataQueryService queryService, ObjectMapper objectMapper) {
        this.registry = registry;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
    }

    @McpTool(name = "list-tables",
            description = "List all registered database tables with their descriptions")
    public String listTables() {
        List<Map<String, Object>> tables = registry.listTables();
        if (tables.isEmpty()) {
            return "No tables registered.";
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tables);
    }

    @McpTool(name = "describe-table",
            description = "Show column definitions for a specific table including name, data type, nullable, and primary key information")
    public String describeTable(
            @McpToolParam(description = "Name of the table to describe", required = true) String tableName) {
        List<Map<String, Object>> columns = registry.describeTable(tableName);
        if (columns.isEmpty()) {
            return "Table '" + tableName + "' not found or has no columns.";
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(columns);
    }

    @McpTool(name = "query-table",
            description = "Query data from a registered table with optional filtering, sorting, and row limit")
    public String queryTable(
            @McpToolParam(description = "Name of the table to query", required = true) String tableName,
            @McpToolParam(description = "Column name to filter on (optional)", required = false) String filterColumn,
            @McpToolParam(description = "Filter operator: =, !=, >, <, >=, <=, LIKE (default: =)", required = false) String filterOperator,
            @McpToolParam(description = "Value to filter by (optional)", required = false) String filterValue,
            @McpToolParam(description = "Column name to sort by, optionally with ASC or DESC (optional)", required = false) String orderBy,
            @McpToolParam(description = "Maximum number of rows to return (default: 100, max: 1000)", required = false) Integer limit) {
        List<Map<String, Object>> rows = queryService.queryTable(
                tableName, filterColumn, filterOperator, filterValue, orderBy, limit);
        if (rows.isEmpty()) {
            return "No data found.";
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
    }

    @McpTool(name = "count-rows",
            description = "Count the number of rows in a registered table")
    public String countRows(
            @McpToolParam(description = "Name of the table", required = true) String tableName) {
        int count = queryService.countRows(tableName);
        return "Table '" + tableName.toUpperCase() + "' has " + count + " rows.";
    }

    @McpTool(name = "register-table",
            description = "Register and create a new database table. Provide column definitions as a JSON array: [{\"name\":\"ID\",\"dataType\":\"INT\",\"description\":\"Primary key\",\"nullable\":false,\"primaryKey\":true}]")
    public String registerTable(
            @McpToolParam(description = "Name for the new table", required = true) String tableName,
            @McpToolParam(description = "Description of the table", required = true) String description,
            @McpToolParam(description = "JSON array of column definitions with fields: name, dataType, description, nullable, primaryKey", required = true) String columnsJson) {
        List<ColumnDefinition> columns = objectMapper.readValue(columnsJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ColumnDefinition.class));

        if (columns.isEmpty()) {
            return "Error: At least one column definition is required.";
        }

        registry.registerTable(tableName, description, columns);
        return "Table '" + tableName.toUpperCase() + "' successfully created and registered with " + columns.size() + " columns.";
    }
}
