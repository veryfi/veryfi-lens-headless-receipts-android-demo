# Veryfi Lens Android — Headless Receipts — troubleshooting

Source of truth: Veryfi Lens Android SDK **2.7.0.6**, commit `7f33726c`. Names below were read from that tree. Items not fully confirmed are marked **Unconfirmed**. Do not copy repository passwords, keystores, or Demo-only types into the host app.


Lens UI logs go through `LogHelper` (logcat). Error callbacks also upload SDK logs.

## Build and launch

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Could not find com.veryfi.lens.headless:receipts-sdk` | Nexus repository missing or no access | Add `https://nexus.veryfi.com/repository/maven-releases/`. Ask Veryfi for access. Credentials from Gradle properties |
| Duplicate class or resource merge | Two Lens UI artifacts, or Core plus a UI artifact | Keep this one dependency |
| Manifest provider authority clash | Host redeclares `${applicationId}.veryfi.lens.fileprovider` | Remove the host duplicate |
| `Manifest merger failed` on `application@allowBackup` | New-app template sets `android:allowBackup="true"`; Lens libraries set `false` | On `<application>`, set `android:allowBackup="false"` and `tools:replace="android:allowBackup"` (`xmlns:tools` on `<manifest>`). Append to an existing `tools:replace` list. Keep `true` only if the host must enable Auto Backup |
| `configure` callback false | No network on first launch with no cached region, or credentials rejected | Retry online. Check `clientId`, `username`, `apiKey` |
| Toast `The URL [..] is not valid.` | Bad `credentials.url` | Remove it or use the URL Veryfi issued |
| Settings edits ignored for returning users | `moreMenuIsOn` true loads stored prefs over the host | Set `moreMenuIsOn = false` |
| Settings differ from code on some accounts | Dashboard `lens_settings` merged | `ignoreRemoteSettings = true` |
| `configure` callback false | No network and no cached region, or credentials rejected | Retry online and check clientId, username, apiKey |
| Analyzer throws | `toNv21` buffer overrun on an unexpected stride | Keep the conversion on the host and close the `ImageProxy` in `finally` |
| Camera stays black | `CAMERA` not granted, or bind before configure returns true | Request permission, then bind inside the configure callback |


## This product

| Symptom | Cause | Fix |
|---------|-------|-----|
| No callbacks | Delegate set on `com.veryfi.lens.VeryfiLens` | `com.veryfi.lens.headless.receipts.VeryfiLensHeadless.setDelegate` |
| Never auto-captures | `autoCaptureIsOn` false | true, or `captureFrame` |
| Cloud fields missing | Headless returns files only | Host uploads `file_paths` |
| Preview box offset | Frame coordinates | Map them |


## Conflicts

Prefer deleting an extra flag over adding another one.

| Combination | What happens | Fix |
|-------------|--------------|-----|
| Calling `VeryfiLens.uploadImages` | Method is not on this object | Host uploads `file_paths`, or use the UI receipts artifact for no-UI upload |
| `autoCaptureIsOn = false` and no shutter | Rectangles only | `captureFrame` or set auto-capture true |
| Rectangles misaligned | Buffer coordinates | Map rotation and scale |
| Landscape `processGalleryImage` not cropped | Source skips crop when width > height | Portrait bitmap, or accept `is_cropped = false` |
| Both `receipts-sdk` and a UI Lens artifact | Duplicate native classes | One product per module |
| Expecting `VeryfiCloudAPI` JSON | Headless does not call Process a Document | Host backend |


| `ignoreRemoteSettings` left false | Dashboard overwrites the object | true |
| `backupDocsToGallery` left true | Scans land in the gallery; storage prompt on API < 33 | false, and More menu off so the user cannot turn it back on |
| `moreMenuIsOn` true on a returning user | Stored prefs replace host capture flags | false |
| `glareDetectionIsOn` true (Android default) and `allowSubmitUndetectedDocsIsOn` false | Glare dialog has no OK path | Glare off, or allow undetected |
| `blurDetectionIsOn` true, undetected disallowed, `allowSubmitBlurredDocsIsOn` false | Blur dialog cannot continue | Torch, `tryAgainLightIsOn`, or allow blurred |
| `allowSubmitUndetectedDocsIsOn` true while blur is meant to block | The undetected flag also lets blur and glare continue | Set both false to block |
| `sameFileUploadPreventionPolicy = Always` without `VeryfiCloudAPI` | Hashes are not persisted | `Session`, or Cloud engine |
| `None` plus `uploadWithoutProcessingIsOn = false` plus auto-delete true | No retained `local_package_path` | `autoDeleteLocalResourcesAfterProcessing = false` |
| `uploadWithoutProcessingIsOn = true` without `None` | Flag ignored | Set `ExtractionEngine.None` |
