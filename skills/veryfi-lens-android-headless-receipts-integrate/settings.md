# Veryfi Lens Android — Headless Receipts — settings

Source of truth: Veryfi Lens Android SDK **2.7.0.6**, commit `7f33726c`. Names below were read from that tree. Items not fully confirmed are marked **Unconfirmed**. Do not copy repository passwords, keystores, or Demo-only types into the host app.


Always construct a fresh `VeryfiLensSettings()`. Android has no `VeryfiLens*Settings` subclasses. Pass that instance to `configure`. The SDK mutates it.

```kotlin
import com.veryfi.lens.helpers.DocumentType
import com.veryfi.lens.helpers.VeryfiLensSettings

fun headlessReceiptSettings() = VeryfiLensSettings().apply {
    ignoreRemoteSettings = true          // default false
    autoCaptureIsOn = true               // default false; processFrame auto-captures
    autoDocDetectionAndCropIsOn = true   // default true
    autoCropGalleryIsOn = true           // default false; processGalleryImage crop path
}

```

## Defaults versus this recipe

Android initializers below are the property defaults in `VeryfiLensSettings`.

| Property | Android default | This recipe | Why |
|----------|-----------------|-------------|-----|
| `ignoreRemoteSettings` | false | true | Avoid dashboard `lens_settings` replacing this object |
| `autoCaptureIsOn` | false | true | `processFrame` auto-captures. False reports rectangles until `captureFrame` |
| `autoDocDetectionAndCropIsOn` | true | true | Crop on auto-capture |
| `autoCropGalleryIsOn` | false | true | `processGalleryImage` crops when the bitmap is not wider than it is tall |
| `originalImageMaxSizeInMB` | 2.5 | 2.5 | Headless code can read this cap. Change only if the host asks |


The headless receipts module reads `autoCaptureIsOn`, `autoDocDetectionAndCropIsOn`, `autoCropGalleryIsOn`, and `originalImageMaxSizeInMB`. `ignoreRemoteSettings` matters during configure validation. Other UI flags (stitch, gallery button, More menu, document type list) are not the headless contract. Shared native code may still read blur settings. That is **Unconfirmed**.

`processGalleryImage` crops only when `autoCropGalleryIsOn` is true and the bitmap is not wider than it is tall (`ImageProcessorHeadless`). A landscape import skips crop and sets `is_cropped` false. Confirm that on a device (**Unconfirmed** as a product rule beyond that condition).

Fraud is passed as false in the headless receipts detector settings (`ImageProcessorHeadless` sets `isFraudDetectionOn = false`).


## Suggestions

- Map `rectangles` from buffer space onto the preview with the image's rotation.
- Delete `file_paths` when the host is finished. SDK cleanup is **Unconfirmed**.
- If the host wants Lens UI instead, the UI coordinate is `com.veryfi.lens:veryfi-lens-receipts-sdk:2.7.0.6` (`VeryfiLens.uploadImages` and `showCamera`). Do not mix the two objects.
- Compare installed app size for `receipts-sdk` versus `receipts-sdk-full`. The `-full` trade-off is **Unconfirmed**. Do not quote AAR size.

