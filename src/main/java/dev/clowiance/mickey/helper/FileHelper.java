package dev.clowiance.mickey.helper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileHelper {

    public Set<String> readWordsFromFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                return Collections.emptySet();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                String[] words = content.split(";");
                Set<String> loaded = new HashSet<>();
                for (String word : words) {
                    String cleaned = word.trim().toLowerCase();
                    if (!cleaned.isBlank()) {
                        loaded.add(cleaned);
                    }
                }
                return loaded;
            }
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }
}
