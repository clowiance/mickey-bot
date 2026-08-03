package dev.clowiance.mickey.administration.presentation.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "bot.username=test_bot",
        "bot.token=123456:test",
        "bot.dev-ids=1",
        "bot.polling-enabled=false",
        "admin-panel.enabled=true",
        "admin-panel.username=integration-admin",
        "admin-panel.password=very-long-test-password"
})
@AutoConfigureMockMvc
class AdminDashboardIntegrationTest {
    private static final Path DATABASE = Path.of(System.getProperty("java.io.tmpdir"),
            "mickey-admin-test-" + UUID.randomUUID() + ".db");

    static {
        DATABASE.toFile().deleteOnExit();
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void protectsAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin").with(httpBasic("integration-admin", "very-long-test-password")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mickey Bot")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Посилання навчальних груп")));
    }
}
