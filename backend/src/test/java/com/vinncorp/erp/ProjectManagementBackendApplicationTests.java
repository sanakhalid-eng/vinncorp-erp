package com.vinncorp.erp;

import com.vinncorp.erp.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProjectManagementBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}

