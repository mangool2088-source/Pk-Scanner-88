# ScanConnect Android project

This project contains a two-option home screen:
- Scan Document
- Online Chat & Calls Setup

The Scan Document screen supports multi-page camera scanning through Google ML Kit Document Scanner, multi-image gallery import, page selection, select/deselect all, black-and-white processing, contrast enhancement, 90-degree rotation, saving selected images to Pictures/ScanConnect, creating one multi-page PDF in Downloads, and sharing the PDF through Android's share sheet.

## Build APK with GitHub Actions
1. Extract this ZIP on your computer/phone.
2. Upload the **contents** of this folder to the root of your GitHub repository (so `app`, `.github`, `build.gradle`, and `settings.gradle` are at repository root).
3. Open GitHub → Actions → **Build Android APK** → Run workflow.
4. When the run succeeds, open it and download the `ScanConnect-debug-apk` artifact.

The online chat/call screen is currently setup information only. Actual messaging and audio/video calls still require backend, authentication, signaling, and call infrastructure.

Note: This source was updated, but an Android build could not be run in this environment. Build and test the APK on GitHub Actions before installing it.


Upgrade v1.1: Main screen contains only Scan Document and Online Chat & Calls Setup. Scan Document keeps camera/gallery, image tools, select/deselect all, save/share, and creates one multi-page PDF from all selected pages. Build with GitHub Actions workflow: Build Android APK.
