package com.example.generate_audio_transcription_v2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Mp3MergeServiceTests {

    @Test
    void shouldFallbackToConcatenationWhenFfmpegUnavailable() {
        Mp3MergeService mergeService = new Mp3MergeService("definitely-missing-ffmpeg", true);

        byte[] merged = mergeService.mergeSegments(List.of(
                "part-1".getBytes(StandardCharsets.UTF_8),
                "part-2".getBytes(StandardCharsets.UTF_8)
        ));

        assertThat(new String(merged, StandardCharsets.UTF_8)).isEqualTo("part-1part-2");
    }
}

