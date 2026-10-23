# ScanConnect Android starter

An Android starter project with:
- Camera document scanner with automatic edge detection/cropping (ML Kit Document Scanner).
- Import an image from Gallery.
- Offline black-and-white processing and a basic contrast/brightness enhancement.
- Save processed images to Gallery.
- Export the current image as a one-page PDF.
- Share the image through Android's share sheet (WhatsApp, Bluetooth and other installed apps when supported).
- GitHub Actions workflow to build a debug APK for free.

## Build APK on GitHub (free)
1. Create a new GitHub repository.
2. Upload/extract all files in this ZIP into the repository root.
3. Commit to `main` (or `master`).
4. Open **Actions** and select **Build Android APK**. You can also press **Run workflow**.
5. When it finishes, open the workflow run and download the **ScanConnect-debug-apk** artifact.
6. Extract the artifact ZIP to get `app-debug.apk`. Send that APK to friends. Android may warn that it is from outside Play Store.

## Requirements and limitations
- Android Studio is optional if using GitHub Actions.
- First scanner launch may require Google Play services and an internet connection to download scanner components. Image edits, saving and PDF export run on-device.
- Enhancement is a simple offline contrast/brightness filter, not AI restoration.
- This ZIP does **not** contain a completed online chat/calling service. Real text/image/audio messaging, audio/video calls and Gmail/phone/Facebook login need a configured backend, authentication provider credentials, WebRTC signaling and TURN servers. See `SETUP.md`.
- This starter creates a debug APK. Publishing a signed release APK needs a signing key and secure GitHub Secrets.
