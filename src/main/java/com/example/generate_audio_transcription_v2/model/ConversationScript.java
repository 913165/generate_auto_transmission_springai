package com.example.generate_audio_transcription_v2.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ConversationScript(String id, String title, String description, List<ConversationLine> lines) {

    public Map<String, String> speakerDefaults() {
        Map<String, String> defaults = new LinkedHashMap<>();
        for (ConversationLine line : lines) {
            defaults.putIfAbsent(line.speaker(), line.defaultVoice());
        }
        return defaults;
    }
}
