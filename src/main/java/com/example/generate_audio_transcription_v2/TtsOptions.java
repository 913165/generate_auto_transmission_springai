package com.example.generate_audio_transcription_v2;

import java.util.List;

public final class TtsOptions {

    public static final List<String> MODELS = List.of("tts-1", "tts-1-hd");
    public static final List<String> VOICES = List.of("alloy", "echo", "fable", "onyx", "nova", "shimmer");

    private TtsOptions() {
    }
}

