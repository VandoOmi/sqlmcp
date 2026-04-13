package com.vando.mcp_test.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DataQueryService {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,127}$");
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    private final JdbcTemplate jdbc;
    private final TableRegistryService registry;

    public DataQueryService(JdbcTemplate jdbc, TableRegistryService registry) {
        this.jdbc = jdbc;
        this.registry = registry;
    }

    public List<Map<String, Object>> queryTable(String tableName, String filterColumn,
                                                 String filterOperator, String filterValue,
                                                 String orderBy, Integer limit) {
        validateTableAccess(tableName);
        String upperName = tableName.toUpperCase();

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(upperName);
        List<Object> params = new ArrayList<>();

        if (filterColumn != null && !filterColumn.isBlank()) {
            validateName(filterColumn);
            String op = sanitizeOperator(filterOperator);
            sql.append(" WHERE ").append(filterColumn.toUpperCase()).append(" ").append(op).append(" ?");
            params.add(filterValue);
        }

        if (orderBy != null && !orderBy.isBlank()) {
            validateName(orderBy.replace(" ASC", "").replace(" DESC", "").trim());
            sql.append(" ORDER BY ").append(orderBy.toUpperCase());
        }

        int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sql.append(" LIMIT ?");
        params.add(effectiveLimit);

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public int countRows(String tableName) {
        validateTableAccess(tableName);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + tableName.toUpperCase(), Integer.class);
        return count != null ? count : 0;
    }

    public List<Map<String, Object>> getTableData(String tableName, int limit) {
        validateTableAccess(tableName);
        int effectiveLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        return jdbc.queryForList(
                "SELECT * FROM " + tableName.toUpperCase() + " LIMIT ?", effectiveLimit);
    }

    private void validateTableAccess(String tableName) {
        validateName(tableName);
        if (!registry.tableExists(tableName)) {
            throw new IllegalArgumentException("Table '" + tableName + "' is not registered.");
        }
    }

    private void validateName(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid identifier: '" + name + "'. Only letters, digits, and underscores are allowed.");
        }
    }

    private String sanitizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return "=";
        }
        return switch (operator.trim().toUpperCase()) {
            case "=", "EQ" -> "=";
            case "!=", "<>", "NE" -> "<>";
            case ">", "GT" -> ">";
            case "<", "LT" -> "<";
            case ">=", "GE" -> ">=";
            case "<=", "LE" -> "<=";
            case "LIKE" -> "LIKE";
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }
}
