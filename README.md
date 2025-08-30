# Transcribily

**Transcribily** is a **Kotlin Multiplatform** app (Android + iOS) for **transcribing** and **live captioning** using **local Whisper** (no cloud upload). It supports input from **video**, **audio**, and **microphone**, and exposes a shared expect/actual engine for both platforms.

> Based on a KMP starter template, adapted to this repository's architecture, modules, and utilities.

---

## ✨ Features

* **Local Whisper** via GGML / whisper.cpp (offline).
* **Transcription of local files**: video (`.mp4`, `.mov`, …) and audio (`.m4a`, `.wav`, `.mp3`, …).
* **Real‑time captions** from the microphone (on‑device).
* **Audio extraction from video**: Android with **Media3 Transformer** (AAC `.m4a`), iOS with **AVAssetExportSession**.
* **KMP architecture** with `expect/actual` for the transcription engine.
* **Compose Multiplatform** shared UI; **Material 3**.
* **Koin DI** and MVVM/MVI pattern.

---

## 🏗️ Project Structure

```
composeApp/
  src/
    commonMain/
      kotlin/
        com/transcribily/
          core/
          utils/
          feature/transcription/
            core/               # Common expect API + models
            domain/             # Use cases
            di/                 # Koin module
          ui/
            screens/transcription/  # VM + Compose screens
    androidMain/
      kotlin/
        com/transcribily/feature/transcription/android/   # Android actual
      res/
      AndroidManifest.xml
    iosMain/
      kotlin/
        com/transcribily/feature/transcription/ios/       # iOS actual (Kotlin)
  build.gradle.kts

iosApp/                        # Xcode project (if applicable)

ggml/                          # GGML models (not committed by default)
lib/                           # Native binaries/artifacts (if any)
whisper.cpp/                   # Local native source (submodule/folder)
```

> If your template includes `starter_feaures/` (auth, etc.), keep it and add `feature/transcription` following the structure above.

---

## 🔧 Key Dependencies

### Android

* **Media3 Transformer** to extract audio from video (`.m4a`/AAC).
* **AudioRecord** / AAudio (if you implement low‑latency mic) or capture with `AudioRecord` and feed Whisper.
* **Kotlinx Coroutines** and **Koin**.

### iOS

* **AVFoundation** (audio extraction from video) and **Speech** (optional fallback; default path uses local Whisper).
* **whisper.cpp** built for iOS (arm64 + simulator) as a **static lib** or **XCFramework**.

---

## 🔐 Permissions

### Android (AndroidManifest.xml)

* `RECORD_AUDIO` (for live captions).
* File access depending on your picker flow (SAF / Storage Access Framework).

### iOS (Info.plist)

* `NSMicrophoneUsageDescription` (for live mic).
* If only transcribing local files, mic is not required.

---

## 🚀 Quick Start

### 1) Whisper Models (GGML)

1. Download a model (e.g., `ggml-base.bin`) and place it under `ggml/`.
2. Do not commit models due to size; prefer Git LFS or runtime download.

### 2) whisper.cpp (native)

* Include `whisper.cpp/` as a submodule or folder.
* Build native libs:

    * **Android**: CMake + NDK → produce `.so`/`aar` and link in `composeApp`.
    * **iOS**: build a **static lib** or **XCFramework** (arm64 + simulator) and link in Xcode/Gradle.

> You can start with a backend transcription and migrate to local; this repo is prepared for **local‑first**.

### 3) Audio extraction from video

* **Android**: use **Media3 Transformer** to export audio‑only `.m4a`.
* **iOS**: use `AVAssetExportSession` with the `AppleM4A` preset.

### 4) Build

* **Android Studio**: open the project and run on a device/emulator.
* **CLI** (depending on your template):

    * Single‑module variant: `./gradlew :composeApp:assembleDebug`
    * With a dedicated android module: `./gradlew :androidApp:assembleDebug`
* **iOS**:

    * Generate XCFramework if needed: `./gradlew :iosApp:assembleXCFramework`
    * Or open `iosApp` in Xcode and run on device/simulator.

---

## 🧩 Shared API (KMP)

```kotlin
// commonMain
sealed class TranscriptSource {
    data class Url(val value: String): TranscriptSource()
    data class Path(val value: String): TranscriptSource()
    data class Bytes(val data: ByteArray, val mimeType: String = "audio/wav"): TranscriptSource()
}

data class TranscriptConfig(
    val languageHint: String? = null,
    val enableTimestamps: Boolean = false
)

data class TranscriptChunk(
    val text: String,
    val startSec: Double? = null,
    val endSec: Double? = null
)

data class TranscriptResult(
    val fullText: String,
    val chunks: List<TranscriptChunk> = emptyList(),
    val languageCode: String? = null
)

expect class TranscriptEngine(config: TranscriptConfig = TranscriptConfig()) {
    suspend fun transcribeFile(source: TranscriptSource): TranscriptResult
    fun stream(): kotlinx.coroutines.flow.Flow<TranscriptChunk>
}
```

### Android (audio extractor with Media3)

* Implement `actual` to convert video→`.m4a` and feed local Whisper (native) or your internal pipeline.
* If you already have whisper.cpp bindings, call transcription directly on decoded PCM or the extracted audio.

### iOS (Kotlin + whisper.cpp)

* `actual` that:

    1. If the input is a video, export audio `.m4a` with AVFoundation.
    2. Decode to PCM and call local Whisper.

---

## 🖥️ Usage (example)

```kotlin
val engine = TranscriptEngine(TranscriptConfig(languageHint = "en-US"))

// 1) Local file (video or audio)
val result = engine.transcribeFile(TranscriptSource.Path("/sdcard/Movies/video.mp4"))
println(result.fullText)

// 2) Live captions (mic)
engine.stream() // Flow<TranscriptChunk>
// collect in your VM and render captions in the UI
```

---

## 🧱 DI (Koin)

```kotlin
val transcriptionModule = module {
    factory { (cfg: TranscriptConfig?) -> TranscriptEngine(cfg ?: TranscriptConfig()) }
}
```

Register `transcriptionModule` along with your base modules from the template.

---

## 🧪 Status

* [x] Shared expect/actual API
* [x] Audio extraction (Android/iOS)
* [x] Local Whisper integration (native bindings)
* [ ] Word‑timestamps & diarization
* [ ] Subtitle editing/export (SRT/VTT)

---

## 📄 License

MIT

---

## 🙌 Credits

* whisper.cpp (original license)
* Jetpack Media3, AVFoundation
* Kotlin Multiplatform & Compose
