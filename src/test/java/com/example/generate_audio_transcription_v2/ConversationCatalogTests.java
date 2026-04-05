package com.example.generate_audio_transcription_v2;

import com.example.generate_audio_transcription_v2.catalog.ConversationCatalog;
import com.example.generate_audio_transcription_v2.model.ConversationScript;
import com.example.generate_audio_transcription_v2.parser.ConversationScriptParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationCatalogTests {

    private final ConversationScriptParser parser = new ConversationScriptParser();

    @Test
    void shouldExposeThreeScripts() {
        Map<String, ConversationScript> scripts = parser.parse(sampleScript());
        ConversationCatalog catalog = new ConversationCatalog(scripts);

        assertThat(catalog.getAll()).hasSize(3);
        assertThat(catalog.findById("internet-not-working-complaint")).isPresent();
        assertThat(catalog.findById("credit-card-wrong-charge-issue")).isPresent();
        assertThat(catalog.findById("e-commerce-product-return-request")).isPresent();
    }

    @Test
    void shouldReturnOrderedSpeakerDefaults() {
        Map<String, ConversationScript> scripts = parser.parse(sampleScript());
        ConversationCatalog catalog = new ConversationCatalog(scripts);
        ConversationScript script = catalog.findById("internet-not-working-complaint").orElseThrow();

        assertThat(script.speakerDefaults())
                .containsEntry("Rahul", "alloy")
                .containsEntry("Amit", "echo")
                .hasSize(2);
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
