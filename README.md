# PAK Scanner Android project

This is a complete starter Android Studio/Gradle project. It builds a debug APK using GitHub Actions.

## Features included
- Home screen with exactly two choices: Scan Document and Online Chat & Calls Setup
- ML Kit document scanner (camera scanning, automatic document edge detection/cropping, multi-page scan)
- Import multiple images from gallery
- Select/deselect pages, rotate, delete
- Manual crop: drag the four corner handles with your finger for each selected page
- Black & white conversion and contrast enhancement
- Save selected pages to Pictures/PAK Scanner
- Create one multi-page PDF for all selected pages, saved to Download/PAK Scanner
- Share the generated PDF

## Build from a phone using GitHub
1. Create a new GitHub account and verify your email.
2. Create a new repository named `PAK-Scanner`, choose Public or Private, and create it empty (do not add README).
3. Extract this ZIP on your Android phone.
4. Upload the *contents* of this extracted project to the repository root. Keep the folder structure. In particular, `.github/workflows/build-apk.yml` must be present.
5. Open the repository's Actions tab. If GitHub asks to enable workflows, enable them.
6. Select `Build PAK Scanner APK` and press `Run workflow`.
7. When the run completes successfully, open it and download the `PAK-Scanner-debug-apk` artifact.
8. Extract the downloaded artifact ZIP and install `app-debug.apk`.

## Important
- This is a debug APK for testing, not a Play Store release.
- The document scanner uses Google ML Kit / Google Play services; the first launch may need internet to download its scanner module. The scan processing itself is on-device once available.
- Online Chat & Calls Setup is currently a placeholder screen because no chat/calling service provider or server has been specified.
- Automatic crop is provided by ML Kit; manual crop is also available using the four draggable corner handles.
- Test on the target Android phone. Android may ask you to allow installation from the browser or file manager.
