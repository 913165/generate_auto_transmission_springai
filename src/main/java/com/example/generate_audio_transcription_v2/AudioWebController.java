package com.example.generate_audio_transcription_v2;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AudioWebController {

    private final ConversationCatalog conversationCatalog;
    private final ConversationAudioService conversationAudioService;

    public AudioWebController(ConversationCatalog conversationCatalog, ConversationAudioService conversationAudioService) {
        this.conversationCatalog = conversationCatalog;
        this.conversationAudioService = conversationAudioService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("conversations", conversationCatalog.getAll().values());
        model.addAttribute("ttsModels", TtsOptions.MODELS);
        model.addAttribute("ttsVoices", TtsOptions.VOICES);
        model.addAttribute("defaultModel", TtsOptions.MODELS.getFirst());
        return "index";
    }

    @PostMapping("/generate/{conversationId}")
    public ResponseEntity<byte[]> generateSingle(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "tts-1") String model,
            @RequestParam Map<String, String> requestParams
    ) {
        Map<String, String> overrides = extractSpeakerOverrides(requestParams);
        byte[] audio = conversationAudioService.generateConversationAudio(conversationId, model, overrides);

        ConversationScript script = conversationCatalog.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown conversation id: " + conversationId));

        String fileName = ConversationAudioService.toFileName(script.title()) + ".mp3";
        return asDownload(audio, fileName, MediaType.valueOf("audio/mpeg"));
    }

    @PostMapping("/generate-all")
    public ResponseEntity<byte[]> generateAll(
            @RequestParam(defaultValue = "tts-1") String model,
            @RequestParam Map<String, String> requestParams
    ) {
        Map<String, Map<String, String>> allOverrides = extractConversationOverrides(requestParams);
        byte[] zip = conversationAudioService.generateAllAsZip(model, allOverrides);
        return asDownload(zip, "all_conversations.zip", MediaType.APPLICATION_OCTET_STREAM);
    }

    private Map<String, String> extractSpeakerOverrides(Map<String, String> requestParams) {
        Map<String, String> overrides = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (entry.getKey().startsWith("voice_")) {
                String speaker = entry.getKey().substring("voice_".length());
                overrides.put(speaker, entry.getValue());
            }
        }
        return overrides;
    }

    private Map<String, Map<String, String>> extractConversationOverrides(Map<String, String> requestParams) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (!entry.getKey().startsWith("voice_")) {
                continue;
            }

            // voice_<conversationId>__<speaker>
            String raw = entry.getKey().substring("voice_".length());
            String[] parts = raw.split("__", 2);
            if (parts.length != 2) {
                continue;
            }

            result.computeIfAbsent(parts[0], ignored -> new LinkedHashMap<>()).put(parts[1], entry.getValue());
        }

        return result;
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
}
