package com.example.generate_audio_transcription_v2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenAiSpeechSynthesisClient implements SpeechSynthesisClient {

    private final RestClient restClient;
    private final String apiKey;

    public OpenAiSpeechSynthesisClient(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${app.openai.speech-url:https://api.openai.com/v1/audio/speech}") String speechUrl
    ) {
        this.apiKey = apiKey;
        this.restClient = restClientBuilder
                .baseUrl(speechUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public byte[] synthesize(String text, String voice, String model) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Missing API key. Set spring.ai.openai.api-key or OPENAI_API_KEY.");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "voice", voice,
                "input", text,
                "response_format", "mp3"
        );

        byte[] body = restClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(payload)
                .retrieve()
                .body(byte[].class);

        if (body == null || body.length == 0) {
            throw new IllegalStateException("OpenAI returned an empty audio payload.");
        }

        return body;
    }
}
