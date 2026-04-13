package com.vando.mcp_test.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TableRegistryService {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,127}$");

    private final JdbcTemplate jdbc;

    public TableRegistryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listTables() {
        return jdbc.queryForList(
                "SELECT TABLE_NAME, DESCRIPTION, CREATED_AT FROM TABLE_REGISTRY ORDER BY TABLE_NAME");
    }

    public List<Map<String, Object>> describeTable(String tableName) {
        validateName(tableName);
        return jdbc.queryForList(
                "SELECT COLUMN_NAME, DATA_TYPE, DESCRIPTION, NULLABLE, PRIMARY_KEY " +
                "FROM COLUMN_REGISTRY WHERE TABLE_NAME = ? ORDER BY ORDINAL",
                tableName.toUpperCase());
    }

    public boolean tableExists(String tableName) {
        validateName(tableName);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM TABLE_REGISTRY WHERE TABLE_NAME = ?",
                Integer.class, tableName.toUpperCase());
        return count != null && count > 0;
    }

    @Transactional
    public void registerTable(String tableName, String description, List<ColumnDefinition> columns) {
        validateName(tableName);
        columns.forEach(col -> validateName(col.name()));

        String upperName = tableName.toUpperCase();

        jdbc.update("INSERT INTO TABLE_REGISTRY (TABLE_NAME, DESCRIPTION) VALUES (?, ?)",
                upperName, description);

        int ordinal = 1;
        for (ColumnDefinition col : columns) {
            jdbc.update(
                    "INSERT INTO COLUMN_REGISTRY (TABLE_NAME, COLUMN_NAME, DATA_TYPE, DESCRIPTION, NULLABLE, PRIMARY_KEY, ORDINAL) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    upperName, col.name().toUpperCase(), col.dataType(), col.description(),
                    col.nullable(), col.primaryKey(), ordinal++);
        }

        String ddl = buildCreateTableDdl(upperName, columns);
        jdbc.execute(ddl);
    }

    @Transactional
    public void removeTable(String tableName) {
        validateName(tableName);
        String upperName = tableName.toUpperCase();

        jdbc.execute("DROP TABLE IF EXISTS " + upperName);
        jdbc.update("DELETE FROM COLUMN_REGISTRY WHERE TABLE_NAME = ?", upperName);
        jdbc.update("DELETE FROM TABLE_REGISTRY WHERE TABLE_NAME = ?", upperName);
    }

    private String buildCreateTableDdl(String tableName, List<ColumnDefinition> columns) {
        String columnsDdl = columns.stream()
                .map(col -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(col.name().toUpperCase()).append(" ").append(col.dataType());
                    if (col.primaryKey()) {
                        sb.append(" PRIMARY KEY");
                    }
                    if (!col.nullable() && !col.primaryKey()) {
                        sb.append(" NOT NULL");
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining(", "));

        return "CREATE TABLE " + tableName + " (" + columnsDdl + ")";
    }

    private void validateName(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid identifier: '" + name + "'. Only letters, digits, and underscores are allowed.");
        }
    }

    public record ColumnDefinition(
            String name,
            String dataType,
            String description,
            boolean nullable,
            boolean primaryKey
    ) {}
}
