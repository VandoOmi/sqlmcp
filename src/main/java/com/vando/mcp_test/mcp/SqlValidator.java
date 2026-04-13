package com.vando.mcp_test.mcp;

import java.util.List;
import java.util.regex.Pattern;

public final class SqlValidator {

    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "CREATE",
            "TRUNCATE", "MERGE", "EXEC", "EXECUTE", "CALL", "SET",
            "GRANT", "REVOKE", "COMMIT", "ROLLBACK"
    );

    private static final List<String> BLOCKED_H2_FUNCTIONS = List.of(
            "FILE_READ", "FILE_WRITE", "CSVREAD", "CSVWRITE", "LINK_SCHEMA"
    );

    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\b(%s)\\b",
            Pattern.CASE_INSENSITIVE);

    private SqlValidator() {
    }

    public static void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new SqlValidationException("SQL query must not be empty.");
        }

        String trimmed = sql.strip();

        if (trimmed.contains(";")) {
            throw new SqlValidationException("SQL query must not contain semicolons (statement stacking is not allowed).");
        }

        if (!trimmed.toUpperCase().startsWith("SELECT")) {
            throw new SqlValidationException("SQL query must start with SELECT. Only read operations are allowed.");
        }

        String upper = trimmed.toUpperCase();

        for (String keyword : BLOCKED_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(upper).find()) {
                throw new SqlValidationException("SQL query contains blocked keyword: " + keyword);
            }
        }

        for (String function : BLOCKED_H2_FUNCTIONS) {
            Pattern pattern = Pattern.compile("\\b" + function + "\\s*\\(", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(upper).find()) {
                throw new SqlValidationException("SQL query contains blocked H2 function: " + function);
            }
        }
    }

    public static class SqlValidationException extends RuntimeException {
        public SqlValidationException(String message) {
            super(message);
        }
    }
}
