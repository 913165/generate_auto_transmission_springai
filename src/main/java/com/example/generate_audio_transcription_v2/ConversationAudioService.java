package com.example.generate_audio_transcription_v2;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ConversationAudioService {

    private final ConversationCatalog conversationCatalog;
    private final SpeechSynthesisClient speechSynthesisClient;
    private final Mp3MergeService mp3MergeService;

    public ConversationAudioService(
            ConversationCatalog conversationCatalog,
            SpeechSynthesisClient speechSynthesisClient,
            Mp3MergeService mp3MergeService
    ) {
        this.conversationCatalog = conversationCatalog;
        this.speechSynthesisClient = speechSynthesisClient;
        this.mp3MergeService = mp3MergeService;
    }

    public byte[] generateConversationAudio(String conversationId, String model, Map<String, String> voiceOverrides) {
        ConversationScript script = conversationCatalog.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown conversation id: " + conversationId));

        return mergeLines(script.lines(), model, voiceOverrides);
    }

    public byte[] generateAllAsZip(String model, Map<String, Map<String, String>> allOverrides) {
        Map<String, byte[]> generated = new LinkedHashMap<>();

        for (ConversationScript script : conversationCatalog.getAll().values()) {
            Map<String, String> overrides = allOverrides.getOrDefault(script.id(), Map.of());
            generated.put(toFileName(script.title()) + ".mp3", mergeLines(script.lines(), model, overrides));
        }

        return zipFiles(generated);
    }

    private byte[] mergeLines(List<ConversationLine> lines, String model, Map<String, String> overrides) {
        List<byte[]> segments = new ArrayList<>();

        for (ConversationLine line : lines) {
            String voice = overrides.getOrDefault(line.speaker(), line.defaultVoice());
            byte[] part = speechSynthesisClient.synthesize(line.text(), voice, model);
            segments.add(part);
        }

        return mp3MergeService.mergeSegments(segments);
    }

    private byte[] zipFiles(Map<String, byte[]> files) {
        try (ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(zipBytes)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(entry.getValue());
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return zipBytes.toByteArray();
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to build zip file", ex);
        }
    }

    public static String toFileName(String title) {
        return title.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replaceAll("[^a-z0-9_]", "")
                .replaceAll("_+", "_");
    }
}
