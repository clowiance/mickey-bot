package dev.clowiance.mickey.config;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "bot")
@Validated
public class BotProperties {
    @NotBlank
    private String username;
    @NotBlank
    private String token;
    private List<Long> devIds = List.of();

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
