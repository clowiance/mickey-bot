package dev.clowiance.mickey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String username;
    private String token;
    private List<Long> devIds;
    private String targetGroupUsername;
}
