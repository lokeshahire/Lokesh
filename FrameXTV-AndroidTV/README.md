# FrameXTV Android TV starter

A Kotlin Android TV shell for Sony Google TV / Android TV using the FrameXTV pages and embed URL formats supplied in its documentation.

## Build without installing Android Studio

This project includes `.github/workflows/build-apk.yml` to build the debug APK in GitHub Actions.

1. Sign in to GitHub and create a **new private repository**.
2. Upload the contents of this folder to the repository (including the hidden `.github` folder), or upload the ZIP and extract it in the repository using GitHub's web editor where possible.
3. Open the repository's **Actions** tab.
4. Select **Build FrameXTV Android TV APK** and click **Run workflow**. A push to `main` or `master` also triggers the build.
5. When the workflow completes successfully, open the run and download the `FrameXTV-AndroidTV-debug-apk` artifact.
6. Extract the artifact ZIP; it contains `app-debug.apk`.

## Install on Sony TV

Copy `app-debug.apk` to a USB drive or use a trusted Android TV sideloading method. On the TV, allow installation from the file manager used to open the APK, then install it. Menu wording varies by Sony TV / Android version.

## Current feature scope and limitations

- Home, Movies, TV, Anime, My List and History navigation uses documented website paths.
- Playback uses documented embed formats: `/embed/{tmdbId}` and `/embed/{tmdbId}/{season}/{episode}`.
- Local history stores IDs launched using **Play by ID**; it is not synchronized with FrameXTV's website.
- The supplied documentation does not specify catalogue/search JSON endpoints. Search and browse currently rely on FrameXTV's own website UI; no undocumented API endpoints are invented.
- Playback depends on FrameXTV's embed and the Sony TV's WebView/browser compatibility. Test on the target TV.

## Requirements for local build

JDK 17, Gradle 8.9, Android SDK Platform 35 and Build Tools 35.0.0.
