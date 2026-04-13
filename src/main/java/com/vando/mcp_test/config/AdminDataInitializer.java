package com.vando.mcp_test.config;

import com.vando.mcp_test.service.AdminUserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private final AdminUserService adminUserService;

    public AdminDataInitializer(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    public void run(ApplicationArguments args) {
        adminUserService.initDefaultAdmin();
    }
}
