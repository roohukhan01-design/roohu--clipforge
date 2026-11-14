# Roohu ClipForge (Android)

Android wrapper for the existing `Roohu-ClipForge.html` editor. The original HTML interface is bundled locally in the app assets and displayed using Android WebView. The app includes a native animated startup screen and a native download bridge.

- App name: Roohu ClipForge
- Package: `com.roohu.clipforge`
- Minimum Android version: Android 8.0 (API 26)
- Build: Android Gradle Plugin 8.5.2, Gradle 8.7, Java 17

## Build an APK on GitHub (no Android Studio required)

1. Push or upload the complete repository contents to the `main` branch. Keep the hidden `.github/workflows/` folder.
2. Open the repository's **Actions** tab.
3. Select **Build Roohu ClipForge APK**. A push to `main` also starts the workflow automatically.
4. If starting it manually, press **Run workflow**.
5. Wait for the run to finish with a green check.
6. Open that run and download **Roohu-ClipForge-debug-apk** from the Artifacts section.
7. Extract the downloaded ZIP and install `app-debug.apk` on your Android phone. Allow your file manager or browser to install unknown apps if Android asks.

## Project layout

- `app/src/main/assets/Roohu-ClipForge.html` — original editor interface
- `app/src/main/java/com/roohu/clipforge/MainActivity.java` — Android WebView host, native splash and download bridge
- `app/src/main/res/` — layouts, theme, icons and splash artwork
- `.github/workflows/build-apk.yml` — GitHub Actions build

The workflow installs Gradle 8.7 directly, so a Gradle wrapper is not required for this GitHub build.

## Important limitations

- This packaging step does not add a remote AI model or cloud video-analysis service. Do not treat heuristic/local analysis as a hosted AI feature.
- Android WebView support for video codecs, MediaRecorder, and large-file exports depends on the phone's Android System WebView version, available memory, codecs, and free storage.
- Some fonts are requested from Google Fonts when the phone is online; system fallback fonts are used when unavailable.
- Auto speech-to-text captions require a speech recognition engine/service; importing subtitle files is a separate capability.
- YouTube downloading is not included.
- The generated APK is a debug build for testing, not a Play Store release.

## Troubleshooting

If GitHub Actions fails, open the failed run and inspect the first failed step. The build log will show the missing dependency, compilation error, or SDK issue. A green workflow run confirms the APK was produced and uploaded; it does not by itself prove every editing feature works on every phone.
