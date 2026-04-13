package com.vando.mcp_test.mcp;

import tools.jackson.databind.ObjectMapper;
import com.vando.mcp_test.service.DataQueryService;
import com.vando.mcp_test.service.TableRegistryService;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabasePrompts {

    private final TableRegistryService registry;
    private final DataQueryService queryService;
    private final ObjectMapper objectMapper;

    public DatabasePrompts(TableRegistryService registry, DataQueryService queryService, ObjectMapper objectMapper) {
        this.registry = registry;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
    }

    @McpPrompt(name = "data-analysis",
            description = "Generate a prompt to analyze the data in a specific table, including its schema and sample data")
    public GetPromptResult dataAnalysis(
            @McpArg(name = "tableName", description = "Name of the table to analyze", required = true) String tableName) {

        List<Map<String, Object>> columns = registry.describeTable(tableName);
        List<Map<String, Object>> sampleData = queryService.getTableData(tableName, 10);
        int rowCount = queryService.countRows(tableName);

        String promptText = String.format("""
                Please analyze the following database table:
                
                Table: %s
                Total Rows: %d
                
                Schema:
                %s
                
                Sample Data (up to 10 rows):
                %s
                
                Please provide:
                1. A summary of what this table contains
                2. Data quality observations
                3. Interesting patterns or insights
                4. Suggestions for useful queries""",
                tableName.toUpperCase(),
                rowCount,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(columns),
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleData));

        return new GetPromptResult(
                "Data Analysis for " + tableName.toUpperCase(),
                List.of(new PromptMessage(Role.USER, new TextContent(promptText))));
    }

    @McpPrompt(name = "query-helper",
            description = "Generate a prompt that helps with creating database queries by providing all available tables and their schemas")
    public GetPromptResult queryHelper() {

        List<Map<String, Object>> tables = registry.listTables();
        StringBuilder schemaInfo = new StringBuilder();

        for (Map<String, Object> table : tables) {
            String name = (String) table.get("TABLE_NAME");
            schemaInfo.append("\n--- ").append(name).append(" ---\n");
            schemaInfo.append("Description: ").append(table.get("DESCRIPTION")).append("\n");
            schemaInfo.append("Columns:\n");
            List<Map<String, Object>> columns = registry.describeTable(name);
            schemaInfo.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(columns));
            schemaInfo.append("\n");
        }

        String promptText = String.format("""
                You are a database query assistant. Here are the available tables and their schemas:
                
                %s
                
                Help the user create queries using the 'query-table' tool.
                The tool supports filtering (column, operator, value), sorting (orderBy), and limiting results.
                Available operators: =, !=, >, <, >=, <=, LIKE""",
                schemaInfo);

        return new GetPromptResult(
                "Database Query Helper",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(promptText))));
    }
}
