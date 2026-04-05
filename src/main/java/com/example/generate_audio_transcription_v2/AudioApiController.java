package com.example.generate_audio_transcription_v2;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AudioApiController {

    private final ConversationCatalog conversationCatalog;
    private final ConversationAudioService conversationAudioService;

    public AudioApiController(ConversationCatalog conversationCatalog, ConversationAudioService conversationAudioService) {
        this.conversationCatalog = conversationCatalog;
        this.conversationAudioService = conversationAudioService;
    }

    @GetMapping("/options")
    public Map<String, List<String>> options() {
        return Map.of(
                "models", TtsOptions.MODELS,
                "voices", TtsOptions.VOICES
        );
    }

    @GetMapping("/conversations")
    public List<ConversationScript> conversations() {
        return conversationCatalog.getAll().values().stream().toList();
    }

    @PostMapping("/generate/{conversationId}")
    public ResponseEntity<byte[]> generateSingle(
            @PathVariable String conversationId,
            @RequestBody(required = false) GenerateAudioRequest request
    ) {
        String model = request != null && request.model() != null ? request.model() : TtsOptions.MODELS.getFirst();
        Map<String, String> overrides = request != null && request.voiceOverrides() != null ? request.voiceOverrides() : Map.of();

        byte[] audio = conversationAudioService.generateConversationAudio(conversationId, model, overrides);
        ConversationScript script = conversationCatalog.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown conversation id: " + conversationId));

        return asDownload(audio, ConversationAudioService.toFileName(script.title()) + ".mp3", MediaType.valueOf("audio/mpeg"));
    }

    @PostMapping("/generate-all")
    public ResponseEntity<byte[]> generateAll(@RequestBody(required = false) GenerateAllAudioRequest request) {
        String model = request != null && request.model() != null ? request.model() : TtsOptions.MODELS.getFirst();
        Map<String, Map<String, String>> overrides =
                request != null && request.voiceOverrides() != null ? request.voiceOverrides() : Map.of();

        byte[] zip = conversationAudioService.generateAllAsZip(model, overrides);
        return asDownload(zip, "all_conversations.zip", MediaType.APPLICATION_OCTET_STREAM);
    }

    private ResponseEntity<byte[]> asDownload(byte[] data, String fileName, MediaType mediaType) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(data.length)
                .body(data);
    }

    public record GenerateAudioRequest(String model, Map<String, String> voiceOverrides) {
    }

    public record GenerateAllAudioRequest(String model, Map<String, Map<String, String>> voiceOverrides) {
    }
}

