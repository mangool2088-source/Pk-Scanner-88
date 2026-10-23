PAK SCANNER UPDATED — ANDROID SETUP

WHAT'S INCLUDED
- Camera capture and gallery multi-image import
- Manual crop rectangle using touch
- Automatic best-effort edge crop for camera captures and gallery imports (can be turned off with a checkbox)
- Manual crop and an Auto-detect edges button remain available
- Clean B&W using adaptive global Otsu threshold
- Rotate, select all, clear selection, delete selected
- Multi-page PDF creation with embedded JPEG pages
- Download selected images, share menu where browser supports file sharing
- PWA manifest and service worker cache for offline use after first successful load

ANDROID PHONE SETUP (GitHub Pages)
1. Download this ZIP to your Android phone.
2. Open Files / My Files > Downloads and tap the ZIP > Extract.
3. In Chrome, open https://github.com/new and sign in.
4. Repository name: pak-scanner-new. Choose Public. Create repository.
5. Tap Add file > Upload files. Upload index.html, manifest.webmanifest, sw.js and the icons folder contents while preserving paths. If mobile UI doesn't allow folder upload, create icons folder and upload both PNGs inside it.
6. Commit changes.
7. Repository Settings > Pages > Build and deployment: Deploy from a branch; branch main, folder /(root); Save.
8. Wait for GitHub Pages to publish, then open:
   https://YOUR-USERNAME.github.io/pak-scanner-new/
9. In Chrome, open site menu (⋮) > Add to Home screen / Install app.
10. Open once while online. Then test airplane mode; offline availability depends on successful first cache and browser storage retention.

LIMITATIONS / HONEST NOTES
- Browser cannot guarantee silent direct saving into Android Gallery. Downloads usually go to Downloads; move/share into Gallery if needed.
- WhatsApp, Bluetooth and email targets are controlled by Android's share sheet and installed apps; the app cannot force a specific target.
- Automatic edge detection is a rough estimate, not professional perspective correction. It may crop incorrectly or fail on busy backgrounds, shadows, colored surfaces, or low contrast. Check every result and use manual crop when needed.
- Offline mode works only after the site and files are cached on the device. Browser may clear cache.
- This is a PWA/web app, not an APK. APK needs a separate Android wrapper/build and testing.
- The included PDF builder embeds JPEG pages and works locally; very large photos may use lots of memory. If PDF fails, use fewer pages or smaller photos.
- For privacy, images are processed locally in the browser; this project does not upload them to a server.
