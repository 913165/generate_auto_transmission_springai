package com.example.generate_audio_transcription_v2.tts;

public interface SpeechSynthesisClient {

    byte[] synthesize(String text, String voice, String model);
}
