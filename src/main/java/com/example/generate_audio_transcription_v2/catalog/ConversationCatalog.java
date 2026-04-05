package com.example.generate_audio_transcription_v2.catalog;

import com.example.generate_audio_transcription_v2.model.ConversationScript;
import com.example.generate_audio_transcription_v2.parser.ConversationScriptParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
public class ConversationCatalog {

    private final Map<String, ConversationScript> scripts;

    @Autowired
    public ConversationCatalog(
            ConversationScriptParser parser,
            ResourceLoader resourceLoader,
            @Value("${app.scripts.path:classpath:samle_scripts.txt}") String scriptsPath
    ) {
        this.scripts = loadScripts(parser, resourceLoader, scriptsPath);
    }

    public ConversationCatalog(Map<String, ConversationScript> scripts) {
        this.scripts = Map.copyOf(scripts);
    }

    public Map<String, ConversationScript> getAll() {
        return scripts;
    }

    public Optional<ConversationScript> findById(String id) {
        return Optional.ofNullable(scripts.get(id));
    }

    private static Map<String, ConversationScript> loadScripts(
            ConversationScriptParser parser,
            ResourceLoader resourceLoader,
            String scriptsPath
    ) {
        try {
            Resource resource = resourceLoader.getResource(scriptsPath);
            if (!resource.exists()) {
                throw new IllegalStateException("Scripts file not found: " + scriptsPath);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return parser.parse(content);
            }
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to load scripts from " + scriptsPath, ex);
        }
    }
}
