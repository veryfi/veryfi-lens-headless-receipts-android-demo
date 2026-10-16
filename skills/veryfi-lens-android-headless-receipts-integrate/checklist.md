# Validation checklist

Run this before delivering host code. Fix conflicts by removing extra flags.

- [ ] Dependency is `com.veryfi.lens.headless:receipts-sdk:2.7.0.6` unless Veryfi asked for `receipts-sdk-full`
- [ ] Host requests CAMERA and binds CameraX only after configure returns true
- [ ] Analyzer calls `processFrame` with a host-built NV21 `Frame` and closes the ImageProxy
- [ ] Delegate is `VeryfiLensHeadlessDelegate` from `headless.receipts`
- [ ] `file_paths` deletion and upload are owned by the host
- [ ] No call to `VeryfiLens.uploadImages` or `showCamera`
- [ ] Demo ImageProxyUtils is not imported
- [ ] Fraud and field detection are not promised
- [ ] Any-Documents escalated away from this SDK
- [ ] Installed app size compared, not AAR size

- [ ] Every relative link in the generated host notes stays inside this skill folder
- [ ] Unconfirmed behavior the host depends on is labeled **Unconfirmed** in the delivery note
- [ ] Host `<application>` sets `android:allowBackup="false"` and `tools:replace="android:allowBackup"` so a new-project `true` does not fail the manifest merger
- [ ] No repository password, Demo `SettingsHolder` preset, or `VeryfiLensHelper` mutation is in the host code
