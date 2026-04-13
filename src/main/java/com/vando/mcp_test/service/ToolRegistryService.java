package com.vando.mcp_test.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ToolRegistryService {

    private final JdbcTemplate jdbc;

    public ToolRegistryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ToolDefinition> loadActiveTools() {
        List<Map<String, Object>> tools = jdbc.queryForList(
                "SELECT TOOL_NAME, DESCRIPTION, SQL_QUERY FROM TOOL_REGISTRY WHERE ACTIVE = TRUE ORDER BY TOOL_NAME");

        return tools.stream().map(row -> {
            String toolName = (String) row.get("TOOL_NAME");
            String description = (String) row.get("DESCRIPTION");
            String sqlQuery = (String) row.get("SQL_QUERY");
            List<ToolParameter> parameters = loadParameters(toolName);
            return new ToolDefinition(toolName, description, sqlQuery, parameters);
        }).toList();
    }

    public List<Map<String, Object>> listAllTools() {
        return jdbc.queryForList(
                "SELECT TOOL_NAME, DESCRIPTION, SQL_QUERY, ACTIVE, CREATED_AT FROM TOOL_REGISTRY ORDER BY TOOL_NAME");
    }

    public Map<String, Object> getTool(String toolName) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT TOOL_NAME, DESCRIPTION, SQL_QUERY, ACTIVE, CREATED_AT FROM TOOL_REGISTRY WHERE TOOL_NAME = ?",
                toolName);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public List<ToolParameter> loadParameters(String toolName) {
        return jdbc.queryForList(
                        "SELECT PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED FROM TOOL_PARAMETER WHERE TOOL_NAME = ? ORDER BY ORDINAL",
                        toolName)
                .stream()
                .map(row -> new ToolParameter(
                        (String) row.get("PARAM_NAME"),
                        (String) row.get("PARAM_TYPE"),
                        (String) row.get("DESCRIPTION"),
                        (Boolean) row.get("REQUIRED")))
                .toList();
    }

    @Transactional
    public void createTool(String toolName, String description, String sqlQuery,
                           boolean active, List<ToolParameter> parameters) {
        jdbc.update("INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY, ACTIVE) VALUES (?, ?, ?, ?)",
                toolName, description, sqlQuery, active);

        int ordinal = 1;
        for (ToolParameter param : parameters) {
            jdbc.update("INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) " +
                         "VALUES (?, ?, ?, ?, ?, ?)",
                    toolName, param.name(), param.type(), param.description(), param.required(), ordinal++);
        }
    }

    @Transactional
    public void updateTool(String toolName, String description, String sqlQuery,
                           boolean active, List<ToolParameter> parameters) {
        jdbc.update("UPDATE TOOL_REGISTRY SET DESCRIPTION = ?, SQL_QUERY = ?, ACTIVE = ? WHERE TOOL_NAME = ?",
                description, sqlQuery, active, toolName);

        jdbc.update("DELETE FROM TOOL_PARAMETER WHERE TOOL_NAME = ?", toolName);

        int ordinal = 1;
        for (ToolParameter param : parameters) {
            jdbc.update("INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) " +
                         "VALUES (?, ?, ?, ?, ?, ?)",
                    toolName, param.name(), param.type(), param.description(), param.required(), ordinal++);
        }
    }

    @Transactional
    public void deleteTool(String toolName) {
        jdbc.update("DELETE FROM TOOL_PARAMETER WHERE TOOL_NAME = ?", toolName);
        jdbc.update("DELETE FROM TOOL_REGISTRY WHERE TOOL_NAME = ?", toolName);
    }

    public void setToolActive(String toolName, boolean active) {
        jdbc.update("UPDATE TOOL_REGISTRY SET ACTIVE = ? WHERE TOOL_NAME = ?", active, toolName);
    }

    public record ToolDefinition(
            String name,
            String description,
            String sqlQuery,
            List<ToolParameter> parameters
    ) {}

    public record ToolParameter(
            String name,
            String type,
            String description,
            boolean required
    ) {}
}
