package dev.clowiance.mickey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String username;
    private String token;
    private List<Long> devIds;

    public void setDevIds(String devIds) {
        if (devIds != null && !devIds.isBlank()) {
            this.devIds = java.util.Arrays.stream(devIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();
        }
    }
}
