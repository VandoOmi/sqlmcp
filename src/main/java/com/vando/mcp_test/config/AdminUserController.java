package com.vando.mcp_test.config;

import com.vando.mcp_test.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAdmins() {
        return ResponseEntity.ok(adminUserService.listAdmins());
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Benutzername ist erforderlich"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwort ist erforderlich"));
        }
        if (adminUserService.exists(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admin '" + username + "' existiert bereits"));
        }
        adminUserService.createAdmin(username, password);
        return ResponseEntity.ok(Map.of("message", "Admin '" + username + "' erstellt"));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String username, Authentication authentication) {
        if (!adminUserService.exists(username)) {
            return ResponseEntity.notFound().build();
        }
        if (username.equals(authentication.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Eigenes Konto kann nicht gelöscht werden"));
        }
        if (adminUserService.countAdmins() <= 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Der letzte Admin kann nicht gelöscht werden"));
        }
        adminUserService.deleteAdmin(username);
        return ResponseEntity.ok(Map.of("message", "Admin '" + username + "' gelöscht"));
    }

    @PostMapping("/{username}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String username,
                                           @RequestBody Map<String, String> body) {
        if (!adminUserService.exists(username)) {
            return ResponseEntity.notFound().build();
        }
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Neues Passwort ist erforderlich"));
        }
        adminUserService.resetPassword(username, newPassword);
        return ResponseEntity.ok(Map.of("message", "Passwort für '" + username + "' zurückgesetzt"));
    }
}
