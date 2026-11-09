# Online features: what is still needed

The offline scanner portion can be built from this repository. The following online features need additional configuration and implementation before they are production-ready:

- Gmail/Google login: Firebase Authentication Google provider + OAuth configuration.
- Phone login: Firebase Phone Authentication, SMS quota and abuse protection.
- Facebook login: Meta developer app, OAuth redirect and Facebook provider configuration.
- Text and image chat: Firestore/Realtime Database, Firebase Storage, user profiles, access rules, blocking/reporting and moderation.
- Audio/video calls: WebRTC peer connection, signaling channel, STUN/TURN service, microphone/camera permissions, call state and push notifications.
- Security: never commit `google-services.json` if your organization treats it as sensitive, never commit private service-account keys, and set restrictive database/storage rules.

A public repository cannot magically provide real accounts or reliable calls without service setup. Do not publish an app with open database rules.
