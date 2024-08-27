package com.veryfi.lens.headless.receipts.demo.receipts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.veryfi.lens.headless.receipts.VeryfiLensHeadless
import com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate
import com.veryfi.lens.headless.receipts.demo.BuildConfig
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityCaptureBinding
import com.veryfi.lens.headless.receipts.demo.helpers.ThemeHelper
import com.veryfi.lens.headless.receipts.demo.logs.LogsActivity
import com.veryfi.lens.helpers.Frame
import com.veryfi.lens.helpers.ScreenHelper
import com.veryfi.lens.helpers.VeryfiLensCredentials
import com.veryfi.lens.helpers.VeryfiLensSettings
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CaptureActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private lateinit var viewBinding: ActivityCaptureBinding
    private lateinit var imageCapture: ImageCapture
    private var rectViewPort: Rect? = null
    private var isTakingPhoto = false
    private var isVeryfiReady = false
    private lateinit var cameraExecutor: ExecutorService

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        viewBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ThemeHelper.setSecondaryColorToStatusBar(this, application)
        checkPermissions()
        setUpClickEvents()
        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupWindow() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
    }

    private fun setupHeadless() {
        val autoRotateIsOn = intent.extras?.getSerializable(AUTO_ROTATE) as Boolean
        val autoDocDetectionAndCropIsOn = intent.extras?.getSerializable(AUTO_DOC_DETECTION_CROP) as Boolean
        val blurDetectionIsOn = intent.extras?.getSerializable(BLUR_DETECTION) as Boolean
        val autoSkewCorrectionIsOn = intent.extras?.getSerializable(AUTO_SKEW_CORRECTION) as Boolean
        val autoCropGalleryIsOn = intent.extras?.getSerializable(AUTO_CROP_GALLERY) as Boolean

        val veryfiLensHeadlessCredentials = VeryfiLensCredentials()
        val veryfiLensSettings = VeryfiLensSettings()

        veryfiLensSettings.autoRotateIsOn = autoRotateIsOn
        veryfiLensSettings.autoDocDetectionAndCropIsOn = autoDocDetectionAndCropIsOn
        veryfiLensSettings.blurDetectionIsOn = blurDetectionIsOn
        veryfiLensSettings.autoSkewCorrectionIsOn = autoSkewCorrectionIsOn
        veryfiLensSettings.autoCropGalleryIsOn = autoCropGalleryIsOn
        veryfiLensSettings.gpuIsOn = true

        veryfiLensHeadlessCredentials.apiKey = AUTH_API_KEY
        veryfiLensHeadlessCredentials.username = AUTH_USERNAME
        veryfiLensHeadlessCredentials.clientId = CLIENT_ID

        VeryfiLensHeadless.configure(
            application, veryfiLensHeadlessCredentials, veryfiLensSettings
        ) { success ->
            if (success) {
                setUpVeryfiLensDelegate()
                isVeryfiReady = true
            } else {
                Toast.makeText(
                    this, "Credentials invalid.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            bindCameraUseCases(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = buildPreview()
        imageCapture = ImageCapture.Builder().setTargetRotation(Surface.ROTATION_90).build()
        val imageAnalyzer = buildImageAnalyzer()

        val useCaseGroup = viewBinding.cameraPreview.viewPort?.let {
            UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalyzer)
                .addUseCase(imageCapture)
                .setViewPort(it)
                .build()
        } ?: run {
            UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalyzer)
                .addUseCase(imageCapture)
                .build()
        }
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, useCaseGroup
            )
            startAutoFocusing()
            startFocusOnClick()
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun buildPreview(): Preview {
        return Preview.Builder()
            .setTargetAspectRatio(aspectRatio())
            .setTargetRotation(Surface.ROTATION_0)
            .build().also {
                it.setSurfaceProvider(viewBinding.cameraPreview.surfaceProvider)
            }
    }

    private fun buildImageAnalyzer(): ImageAnalysis {
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(aspectRatio())
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetRotation(Surface.ROTATION_0).build()
        imageAnalyzer.setAnalyzer(
            cameraExecutor,
            CameraAnalyzer { byteArray, width, height, cropRect ->
                rectViewPort = cropRect
                if (viewBinding.cameraPreview.visibility != View.GONE && !isTakingPhoto &&isVeryfiReady) {
                    VeryfiLensHeadless.processFrame(Frame(byteArray, width, height))
                }
            })
        return imageAnalyzer
    }

    private fun aspectRatio(): Int {
        val width = ScreenHelper.getScreenWidth(this)
        val height = ScreenHelper.getScreenHeight(this)
        val previewRatio = max(width, height).toDouble() / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }

    private fun captureImage() {
        isTakingPhoto = true
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val frame = Frame(image.toByteArray(), image.width, image.height)
                    VeryfiLensHeadless.captureFrame(frame)
                    image.close()
                    isTakingPhoto = false
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Error capturing image: ${exception.message}", exception)
                    isTakingPhoto = false
                }
            }
        )
    }

    private fun startAutoFocusing() {
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
            .createPoint(.2f, .5f)
        val autoFocusAction =
            FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(AUTO_FOCUS_TIME, TimeUnit.SECONDS)
                .build()
        camera?.cameraControl?.startFocusAndMetering(autoFocusAction)
    }

    private fun startFocusOnClick() {
        viewBinding.cameraPreview.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                performManualFocus(event.x, event.y)
            }
            viewBinding.cameraPreview.performClick()
        }
    }

    private fun performManualFocus(x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(
            viewBinding.cameraPreview.width.toFloat(),
            viewBinding.cameraPreview.height.toFloat()
        )
        val autoFocusPoint = factory.createPoint(x, y)
        val focusAction = FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
            .disableAutoCancel()
            .build()
        camera?.cameraControl?.startFocusAndMetering(focusAction)
    }

    private fun setUpVeryfiLensDelegate() {
        VeryfiLensHeadless.setDelegate(object : VeryfiLensHeadlessDelegate {
            override fun veryfiLensClose(json: JSONObject) {
                Toast.makeText(
                    this@CaptureActivity,
                    "Veryfi Lens Headless close",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun veryfiLensError(json: JSONObject) {
                Log.e(TAG, "Veryfi Lens Error: ${json.toString(2)}")
            }

            override fun veryfiLensSuccess(json: JSONObject) {
                showExtractedData(json)
            }

            override fun veryfiLensUpdate(json: JSONObject) {
                drawCorners(json)
            }
        })
    }

    private fun showExtractedData(json: JSONObject) {
        Intent(this, LogsActivity::class.java).apply {
            putExtra(DATA, json.toString())
            startActivity(this)
        }
        finish()
    }

    private fun drawCorners(json: JSONObject) {
        viewBinding.relativeLayout.removeAllViews()
        val rectangles = json.getJSONArray(RECTANGLES)
        if (rectangles.length() == 0) return

        for (i in 0 until rectangles.length()) {
            val rectangle = rectangles.getJSONObject(i)
            val path = Path()
            for (j in 0..3) {
                val corner = rectangle.getJSONObject("$CORNER$j")
                drawCorner(corner, j, path)
            }
        }
    }

    private fun drawCorner(corner: JSONObject, index: Int, path: Path) {
        var x = corner.getDouble(X_VALUE).toFloat()
        var y = corner.getDouble(Y_VALUE).toFloat()

        rectViewPort?.let {
            x -= it.top
            y -= it.left
            x *= (viewBinding.cameraPreview.width.toFloat() / it.height())
            y *= (viewBinding.cameraPreview.height.toFloat() / it.width())
        }

        if (index == 0) path.moveTo(x, y)
        if (index != 0) path.lineTo(x, y)
        if (index == 3) {
            path.close()
            val rectCanvasView = RectangleCanvasView(applicationContext)
            rectCanvasView.path = path
            rectCanvasView.invalidate()
            viewBinding.relativeLayout.addView(rectCanvasView)
        }
    }

    private fun ImageProxy.toByteArray(): ByteArray {
        val planeProxy = planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return bytes
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            setupHeadless()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setupHeadless()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setUpClickEvents() {
        viewBinding.cancel.setOnClickListener { onBackPressed() }
        viewBinding.btnCapture.setOnClickListener { captureImage() }
    }

    companion object {
        const val CLIENT_ID = BuildConfig.VERYFI_CLIENT_ID
        const val AUTH_USERNAME = BuildConfig.VERYFI_USERNAME
        const val AUTH_API_KEY = BuildConfig.VERYFI_API_KEY

        private const val DATA = "data"
        private const val TAG = "HeadlessReceiptsActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val AUTO_FOCUS_TIME = 3L
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val RECTANGLES = "rectangles"
        private const val CORNER = "corner"
        private const val X_VALUE = "x"
        private const val Y_VALUE = "y"
        private const val AUTO_ROTATE = "auto_rotate"
        private const val AUTO_DOC_DETECTION_CROP = "auto_doc_detection_crop"
        private const val BLUR_DETECTION = "blur_detection"
        private const val AUTO_SKEW_CORRECTION = "auto_skew_correction"
        private const val AUTO_CROP_GALLERY = "auto_crop_gallery"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }
}
