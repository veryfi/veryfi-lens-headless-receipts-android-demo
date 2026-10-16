# Veryfi Lens Android — Headless Receipts — install

Source of truth: Veryfi Lens Android SDK **2.7.0.6**, commit `7f33726c`. Names below were read from that tree. Items not fully confirmed are marked **Unconfirmed**. Do not copy repository passwords, keystores, or Demo-only types into the host app.


## Requirements

| Item | Value |
|------|--------|
| minSdk | 23 |
| compileSdk / targetSdk used by the SDK | 36 |
| Language | Kotlin façade, callable from Java via `@JvmStatic` |
| SDK version | `2.7.0.6` (commit `7f33726c`) |

Camera stack: Host-owned CameraX (`Preview`, `ImageAnalysis`, and `ImageCapture` for the manual shutter). The SDK does not start `CaptureActivity`.

## Maven

Repository: `https://nexus.veryfi.com/repository/maven-releases/`

```groovy
// settings.gradle — dependencyResolutionManagement.repositories
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url "https://nexus.veryfi.com/repository/maven-releases/"
            // Only when Veryfi issued repository credentials. Read them from Gradle properties.
            // Never commit them, and never copy a password out of the SDK repository.
            credentials {
                username = providers.gradleProperty("veryfiMavenUser").getOrNull()
                password = providers.gradleProperty("veryfiMavenPassword").getOrNull()
            }
        }
    }
}
```

```groovy
// app/build.gradle
dependencies {
    implementation "com.veryfi.lens.headless:receipts-sdk:2.7.0.6"
    // Host-owned CameraX. Use one version for all three artifacts.
    implementation "androidx.camera:camera-camera2:<host CameraX version>"
    implementation "androidx.camera:camera-lifecycle:<host CameraX version>"
    implementation "androidx.camera:camera-view:<host CameraX version>"
}
```

Transitive `com.veryfi.lens.cpp` and `com.veryfi.lens.helpers` artifacts resolve with the Lens dependency. Do not add them by hand.


## Flavor check

`FlavorCheckerHelper` reads `BuildConfig.FLAVOR` (`lensHeadlessReceipts`). Allowed types: Document type is always `RECEIPT`. `documentTypes` is not used to choose a type.

There is no `FlavorCheckerHelper` on this façade. Do not add a Lens UI artifact in the same module. `receipts-sdk-full` is published from the full native component and is larger. Prefer `receipts-sdk` unless Veryfi instructs otherwise (**Unconfirmed** feature difference).

## Permissions

The host owns the camera. Declare `android.permission.CAMERA` and request it at runtime before `ProcessCameraProvider.bindToLifecycle`. These headless artifacts do not open `CaptureActivity`, so they do not request `CAMERA` for you.

The headless receipts module manifest does not itself declare `CAMERA` or `INTERNET`. `ACCESS_COARSE_LOCATION` and `ACCESS_NETWORK_STATE` come from the helpers manifest. Volley (a headless dependency) normally merges `INTERNET`. Treat host `INTERNET` as required for `validateClientId`. Whether every install path merges it is **Unconfirmed** — declare `INTERNET` on the host.

`veryfihelpers` merges `ACCESS_COARSE_LOCATION` and `ACCESS_NETWORK_STATE`. This product does not turn on `locationServicesIsOn`. If the Play listing must not mention location, remove `ACCESS_COARSE_LOCATION` with `tools:node="remove"`. Whether every code path tolerates that removal is **Unconfirmed**.

New projects set `android:allowBackup="true"`. This module and `veryfihelpers` set `false`, so the merger fails on `application@allowBackup`. On the host `<application>`, set `android:allowBackup="false"` and `tools:replace="android:allowBackup"`, with `xmlns:tools` on `<manifest>`. Append `android:allowBackup` when `tools:replace` already lists other attributes. Leave `dataExtractionRules` and `fullBackupContent` in place. Keep `allowBackup="true"` only when the host must enable Auto Backup.

New projects set `android:allowBackup="true"`. This module and `veryfihelpers` set `false`, so the merger fails on `application@allowBackup`. On the host `<application>`, set `android:allowBackup="false"` and `tools:replace="android:allowBackup"`, with `xmlns:tools` on `<manifest>`. Append `android:allowBackup` when `tools:replace` already lists other attributes. Leave `dataExtractionRules` and `fullBackupContent` in place. Keep `allowBackup="true"` only when the host must enable Auto Backup.


## App size

Compare the **installed app**, with this dependency linked and with it removed (release APK/AAB, Android Studio APK Analyzer, or the Play Console size report). The published AAR is a fat artifact. Do not quote AAR size as the user's install delta. Background: [SDK binary sizes](https://docs.veryfi.com/lens/mobile/settings/sdk_binary_sizes/).

Publish two coordinates: `com.veryfi.lens.headless:receipts-sdk:2.7.0.6` and `com.veryfi.lens.headless:receipts-sdk-full:2.7.0.6`. Default to `receipts-sdk`. `-full` is built from the full native component and is larger. What it adds is **Unconfirmed**. Compare the installed app with the dependency removed. Do not quote AAR size. Remote model download follows `RemoteModelConfig` for the native flavor inside the AAR; do not assume the UI Lite/Remote Models switch applies until Veryfi confirms (**Unconfirmed**).



