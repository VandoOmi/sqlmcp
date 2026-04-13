package com.vando.mcp_test.config;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A {@link DataSource} wrapper whose underlying delegate can be swapped at runtime.
 * The primary (H2) datasource is used for registry tables; this datasource is used
 * for user-configured external database queries.
 */
public class DynamicDataSource extends AbstractDataSource {

    private volatile DataSource delegate;

    public DynamicDataSource(DataSource initial) {
        this.delegate = initial;
    }

    /**
     * Replace the current delegate. Thread-safe via volatile write.
     */
    public void switchTo(DataSource newDataSource) {
        this.delegate = newDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }
}
