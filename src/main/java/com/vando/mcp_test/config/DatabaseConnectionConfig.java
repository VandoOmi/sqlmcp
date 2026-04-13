package com.vando.mcp_test.config;

/**
 * Holds the settings for an external database connection.
 */
public class DatabaseConnectionConfig {

    private String url;
    private String driverClassName;
    private String username;
    private String password;

    public DatabaseConnectionConfig() {}

    public DatabaseConnectionConfig(String url, String driverClassName, String username, String password) {
        this.url = url;
        this.driverClassName = driverClassName;
        this.username = username;
        this.password = password;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
