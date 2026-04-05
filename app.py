import os
import streamlit as st
from openai import OpenAI

# ── Page config ──────────────────────────────────────────────────────────────
st.set_page_config(
    page_title="Call Center Audio Generator",
    page_icon="🎧",
    layout="wide",
)

# ── OpenAI client ─────────────────────────────────────────────────────────────
api_key = st.secrets.get("OPENAI_API_KEY", "") or os.environ.get("OPENAI_API_KEY", "")
client = OpenAI(api_key=api_key) if api_key else None

# ── Conversation scripts ──────────────────────────────────────────────────────
CONVERSATIONS = {
    "1️⃣ Internet Not Working Complaint": {
        "description": "Agent **Rahul** helps customer **Amit** with an internet outage issue.",
        "lines": [
            ("Rahul", "alloy",   "Thank you for calling SwiftNet customer support. My name is Rahul. How may I assist you today?"),
            ("Amit",  "echo",    "Hi Rahul, my internet has not been working since morning. I have restarted the router but still no connection."),
            ("Rahul", "alloy",   "I'm sorry for the inconvenience, Amit. Let me check that for you. May I have your registered mobile number?"),
            ("Amit",  "echo",    "Yes, it is 9876543210."),
            ("Rahul", "alloy",   "Thank you. I can see there is a service outage reported in your area due to maintenance work."),
            ("Amit",  "echo",    "Oh okay… but I have an important meeting in 30 minutes."),
            ("Rahul", "alloy",   "I understand how important that is. The expected resolution time is within the next 45 minutes."),
            ("Amit",  "echo",    "Is there anything I can do meanwhile?"),
            ("Rahul", "alloy",   "Yes, you may try connecting through mobile hotspot temporarily. Also, I will mark your connection as priority so it gets restored quickly."),
            ("Amit",  "echo",    "That would be helpful, thank you."),
            ("Rahul", "alloy",   "You're welcome. I will also send you an SMS once the issue is resolved."),
            ("Amit",  "echo",    "Great, appreciate the support."),
            ("Rahul", "alloy",   "Is there anything else I can help you with today?"),
            ("Amit",  "echo",    "No, that's all."),
            ("Rahul", "alloy",   "Thank you for calling SwiftNet. Have a great day!"),
        ],
    },
    "2️⃣ Credit Card Wrong Charge Issue": {
        "description": "Agent **Neha** helps customer **Rajesh** dispute an unauthorized credit card charge.",
        "lines": [
            ("Neha",   "nova",   "Good afternoon, thank you for calling FinTrust Bank. This is Neha speaking. How may I help you?"),
            ("Rajesh", "fable",  "Hi Neha, I noticed an unknown charge of 4,500 rupees on my credit card yesterday."),
            ("Neha",   "nova",   "I understand your concern, Rajesh. Let me help you with that. Could you please confirm the last four digits of your card?"),
            ("Rajesh", "fable",  "Yes, it is 2345."),
            ("Neha",   "nova",   "Thank you. I can see a transaction from an online shopping website. Did you make any purchase recently?"),
            ("Rajesh", "fable",  "No, I did not make this transaction."),
            ("Neha",   "nova",   "Alright, I will block your card immediately to prevent further unauthorized usage."),
            ("Rajesh", "fable",  "Yes please, block it."),
            ("Neha",   "nova",   "Done. I will also raise a dispute request for this transaction."),
            ("Rajesh", "fable",  "How long will it take to get the refund?"),
            ("Neha",   "nova",   "Usually it takes 5 to 7 working days for investigation."),
            ("Rajesh", "fable",  "Okay, will I get a new card?"),
            ("Neha",   "nova",   "Yes, a replacement card will be delivered within 4 working days."),
            ("Rajesh", "fable",  "Thank you for quick help."),
            ("Neha",   "nova",   "You're welcome. Your complaint reference number is FT98231."),
            ("Rajesh", "fable",  "Noted."),
            ("Neha",   "nova",   "Thank you for calling FinTrust Bank. Have a safe day."),
        ],
    },
    "3️⃣ E-commerce Product Return Request": {
        "description": "Agent **Priya** helps customer **Sneha** return a damaged wireless headphone.",
        "lines": [
            ("Priya", "shimmer", "Hello, thank you for contacting ShopEasy support. My name is Priya. How may I assist you today?"),
            ("Sneha", "onyx",    "Hi Priya, I received a damaged product yesterday and I would like to return it."),
            ("Priya", "shimmer", "I'm really sorry to hear that, Sneha. Could you please provide your order number?"),
            ("Sneha", "onyx",    "Yes, the order number is SE458921."),
            ("Priya", "shimmer", "Thank you. May I know what issue you found with the product?"),
            ("Sneha", "onyx",    "The item is a wireless headphone, but the left side speaker is not working."),
            ("Priya", "shimmer", "I apologize for the inconvenience caused. I will arrange a return pickup for you."),
            ("Sneha", "onyx",    "Do I need to pack the product?"),
            ("Priya", "shimmer", "Yes, please keep the product in its original packaging if possible."),
            ("Sneha", "onyx",    "When will the pickup happen?"),
            ("Priya", "shimmer", "The pickup will be scheduled within the next 24 hours."),
            ("Sneha", "onyx",    "Will I get refund or replacement?"),
            ("Priya", "shimmer", "You can choose either option. Which would you prefer?"),
            ("Sneha", "onyx",    "I prefer a replacement."),
            ("Priya", "shimmer", "Noted. The replacement will be shipped once the returned product is picked up."),
            ("Sneha", "onyx",    "Thank you for the support."),
            ("Priya", "shimmer", "You're welcome. Is there anything else I can assist you with?"),
            ("Sneha", "onyx",    "No, that's all."),
            ("Priya", "shimmer", "Thank you for choosing ShopEasy. Have a great day!"),
        ],
    },
}

TTS_VOICES = ["alloy", "echo", "fable", "onyx", "nova", "shimmer"]
TTS_MODELS = ["tts-1", "tts-1-hd"]

# ── Helpers ───────────────────────────────────────────────────────────────────

def synthesize_line(text: str, voice: str, model: str) -> bytes:
    """Call OpenAI TTS and return raw MP3 bytes."""
    response = client.audio.speech.create(
        model=model,
        voice=voice,
        input=text,
        response_format="mp3",
    )
    return response.content


def build_conversation_audio(lines: list, model: str, progress_bar, status_text) -> bytes:
    """Synthesise each line and concatenate into one MP3 byte stream."""
    segments: list[bytes] = []
    total = len(lines)
    for idx, (speaker, voice, text) in enumerate(lines):
        status_text.text(f"🎙️ Synthesising line {idx + 1}/{total}  —  {speaker}: \"{text[:60]}…\"")
        progress_bar.progress((idx + 1) / total)
        audio_bytes = synthesize_line(text, voice, model)
        segments.append(audio_bytes)
    status_text.text("✅ Done!")
    return b"".join(segments)


# ── UI ────────────────────────────────────────────────────────────────────────

st.title("🎧 Call Center Audio Generator")
st.markdown(
    "Generate realistic call-center conversation audio files using **OpenAI Text-to-Speech**. "
    "Each conversation is synthesised line-by-line with distinct voices for the agent and the customer, "
    "then merged into a single downloadable MP3."
)

# ── API key warning ────────────────────────────────────────────────────────────
if not api_key:
    st.error(
        "⚠️ `OPENAI_API_KEY` environment variable is not set. "
        "Please set it before running the app:\n\n"
        "```\nset OPENAI_API_KEY=sk-...\n```"
    )
    st.stop()

st.success("✅ OpenAI API key loaded from environment.")

st.divider()

# ── Settings sidebar ──────────────────────────────────────────────────────────
with st.sidebar:
    st.header("⚙️ Settings")
    tts_model = st.selectbox("TTS Model", TTS_MODELS, index=0,
                              help="tts-1 is faster; tts-1-hd is higher quality.")
    st.markdown("---")
    st.markdown("**Voice assignments per conversation**")
    st.markdown("Voices are pre-assigned to keep agent & customer distinct. "
                "You can override them below per conversation.")

    # Per-conversation voice overrides
    voice_overrides: dict[str, dict[str, str]] = {}
    for conv_name, conv_data in CONVERSATIONS.items():
        st.markdown(f"**{conv_name}**")
        speakers = list({spk: v for spk, v, _ in conv_data["lines"]}.items())
        overrides = {}
        for speaker, default_voice in speakers:
            chosen = st.selectbox(
                f"{speaker}",
                TTS_VOICES,
                index=TTS_VOICES.index(default_voice),
                key=f"{conv_name}_{speaker}",
            )
            overrides[speaker] = chosen
        voice_overrides[conv_name] = overrides

# ── Main area ─────────────────────────────────────────────────────────────────
st.subheader("📋 Conversations")

tabs = st.tabs(list(CONVERSATIONS.keys()))

for tab, (conv_name, conv_data) in zip(tabs, CONVERSATIONS.items()):
    with tab:
        st.markdown(conv_data["description"])
        st.markdown("#### Script Preview")

        # Show the script in a nice table
        for speaker, _, text in conv_data["lines"]:
            role_label = "🧑‍💼 Agent" if conv_data["lines"].index((speaker, _, text)) % 2 == 0 else "🙋 Customer"
            col1, col2 = st.columns([1, 5])
            with col1:
                st.markdown(f"**{speaker}**")
            with col2:
                st.markdown(text)

        st.divider()

        btn_key = f"gen_{conv_name}"
        if st.button(f"🎙️ Generate Audio for this conversation", key=btn_key, type="primary"):
            # Apply voice overrides
            overrides = voice_overrides.get(conv_name, {})
            updated_lines = [
                (spk, overrides.get(spk, voice), txt)
                for spk, voice, txt in conv_data["lines"]
            ]

            with st.spinner("Generating audio…"):
                progress_bar = st.progress(0)
                status_text = st.empty()
                try:
                    audio_data = build_conversation_audio(
                        updated_lines, tts_model, progress_bar, status_text
                    )
                    safe_name = conv_name.split(" ", 1)[1].replace(" ", "_").lower()
                    filename = f"{safe_name}.mp3"

                    st.success(f"✅ Audio generated! ({len(audio_data) / 1024:.1f} KB)")
                    st.audio(audio_data, format="audio/mp3")
                    st.download_button(
                        label=f"⬇️ Download {filename}",
                        data=audio_data,
                        file_name=filename,
                        mime="audio/mpeg",
                        key=f"dl_{conv_name}",
                    )
                except Exception as e:
                    st.error(f"❌ Error generating audio: {e}")

st.divider()

# ── Generate all 3 at once ────────────────────────────────────────────────────
st.subheader("🚀 Generate All 3 Audio Files")
st.markdown("Click the button below to generate all three conversations in sequence.")

if st.button("🎧 Generate All Conversations", type="primary", key="gen_all"):
    all_results: dict[str, bytes] = {}
    overall_progress = st.progress(0)
    overall_status = st.empty()

    for conv_idx, (conv_name, conv_data) in enumerate(CONVERSATIONS.items()):
        overall_status.text(f"Processing conversation {conv_idx + 1}/3: {conv_name}")
        overrides = voice_overrides.get(conv_name, {})
        updated_lines = [
            (spk, overrides.get(spk, voice), txt)
            for spk, voice, txt in conv_data["lines"]
        ]

        inner_progress = st.progress(0)
        inner_status = st.empty()
        try:
            audio_data = build_conversation_audio(
                updated_lines, tts_model, inner_progress, inner_status
            )
            all_results[conv_name] = audio_data
        except Exception as e:
            st.error(f"❌ Failed on '{conv_name}': {e}")
            break

        overall_progress.progress((conv_idx + 1) / 3)

    if len(all_results) == 3:
        overall_status.text("✅ All 3 conversations generated!")
        st.balloons()
        st.markdown("### ⬇️ Download Files")
        for conv_name, audio_data in all_results.items():
            safe_name = conv_name.split(" ", 1)[1].replace(" ", "_").lower()
            filename = f"{safe_name}.mp3"
            col1, col2 = st.columns([3, 1])
            with col1:
                st.audio(audio_data, format="audio/mp3")
            with col2:
                st.download_button(
                    label=f"⬇️ {filename}",
                    data=audio_data,
                    file_name=filename,
                    mime="audio/mpeg",
                    key=f"dl_all_{conv_name}",
                )

