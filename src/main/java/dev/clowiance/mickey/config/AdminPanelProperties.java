package dev.clowiance.mickey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "admin-panel")
public class AdminPanelProperties {
    private boolean enabled;
    private String username = "admin";
    private String password;
}
