package dev.clowiance.mickey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
<<<<<<< HEAD
import org.springframework.stereotype.Component;
=======
import org.springframework.context.annotation.Configuration;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

import java.util.List;

@Data
<<<<<<< HEAD
@Component
=======
@Configuration
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String username;
    private String token;
    private List<Long> devIds;
    private String targetGroupUsername;
<<<<<<< HEAD
=======

    public void setDevIds(String devIds) {
        if (devIds != null && !devIds.isBlank()) {
            this.devIds = java.util.Arrays.stream(devIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();
        }
    }
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
}
