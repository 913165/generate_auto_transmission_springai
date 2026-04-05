package com.example.generate_audio_transcription_v2;

import com.example.generate_audio_transcription_v2.catalog.ConversationCatalog;
import com.example.generate_audio_transcription_v2.parser.ConversationScriptParser;
import com.example.generate_audio_transcription_v2.service.ConversationAudioService;
import com.example.generate_audio_transcription_v2.service.Mp3MergeService;
import com.example.generate_audio_transcription_v2.tts.SpeechSynthesisClient;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationAudioServiceTests {

    @Test
    void shouldConcatenateSynthesizedPartsInOrder() {
        ConversationCatalog catalog = new ConversationCatalog(new ConversationScriptParser().parse(sampleScript()));
        SpeechSynthesisClient fake = (text, voice, ignoredModel) -> (voice + ":" + text + "|").getBytes(StandardCharsets.UTF_8);
        Mp3MergeService mergeService = new Mp3MergeService("definitely-missing-ffmpeg", true);
        ConversationAudioService service = new ConversationAudioService(catalog, fake, mergeService);

        byte[] result = service.generateConversationAudio("internet-not-working-complaint", "tts-1", Map.of("Amit", "nova"));
        String output = new String(result, StandardCharsets.UTF_8);

        assertThat(output).contains("alloy:Thank you for calling SwiftNet customer support");
        assertThat(output).contains("nova:Hi Rahul, my internet has not been working since morning");
    }

    @Test
    void shouldCreateZipForAllConversations() {
        ConversationCatalog catalog = new ConversationCatalog(new ConversationScriptParser().parse(sampleScript()));
        SpeechSynthesisClient fake = (ignoredText, voice, model) -> (model + ":" + voice + "\n").getBytes(StandardCharsets.UTF_8);
        Mp3MergeService mergeService = new Mp3MergeService("definitely-missing-ffmpeg", true);
        ConversationAudioService service = new ConversationAudioService(catalog, fake, mergeService);

        byte[] zip = service.generateAllAsZip("tts-1", Map.of());

        assertThat(zip).isNotEmpty();
        assertThat(new String(zip, 0, 2, StandardCharsets.ISO_8859_1)).isEqualTo("PK");
    }

    private static String sampleScript() {
        return """
                # 1 Internet Not Working Complaint
                **Agent (Rahul)** and **Customer (Amit)**
                **Rahul:** Thank you for calling SwiftNet customer support.
                **Amit:** Hi Rahul, my internet has not been working since morning.
                **Rahul:** I am sorry for the inconvenience.
                ---
                # 2 Credit Card Wrong Charge Issue
                **Agent (Neha)** and **Customer (Rajesh)**
                **Neha:** Good afternoon.
                **Rajesh:** I noticed an unknown charge.
                ---
                # 3 E-commerce Product Return Request
                **Agent (Priya)** and **Customer (Sneha)**
                **Priya:** Hello.
                **Sneha:** I received a damaged product.
                """;
    }
}
