package com.vando.mcp_test.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Manages the external database connection configuration.
 * <p>
 * Settings are persisted as JSON at the path configured by
 * {@code sqlmcp.db-config-path} (default: {@code db-connection.json}
 * in the working directory). On startup the saved config is automatically
 * applied to the {@link DynamicDataSource} if present.
 */
@Service
public class DatabaseConfigService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfigService.class);

    private final ObjectMapper objectMapper;
    private final DynamicDataSource dynamicDataSource;

    @Value("${sqlmcp.db-config-path:db-connection.json}")
    private String configFilePath;

    // Defaults read from application.properties for the "reset to default" operation
    @Value("${spring.datasource.url:jdbc:h2:mem:mcpdb}")
    private String defaultUrl;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String defaultDriverClass;

    @Value("${spring.datasource.username:sa}")
    private String defaultUsername;

    public DatabaseConfigService(ObjectMapper objectMapper, DynamicDataSource dynamicDataSource) {
        this.objectMapper = objectMapper;
        this.dynamicDataSource = dynamicDataSource;
    }

    @PostConstruct
    public void init() {
        DatabaseConnectionConfig config = loadSavedConfig();
        if (config != null) {
            try {
                DataSource ds = buildDataSource(config);
                dynamicDataSource.switchTo(ds);
                log.info("Applied saved database config: {}", config.getUrl());
            } catch (Exception e) {
                log.warn("Failed to apply saved DB config, using default: {}", e.getMessage());
            }
        }
    }

    /**
     * Returns the saved config without the password, or the H2 defaults if no config is saved.
     */
    public DatabaseConnectionConfig getCurrentConfigWithoutPassword() {
        DatabaseConnectionConfig config = loadSavedConfig();
        if (config == null) {
            config = buildDefaultConfig();
        }
        config.setPassword(null);
        return config;
    }

    /**
     * Tests a connection with the provided config.
     *
     * @return a human-readable database product string
     * @throws Exception if the connection fails
     */
    public String testConnection(DatabaseConnectionConfig config) throws Exception {
        DataSource ds = buildDataSource(config);
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
        }
    }

    /**
     * Saves the configuration to disk and immediately applies it to the dynamic datasource.
     * The connection is tested before persisting.
     */
    public void saveAndApply(DatabaseConnectionConfig config) throws Exception {
        DataSource ds = buildDataSource(config);
        try (Connection conn = ds.getConnection()) {
            // just verifying – connection is closed immediately
        }
        Path path = Path.of(configFilePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(path, objectMapper.writeValueAsBytes(config));
        dynamicDataSource.switchTo(ds);
        log.info("Database config saved and applied: {}", config.getUrl());
    }

    /**
     * Resets the dynamic datasource back to the default H2 datasource and removes the config file.
     */
    public void resetToDefault() {
        DataSource defaultDs = buildDataSource(buildDefaultConfig());
        dynamicDataSource.switchTo(defaultDs);
        try {
            Files.deleteIfExists(Path.of(configFilePath));
            log.info("Database config reset to default H2");
        } catch (IOException e) {
            log.warn("Could not delete DB config file: {}", e.getMessage());
        }
    }

    /** Returns {@code true} when a custom external config file is present. */
    public boolean isExternalConfigActive() {
        return Files.exists(Path.of(configFilePath));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    DatabaseConnectionConfig loadSavedConfig() {
        Path path = Path.of(configFilePath);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            return objectMapper.readValue(bytes, DatabaseConnectionConfig.class);
        } catch (Exception e) {
            log.warn("Failed to read DB config file: {}", e.getMessage());
            return null;
        }
    }

    private DataSource buildDataSource(DatabaseConnectionConfig config) {
        validateJdbcUrl(config.getUrl());
        validateDriverClassName(config.getDriverClassName());
        return DataSourceBuilder.create()
                .url(config.getUrl())
                .driverClassName(config.getDriverClassName())
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
    }

    /**
     * Ensures the URL starts with the {@code jdbc:} scheme to prevent
     * SSRF via non-database protocols.
     */
    private void validateJdbcUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("JDBC URL must not be empty");
        }
        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new IllegalArgumentException("JDBC URL must start with 'jdbc:' (got: " + url + ")");
        }
    }

    /**
     * Ensures the driver class name contains only characters valid in a
     * fully-qualified Java class name, preventing class-path injection.
     */
    private void validateDriverClassName(String driverClassName) {
        if (driverClassName == null || driverClassName.isBlank()) {
            throw new IllegalArgumentException("Driver class name must not be empty");
        }
        if (!driverClassName.matches("[A-Za-z][A-Za-z0-9_.]*")) {
            throw new IllegalArgumentException("Invalid driver class name: " + driverClassName);
        }
    }

    private DatabaseConnectionConfig buildDefaultConfig() {
        return new DatabaseConnectionConfig(defaultUrl, defaultDriverClass, defaultUsername, null);
    }
}
