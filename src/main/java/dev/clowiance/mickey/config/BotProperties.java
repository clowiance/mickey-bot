package dev.clowiance.mickey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
<<<<<<< HEAD
<<<<<<< HEAD
import org.springframework.stereotype.Component;
=======
import org.springframework.context.annotation.Configuration;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
import org.springframework.stereotype.Component;
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

import java.util.List;

@Data
<<<<<<< HEAD
<<<<<<< HEAD
@Component
=======
@Configuration
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
@Component
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String username;
    private String token;
    private List<Long> devIds;
    private String targetGroupUsername;
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

    public void setDevIds(String devIds) {
        if (devIds != null && !devIds.isBlank()) {
            this.devIds = java.util.Arrays.stream(devIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();
        }
    }
<<<<<<< HEAD
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
}
