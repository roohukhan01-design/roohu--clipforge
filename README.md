# Roohu ClipForge (Android)

Roohu ClipForge turns long videos into vertical 9:16 clips. This project wraps the existing
`Roohu-ClipForge.html` editor in a native Android app (package `com.roohu.clipforge`) with a
native animated launch screen. The HTML is bundled in `app/src/main/assets/` and runs offline.

## Build the APK on GitHub (no Android Studio needed)
1. Create a new empty GitHub repository.
2. Upload **everything** in this folder, including the hidden `.github` folder. Easiest: install
   GitHub Desktop or use `git`: `git init`, `git add .`, `git commit -m "ClipForge"`, `git branch -M main`,
   `git remote add origin <your repo URL>`, `git push -u origin main`.
3. Open the repository, then the **Actions** tab. The build starts by itself on every push to `main`.
   You can also pick **Build Roohu ClipForge APK** and press **Run workflow**.
4. When the run shows a green check, open it and scroll to **Artifacts**.
5. Download **Roohu-ClipForge-debug-apk**, unzip it, and install `app-debug.apk` on your phone
   (allow "Install unknown apps" for your file manager or browser when asked).

## What works in the app
- Import video from the phone, analyse it (audio loudness and motion), pick the best moments.
- Review and trim clips, 9:16 reframing, enhancement, transitions, captions from .srt/.vtt files,
  music you add, RoohuXKhan watermark on every export.
- Export runs in real time inside the app. Finished clips are saved in the in-app Library and to
  **Downloads/RoohuClipForge** on the phone (Android 10+; on Android 8-9 they go to the app's Movies folder).

## Known limits (honest list)
- **Auto-captions are not available in the Android app.** Android WebView has no speech-recognition API.
  Import an .srt/.vtt file instead. Accurate auto-captions need a Whisper-style service (not included).
- **Claude title/rating button is unavailable** outside claude.ai.
- **YouTube links are not downloaded.** That needs a server and permission for each video.
- Caption fonts load from Google Fonts when online; offline the app uses a built-in fallback font.
- Exporting must stay in the foreground: Android pauses web rendering when the app is in the background.
- The first build uses a debug-signed APK. A Play Store release needs your own signing key.
- The Gradle wrapper JAR is not included; the workflow installs Gradle 8.7 itself, so the build does not need it.
