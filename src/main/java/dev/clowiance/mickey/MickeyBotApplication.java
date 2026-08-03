package dev.clowiance.mickey;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class MickeyBotApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        String timeZone = System.getProperty("APP_TIME_ZONE", "Europe/Kyiv");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timeZone)));
        SpringApplication.run(MickeyBotApplication.class, args);
    }
}
