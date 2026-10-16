---
name: veryfi-lens-android-headless-receipts-integrate
description: "Integrates the Veryfi Lens Headless Receipts SDK (com.veryfi.lens.headless:receipts-sdk:2.7.0.6) with a host-owned CameraX pipeline: configure, processFrame, captureFrame, processGalleryImage, and VeryfiLensHeadlessDelegate. Use when the Android app owns the camera and needs on-device receipt detection and crop. This is not VeryfiLens.uploadImages."
---

# Veryfi Lens Android — Headless Receipts

Walk a host Android app from Gradle to a working first scan. Change the fewest flags. Do not invent SDK APIs. Facts were read from the Android SDK at version `2.7.0.6` (commit `7f33726c`). Anything not confirmed there is marked **Unconfirmed**.

**Coordinate:** `com.veryfi.lens.headless:receipts-sdk:2.7.0.6`  
**Gradle flavor:** `lensHeadlessReceipts`  
**Allowed document types:** Document type is always `RECEIPT`. `documentTypes` is not used to choose a type.

Public entry point: `com.veryfi.lens.headless.receipts.VeryfiLensHeadless` with `com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate`. Settings type: `com.veryfi.lens.helpers.VeryfiLensSettings`. `Frame` is `com.veryfi.lens.helpers.Frame`.

This folder is complete by itself. Duplicate guidance across products is intentional.

## Pick a workflow

- **Quick** — the host already asked for this product. Ask nothing else. Apply the settings in [settings.md](settings.md), wire [implementation.md](implementation.md), and state the assumptions below.
- **Guided** — something material is missing. Ask only the unknown questions (at most four, one message), then continue.

### Quick workflow

```
veryfi-lens-android-headless-receipts-integrate quick:
- [ ] Confirm this artifact (install.md)
- [ ] Add the Maven repository and this one dependency
- [ ] Permissions (install.md)
- [ ] Credentials + settings from settings.md
- [ ] setDelegate, configure { ok -> }, then bind CameraX only when ok is true; unbind in onDestroy
- [ ] Handle the callbacks in callbacks.md
- [ ] Run the conflicts in troubleshooting.md
```

Assumptions when the host was specific about the product and silent on the rest: Host camera, auto-capture on, detection and crop on, NV21 frames from ImageAnalysis, host deletes file_paths.

### Guided workflow

1. **Capture** — auto-capture (`autoCaptureIsOn = true`) or shutter (`captureFrame`)?
2. **Import** — live camera only, or also `processGalleryImage` for a bitmap?
3. **Upload** — host uploads `file_paths` through its backend, or only shows the crop?
4. **Artifact** — `receipts-sdk` (default) or `receipts-sdk-full` (larger native lib; feature trade-off **Unconfirmed**)?

Defaults: `receipts-sdk`, auto-capture, live camera, host owns upload.


## Rules

1. Call `com.veryfi.lens.headless.receipts.VeryfiLensHeadless`, not `com.veryfi.lens.VeryfiLens`.
2. `processFrame` takes NV21 `Frame` bytes from `ImageAnalysis`. `captureFrame` takes JPEG bytes from `ImageCapture`. `processGalleryImage` takes a `Bitmap`.
3. The SDK returns local `file_paths`. It does not upload image bytes and does not call Process a Document. Doc-ready posts the success JSON (paths and flags) under `data` with `sdk` = `lens-headless-receipts`.
4. `VeryfiLens.uploadImages` is a method on the UI SDK. It does not exist here.
5. Request `CAMERA` before `bindToLifecycle`. Unbind when the screen is destroyed. There is no public `close()` or `removeDelegate` on this type.


## Do not

- Copy a repository password, keystore, or credential from the SDK repo or the Demo app. Maven access comes from Veryfi. Keep it in `~/.gradle/gradle.properties` or CI secrets.
- Depend on a second Lens UI flavor, or on `com.veryfi.lens.core:veryfi-lens-core-sdk` (email and obsolete dictation; adds `WRITE_CONTACTS` and `RECORD_AUDIO`).
- Treat Demo types as the client contract: `SettingsHolder`, Demo analyzers, `ImageProxyUtils`, `VeryfiLensHelper`.
- Set `dictateIsOn = true` or call `VeryfiLensCore.showDictation()`.
- Set `anyDocumentType` (deprecated local regex OCR). A new template id is `anyDocumentTemplate`, and only after Veryfi support confirms it.
- Set `useLegacyModels` (default true; no partner contract).
- Quote AAR size as the installed app size. Compare the final app with the SDK linked and removed. [SDK binary sizes](https://docs.veryfi.com/lens/mobile/settings/sdk_binary_sizes/).
- Treat `allowSubmittingScreenshots` (rejects imported screenshots) and `enableScreenshots` (device screenshots of Lens screens, default true) as one switch.
- Call `VeryfiLens.showCamera()`, `VeryfiLens.uploadImages`, or `VeryfiLens.configure` with this dependency.
- Copy the Demo `ImageProxyUtils` or `CameraAnalyzer` as if they were SDK types.
- Promise Cloud extraction from `file_paths`. The host uploads.
- Depend on `receipts-sdk` and `receipts-sdk-full` together.
- Quote AAR size as the installed app size.


## What to hand back

1. `implementation "com.veryfi.lens.headless:receipts-sdk:2.7.0.6"` plus the host's CameraX artifacts.
2. Runtime `CAMERA` permission.
3. `configure { ok -> bindCamera() }`, analyzer calling `processFrame`, optional `captureFrame` and `processGalleryImage`.
4. Delegate for `rectangles` and `file_paths`.
5. Host upload and deletion of `file_paths`.


## Files in this folder

- [install.md](install.md) — Maven coordinate, requirements, permissions, app size
- [implementation.md](implementation.md) — configure, delegate, and launch or camera lifecycle
- [settings.md](settings.md) — `VeryfiLensSettings` for this product, with Android defaults
- [callbacks.md](callbacks.md) — delegate payloads
- [troubleshooting.md](troubleshooting.md) — symptom, cause, fix
- [clarifications.md](clarifications.md) — Unconfirmed items, Any-Documents, fraud, field detection
- [checklist.md](checklist.md) — validation checklist before handing code to the host

