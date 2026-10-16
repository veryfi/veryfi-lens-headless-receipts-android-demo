# Veryfi Lens Android — Headless Receipts — implementation

Source of truth: Veryfi Lens Android SDK **2.7.0.6**, commit `7f33726c`. Names below were read from that tree. Items not fully confirmed are marked **Unconfirmed**. Do not copy repository passwords, keystores, or Demo-only types into the host app.


Façade: `com.veryfi.lens.headless.receipts.VeryfiLensHeadless` (`object`, `@JvmStatic`).  
Delegate: `com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate` (four methods, no `onHelpButtonClicked`).  
`Frame`, `VeryfiLensCredentials`, and `VeryfiLensSettings` live in `com.veryfi.lens.helpers`.

This SDK detects and crops a receipt on a camera the host owns. It returns local file paths. It does not upload image bytes and it does not call Process a Document. The host uploads `file_paths` through its own backend when extraction is required.

`VeryfiLens.uploadImages` belongs to the Lens **UI** object `com.veryfi.lens.VeryfiLens` (for example `com.veryfi.lens:veryfi-lens-receipts-sdk`). It is a different artifact, a different `configure`, and a different delegate. Do not call `VeryfiLens.uploadImages` on this headless object. There is no such method.

## Credentials and configure

```kotlin
import android.app.Application
import com.veryfi.lens.headless.receipts.VeryfiLensHeadless
import com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate
import com.veryfi.lens.helpers.Frame
import com.veryfi.lens.helpers.VeryfiLensCredentials
import com.veryfi.lens.helpers.VeryfiLensSettings
import org.json.JSONObject

val credentials = VeryfiLensCredentials().apply {
    clientId = BuildConfig.VERYFI_CLIENT_ID
    username = BuildConfig.VERYFI_USERNAME
    apiKey = BuildConfig.VERYFI_API_KEY
}

VeryfiLensHeadless.setDelegate(object : VeryfiLensHeadlessDelegate {
    override fun veryfiLensClose(json: JSONObject) {}
    override fun veryfiLensError(json: JSONObject) {}
    override fun veryfiLensUpdate(json: JSONObject) {
        val rectangles = json.optJSONArray("rectangles")
    }
    override fun veryfiLensSuccess(json: JSONObject) {
        val paths = json.optJSONArray("file_paths")
        val sessionId = json.optString("session_id")
        val blurred = json.optBoolean("is_blurred")
        val cropped = json.optBoolean("is_cropped")
        val detected = json.optBoolean("is_document_detected")
    }
})

VeryfiLensHeadless.configure(application, credentials, headlessReceiptSettings()) { ok ->
    if (ok) bindCamera() else showConfigError()
}
```

The only public configure overload takes a non-null `VeryfiLensSettings` and a callback. Bind the camera only after `true`. There is no public `removeDelegate`. Keep the delegate off the Activity instance when you can: `setDelegate` stores the reference. There is no public `close()` on this object. Call `ProcessCameraProvider.unbindAll()` when the screen is destroyed. Whether the SDK deletes `file_paths` is **Unconfirmed** — the host deletes them.

Document type is always `RECEIPT`. `documentTypes` is not read to choose a type. `showCamera` / `uploadImages` are not on this type.

## CameraX lifecycle

Request `CAMERA` first (`ActivityResultContracts.RequestPermission`). Use the CameraX version the host already depends on (`camera-camera2`, `camera-lifecycle`, `camera-view`). The SDK does not choose that version for you.

```kotlin
private var cameraProvider: ProcessCameraProvider? = null
private var imageCapture: ImageCapture? = null
private val analysisExecutor = Executors.newSingleThreadExecutor()

fun bindCamera() {
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        val provider = future.get()
        cameraProvider = provider
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(analysisExecutor) { image ->
            try {
                VeryfiLensHeadless.processFrame(
                    Frame(image.toNv21(), image.width, image.height)
                )
            } finally {
                image.close()
            }
        }
        imageCapture = ImageCapture.Builder().build()
        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis,
            imageCapture,
        )
    }, ContextCompat.getMainExecutor(context))
}

fun onDestroyScreen() {
    cameraProvider?.unbindAll()
    analysisExecutor.shutdown()
}
```

Manual shutter (`ImageCapture` JPEG bytes, plane 0):

```kotlin
imageCapture?.takePicture(
    ContextCompat.getMainExecutor(context),
    object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val jpeg = ByteArray(buffer.remaining())
            buffer.get(jpeg)
            VeryfiLensHeadless.captureFrame(Frame(jpeg, image.width, image.height))
            image.close()
        }
    },
)
```

Imported bitmap: `VeryfiLensHeadless.processGalleryImage(bitmap)`. Crop follows `autoCropGalleryIsOn` (and the image must be portrait-or-square in the current headless code: width greater than height skips the crop path). That condition is source behavior; treat edge cases as **Unconfirmed** until you log one landscape import.

`toNv21()` is host code. The Demo `ImageProxyUtils` / `CameraAnalyzer` types are not SDK API.

```kotlin
import androidx.camera.core.ImageProxy

fun ImageProxy.toNv21(): ByteArray {
    val y = planes[0]
    val u = planes[1]
    val v = planes[2]
    val out = ByteArray(width * height * 3 / 2)
    var pos = 0
    val yBuf = y.buffer.duplicate()
    val uBuf = u.buffer.duplicate()
    val vBuf = v.buffer.duplicate()
    for (row in 0 until height) {
        for (col in 0 until width) {
            out[pos++] = yBuf.get(row * y.rowStride + col * y.pixelStride)
        }
    }
    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            out[pos++] = vBuf.get(row * v.rowStride + col * v.pixelStride)
            out[pos++] = uBuf.get(row * u.rowStride + col * u.pixelStride)
        }
    }
    return out
}
```

`Frame` is `com.veryfi.lens.helpers.Frame(data: ByteArray, width: Int, height: Int, frameRate: Int = 30)`. Both headless SDKs drop a frame when `imageProcessorBusy` is true, so calling on every analyzer frame is safe. Rectangle and card coordinates are in buffer space. Map them onto the preview yourself (rotation and scale).


## What the host draws

`veryfiLensUpdate` carries `rectangles`: a `JSONArray` of objects with `corner0` … `corner3`, each `{x, y}` in frame coordinates (`ImageProcessorHeadless` keys `rectangles`, `x`, `y`). Draw that overlay yourself. `autoCaptureIsOn = true` makes `processFrame` auto-capture. `false` reports rectangles and waits for `captureFrame`.
