package com.vando.mcp_test.mcp;

import tools.jackson.databind.ObjectMapper;
import com.vando.mcp_test.service.DataQueryService;
import com.vando.mcp_test.service.TableRegistryService;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DatabaseResources {

    private final TableRegistryService registry;
    private final DataQueryService queryService;
    private final ObjectMapper objectMapper;

    public DatabaseResources(TableRegistryService registry, DataQueryService queryService, ObjectMapper objectMapper) {
        this.registry = registry;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
    }

    @McpResource(uri = "db://tables",
            name = "Database Tables",
            description = "List of all registered database tables")
    public String getAllTables() {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registry.listTables());
    }

    @McpResource(uri = "db://table/{name}/schema",
            name = "Table Schema",
            description = "Column definitions for a specific table")
    public String getTableSchema(String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("table", name.toUpperCase());
        result.put("columns", registry.describeTable(name));
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    @McpResource(uri = "db://table/{name}/data",
            name = "Table Data",
            description = "Data from a specific table (limited to 100 rows)")
    public String getTableData(String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("table", name.toUpperCase());
        result.put("rowCount", queryService.countRows(name));
        result.put("data", queryService.getTableData(name, 100));
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }
}
