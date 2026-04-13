package com.vando.mcp_test.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Provides a {@link DynamicDataSource} and the associated {@link JdbcTemplate} /
 * {@link NamedParameterJdbcTemplate} beans used for user-facing data queries.
 *
 * <p>The primary (H2) {@link DataSource} and its auto-configured templates remain
 * unchanged and are used exclusively for internal registry tables
 * (TABLE_REGISTRY, TOOL_REGISTRY, …).
 */
@Configuration
public class DataSourceConfiguration {

    @Value("${spring.datasource.url:jdbc:h2:mem:mcpdb}")
    private String url;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    /**
     * The dynamic datasource that wraps a fresh connection to the configured default
     * datasource. It can be switched at runtime via {@link DatabaseConfigService#saveAndApply}.
     */
    @Bean
    public DynamicDataSource dynamicDataSource() {
        DataSource initial = DataSourceBuilder.create()
                .url(url)
                .driverClassName(driverClassName)
                .username(username)
                .password(password)
                .build();
        return new DynamicDataSource(initial);
    }

    /** JdbcTemplate backed by the dynamic datasource – used by {@code DataQueryService}. */
    @Bean("queryJdbcTemplate")
    public JdbcTemplate queryJdbcTemplate(DynamicDataSource dynamicDataSource) {
        return new JdbcTemplate(dynamicDataSource);
    }

    /** NamedParameterJdbcTemplate backed by the dynamic datasource – used by {@code DynamicToolManager}. */
    @Bean("queryNamedJdbcTemplate")
    public NamedParameterJdbcTemplate queryNamedJdbcTemplate(DynamicDataSource dynamicDataSource) {
        return new NamedParameterJdbcTemplate(dynamicDataSource);
    }
}
