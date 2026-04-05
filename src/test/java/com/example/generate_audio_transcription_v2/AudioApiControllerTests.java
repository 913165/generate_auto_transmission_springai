package com.example.generate_audio_transcription_v2;

import com.example.generate_audio_transcription_v2.catalog.ConversationCatalog;
import com.example.generate_audio_transcription_v2.controller.api.AudioApiController;
import com.example.generate_audio_transcription_v2.model.ConversationLine;
import com.example.generate_audio_transcription_v2.model.ConversationScript;
import com.example.generate_audio_transcription_v2.service.ConversationAudioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AudioApiControllerTests {

    @Test
    void shouldReturnOptions() {
        AudioApiController controller = new AudioApiController(mock(ConversationCatalog.class), mock(ConversationAudioService.class));

        Map<String, List<String>> options = controller.options();

        assertThat(options.get("models")).contains("tts-1", "tts-1-hd");
        assertThat(options.get("voices")).contains("alloy", "echo");
    }

    @Test
    void shouldGenerateSingleMp3() {
        ConversationCatalog catalog = mock(ConversationCatalog.class);
        ConversationAudioService service = mock(ConversationAudioService.class);
        AudioApiController controller = new AudioApiController(catalog, service);

        ConversationScript script = new ConversationScript(
                "internet-not-working-complaint",
                "Internet Not Working Complaint",
                "desc",
                List.of(new ConversationLine("Rahul", "alloy", "hello"))
        );

        when(catalog.findById("internet-not-working-complaint")).thenReturn(Optional.of(script));
        when(service.generateConversationAudio(eq("internet-not-working-complaint"), eq("tts-1"), anyMap()))
                .thenReturn("abc".getBytes());

        ResponseEntity<byte[]> response = controller.generateSingle(
                "internet-not-working-complaint",
                new AudioApiController.GenerateAudioRequest("tts-1", Map.of("Rahul", "alloy"))
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("audio/mpeg"));
        assertThat(response.getBody()).isEqualTo("abc".getBytes());
    }

    @Test
    void shouldReturnConversations() {
        ConversationCatalog catalog = mock(ConversationCatalog.class);
        when(catalog.getAll()).thenReturn(Map.of(
                "internet-not-working-complaint",
                new ConversationScript("internet-not-working-complaint", "Internet Not Working Complaint", "desc", List.of())
        ));

        AudioApiController controller = new AudioApiController(catalog, mock(ConversationAudioService.class));

        List<ConversationScript> scripts = controller.conversations();

        assertThat(scripts).hasSize(1);
        assertThat(scripts.get(0).id()).isEqualTo("internet-not-working-complaint");
    }
}
