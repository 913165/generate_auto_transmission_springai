package com.example.generate_audio_transcription_v2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class Mp3MergeService {

    private final String ffmpegPath;
    private final boolean useFfmpeg;

    public Mp3MergeService(
            @Value("${app.audio.ffmpeg.path:ffmpeg}") String ffmpegPath,
            @Value("${app.audio.ffmpeg.enabled:true}") boolean useFfmpeg
    ) {
        this.ffmpegPath = ffmpegPath;
        this.useFfmpeg = useFfmpeg;
    }

    public byte[] mergeSegments(List<byte[]> segments) {
        if (segments.isEmpty()) {
            return new byte[0];
        }
        if (segments.size() == 1) {
            return segments.get(0);
        }

        if (!useFfmpeg) {
            return fallbackConcatenate(segments);
        }

        try {
            return mergeWithFfmpeg(segments);
        }
        catch (Exception ex) {
            return fallbackConcatenate(segments);
        }
    }

    private byte[] mergeWithFfmpeg(List<byte[]> segments) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("merge-mp3-");
        try {
            List<Path> inputFiles = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                Path input = tempDir.resolve(String.format("segment-%03d.mp3", i + 1));
                Files.write(input, segments.get(i));
                inputFiles.add(input);
            }

            Path concatList = tempDir.resolve("concat.txt");
            StringBuilder listContent = new StringBuilder();
            for (Path input : inputFiles) {
                listContent.append("file '")
                        .append(input.toAbsolutePath().toString().replace("'", "''"))
                        .append("'\n");
            }
            Files.writeString(concatList, listContent.toString(), StandardCharsets.UTF_8);

            Path output = tempDir.resolve("merged.mp3");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", concatList.toAbsolutePath().toString(),
                    "-c", "copy",
                    output.toAbsolutePath().toString()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0 || !Files.exists(output)) {
                throw new IOException("ffmpeg merge failed with exit code " + exitCode);
            }

            return Files.readAllBytes(output);
        }
        finally {
            deleteTreeQuietly(tempDir);
        }
    }

    private static byte[] fallbackConcatenate(List<byte[]> segments) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] segment : segments) {
            output.writeBytes(segment);
        }
        return output.toByteArray();
    }

    private static void deleteTreeQuietly(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                }
                catch (IOException ignored) {
                }
            });
        }
        catch (IOException ignored) {
        }
    }
}
