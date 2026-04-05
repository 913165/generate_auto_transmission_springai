package com.example.generate_audio_transcription_v2;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationScriptParserTests {

    private final ConversationScriptParser parser = new ConversationScriptParser();

    @Test
    void shouldParseConversationsFromMarkdown() {
        Map<String, ConversationScript> scripts = parser.parse("""
                # 1 Internet Not Working Complaint
                **Agent (Rahul)** and **Customer (Amit)**
                **Rahul:** Thank you for calling support.
                **Amit:** Internet is not working.
                ---
                # 2 Credit Card Wrong Charge Issue
                **Agent (Neha)** and **Customer (Rajesh)**
                **Neha:** Good afternoon.
                **Rajesh:** I see an unknown transaction.
                """);

        assertThat(scripts).hasSize(2);
        assertThat(scripts.get("internet-not-working-complaint").lines()).hasSize(2);
        assertThat(scripts.get("internet-not-working-complaint").speakerDefaults())
                .containsEntry("Rahul", "alloy")
                .containsEntry("Amit", "echo");
    }
}

