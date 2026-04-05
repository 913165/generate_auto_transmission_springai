# Call Center Audio Generator (Spring AI) 

This project is a Spring Boot web app equivalent of the Streamlit script in `app.py`.
It generates call-center conversation audio (MP3) with OpenAI text-to-speech.

## Features

- Conversations loaded dynamically from `src/main/resources/samle_scripts.txt`
- Per-speaker voice overrides (`alloy`, `echo`, `fable`, `onyx`, `nova`, `shimmer`)
- Generate a single conversation MP3
- Generate all conversations and download as ZIP
- True MP3 merge through `ffmpeg` concat when available
- Safe fallback to byte concatenation when `ffmpeg` is unavailable
- Thymeleaf UI at `/`
- REST API under `/api`

## Project Structure

- `src/main/java/com/example/generate_audio_transcription_v2/parser/ConversationScriptParser.java`: parses markdown script file
- `src/main/java/com/example/generate_audio_transcription_v2/catalog/ConversationCatalog.java`: dynamic conversation catalog
- `src/main/java/com/example/generate_audio_transcription_v2/tts/OpenAiSpeechSynthesisClient.java`: OpenAI TTS HTTP client
- `src/main/java/com/example/generate_audio_transcription_v2/service/Mp3MergeService.java`: ffmpeg/fallback merge logic
- `src/main/java/com/example/generate_audio_transcription_v2/service/ConversationAudioService.java`: synthesis + packaging logic
- `src/main/java/com/example/generate_audio_transcription_v2/controller/web/AudioWebController.java`: web endpoints
- `src/main/java/com/example/generate_audio_transcription_v2/controller/api/AudioApiController.java`: REST endpoints
- `src/main/java/com/example/generate_audio_transcription_v2/model/`: shared model records and constants
- `src/main/resources/templates/index.html`: UI
- `src/main/resources/static/styles.css`: page styles

## Configuration

Set API key via environment variable (recommended):

```powershell
$env:OPENAI_API_KEY="sk-..."
```

Or in `src/main/resources/application.properties`:

```ini
spring.ai.openai.api-key=sk-...
```

Optional configuration:

```ini
app.scripts.path=classpath:samle_scripts.txt
app.audio.ffmpeg.enabled=true
app.audio.ffmpeg.path=ffmpeg
```

## REST API

- `GET /api/options` -> available models and voices
- `GET /api/conversations` -> parsed conversation catalog
- `POST /api/generate/{conversationId}` -> returns MP3 attachment
- `POST /api/generate-all` -> returns ZIP attachment

`POST /api/generate/{conversationId}` request body example:

```json
{
  "model": "tts-1",
  "voiceOverrides": {
    "Rahul": "alloy",
    "Amit": "nova"
  }
}
```

## Run

```powershell
cd C:\Users\tinum\IdeaProjects\generate_audio_transcription_V2
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`.

## Test

```powershell
cd C:\Users\tinum\IdeaProjects\generate_audio_transcription_V2
.\mvnw.cmd test
```

Note: app startup does not require API key, but generating audio does.
