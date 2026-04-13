package com.vando.mcp_test.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminUserService implements UserDetailsService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT USERNAME, PASSWORD_HASH FROM ADMIN_USERS WHERE USERNAME = ?", username);
        if (rows.isEmpty()) {
            throw new UsernameNotFoundException("Admin not found: " + username);
        }
        Map<String, Object> row = rows.get(0);
        return User.withUsername((String) row.get("USERNAME"))
                .password((String) row.get("PASSWORD_HASH"))
                .roles("ADMIN")
                .build();
    }

    public List<Map<String, Object>> listAdmins() {
        return jdbc.queryForList(
                "SELECT USERNAME, CREATED_AT FROM ADMIN_USERS ORDER BY USERNAME");
    }

    public void createAdmin(String username, String password) {
        jdbc.update("INSERT INTO ADMIN_USERS (USERNAME, PASSWORD_HASH) VALUES (?, ?)",
                username, passwordEncoder.encode(password));
    }

    public void deleteAdmin(String username) {
        jdbc.update("DELETE FROM ADMIN_USERS WHERE USERNAME = ?", username);
    }

    public void resetPassword(String username, String newPassword) {
        jdbc.update("UPDATE ADMIN_USERS SET PASSWORD_HASH = ? WHERE USERNAME = ?",
                passwordEncoder.encode(newPassword), username);
    }

    public boolean exists(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ADMIN_USERS WHERE USERNAME = ?", Integer.class, username);
        return count != null && count > 0;
    }

    public int countAdmins() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM ADMIN_USERS", Integer.class);
        return count != null ? count : 0;
    }

    public void initDefaultAdmin() {
        if (countAdmins() == 0) {
            createAdmin("admin", "admin");
        }
    }
}
