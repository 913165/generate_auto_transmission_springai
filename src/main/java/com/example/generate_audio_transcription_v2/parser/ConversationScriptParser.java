package com.example.generate_audio_transcription_v2.parser;

import com.example.generate_audio_transcription_v2.model.ConversationLine;
import com.example.generate_audio_transcription_v2.model.ConversationScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConversationScriptParser {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^#\\s*(?:\\d+\\s+)?(.+)$");
    private static final Pattern DIALOGUE_PATTERN = Pattern.compile("^\\*\\*([A-Za-z][A-Za-z ]{0,40}):\\*\\*\\s*(.+)$");
    private static final Map<String, String> DEFAULT_VOICES = Map.of(
            "rahul", "alloy",
            "amit", "echo",
            "neha", "nova",
            "rajesh", "fable",
            "priya", "shimmer",
            "sneha", "onyx"
    );

    public Map<String, ConversationScript> parse(String sourceText) {
        Map<String, ConversationScript> scripts = new LinkedHashMap<>();

        String currentTitle = null;
        String currentDescription = "";
        List<ConversationLine> currentLines = new ArrayList<>();

        for (String rawLine : sourceText.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("---")) {
                continue;
            }

            Matcher titleMatcher = TITLE_PATTERN.matcher(line);
            if (titleMatcher.matches()) {
                if (currentTitle != null && !currentLines.isEmpty()) {
                    addScript(scripts, currentTitle, currentDescription, currentLines);
                }
                currentTitle = titleMatcher.group(1).trim();
                currentDescription = "";
                currentLines = new ArrayList<>();
                continue;
            }

            if (currentTitle == null) {
                continue;
            }

            if (line.startsWith("**Agent") || line.startsWith("**Customer")) {
                currentDescription = stripMarkdown(line);
                continue;
            }

            Matcher dialogueMatcher = DIALOGUE_PATTERN.matcher(line);
            if (dialogueMatcher.matches()) {
                String speaker = dialogueMatcher.group(1).trim();
                String text = normalizeQuotes(dialogueMatcher.group(2).trim());
                String defaultVoice = defaultVoiceFor(speaker, currentLines.size());
                currentLines.add(new ConversationLine(speaker, defaultVoice, text));
            }
        }

        if (currentTitle != null && !currentLines.isEmpty()) {
            addScript(scripts, currentTitle, currentDescription, currentLines);
        }

        if (scripts.isEmpty()) {
            throw new IllegalStateException("No conversations parsed from scripts file.");
        }

        return Map.copyOf(scripts);
    }

    private static void addScript(Map<String, ConversationScript> scripts, String title, String description, List<ConversationLine> lines) {
        String id = toSlug(title);
        scripts.put(id, new ConversationScript(id, title, description, List.copyOf(lines)));
    }

    private static String stripMarkdown(String value) {
        return value.replace("**", "").trim();
    }

    private static String normalizeQuotes(String value) {
        return value
                .replace('\u2019', '\'')
                .replace('\u2018', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u2026', '.');
    }

    private static String defaultVoiceFor(String speaker, int index) {
        String mapped = DEFAULT_VOICES.get(speaker.toLowerCase(Locale.ROOT));
        if (mapped != null) {
            return mapped;
        }
        return (index % 2 == 0) ? "alloy" : "echo";
    }

    private static String toSlug(String title) {
        return title.toLowerCase(Locale.ROOT)
                .replace(' ', '-')
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
