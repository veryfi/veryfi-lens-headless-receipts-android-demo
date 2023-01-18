package com.veryfi.lens.headless.receipts.demo.receipts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.veryfi.lens.headless.receipts.VeryfiLensHeadless
import com.veryfi.lens.headless.receipts.VeryfiLensHeadlessDelegate
import com.veryfi.lens.headless.receipts.demo.Application
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityCaptureBinding
import com.veryfi.lens.headless.receipts.demo.helpers.ThemeHelper
import com.veryfi.lens.headless.receipts.demo.logs.LogsActivity
import com.veryfi.lens.helpers.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CaptureActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCaptureBinding
    private lateinit var imageCapture: ImageCapture
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var rectViewPort: Rect? = null
    private var autoRotateIsOn = true
    private var autoDocDetectionAndCropIsOn = true
    private var blurDetectionIsOn = true
    private var autoSkewCorrectionIsOn = true
    private var autoCropGalleryIsOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ThemeHelper.setSecondaryColorToStatusBar(this, application)
        setupHeadless()
        setUpVeryfiLensDelegate()
        checkPermissions()
        setUpClickEvents()
    }

    private fun setupHeadless() {
        autoRotateIsOn = intent.extras?.getSerializable(AUTO_ROTATE) as Boolean
        autoDocDetectionAndCropIsOn =
            intent.extras?.getSerializable(AUTO_DOC_DETECTION_CROP) as Boolean
        blurDetectionIsOn = intent.extras?.getSerializable(BLUR_DETECTION) as Boolean
        autoSkewCorrectionIsOn = intent.extras?.getSerializable(AUTO_SKEW_CORRECTION) as Boolean
        autoCropGalleryIsOn = intent.extras?.getSerializable(AUTO_CROP_GALLERY) as Boolean

        val veryfiLensHeadlessCredentials = VeryfiLensCredentials()
        val veryfiLensSettings = VeryfiLensSettings()

        veryfiLensSettings.autoRotateIsOn = autoRotateIsOn
        veryfiLensSettings.autoDocDetectionAndCropIsOn = autoDocDetectionAndCropIsOn
        veryfiLensSettings.blurDetectionIsOn = blurDetectionIsOn
        veryfiLensSettings.autoSkewCorrectionIsOn = autoSkewCorrectionIsOn
        veryfiLensSettings.autoCropGalleryIsOn = autoCropGalleryIsOn

        veryfiLensHeadlessCredentials.apiKey = Application.AUTH_API_KEY
        veryfiLensHeadlessCredentials.username = Application.AUTH_USERNAME
        veryfiLensHeadlessCredentials.clientId = Application.CLIENT_ID

        VeryfiLensHeadless.configure(
            this.application,
            veryfiLensHeadlessCredentials,
            veryfiLensSettings
        ) {}
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val screenAspectRatio = aspectRatio()
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0)
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.cameraPreview.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().setTargetRotation(Surface.ROTATION_90).build()
            val imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0).build()
            imageAnalyzer.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                CameraAnalyzer { byteArray, width, height, cropRect ->
                    //ViewPort distortion
                    rectViewPort = cropRect
                    //Integration with Lens SDK
                    if (viewBinding.cameraPreview.visibility != View.GONE) {
                        val frame = Frame(byteArray, width, height)
                        VeryfiLensHeadless.processFrame(frame)
                    }
                })
            try {
                cameraProvider?.unbindAll()
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
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    useCaseGroup
                )
                startAutoFocusing()
                startFocusOnClick()
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startAutoFocusing() {
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
            .createPoint(.2f, .5f)
        try {
            val autoFocusAction = FocusMeteringAction.Builder(
                autoFocusPoint,
                FocusMeteringAction.FLAG_AF
            ).apply {
                setAutoCancelDuration(AUTO_FOCUS_TIME, TimeUnit.SECONDS)
            }.build()
            camera?.cameraControl?.startFocusAndMetering(autoFocusAction)
        } catch (e: CameraInfoUnavailableException) {
            LogHelper.e(
                TAG,
                "Can't do auto focus",
                e
            )
        }
    }

    private fun captureImage() {
        if (EmulatorHelper.isProbablyRunningOnEmulator()) {
            val stream = ByteArrayOutputStream()
            viewBinding.cameraPreview.bitmap?.let {
                it.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                VeryfiLensHeadless.captureFrame(Frame(stream.toByteArray(), it.width, it.height))
            }
        } else {
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), object :
                ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    LogHelper.d(TAG, "onCaptureSuccess")
                    val byteArray = ImageProxyUtils.imageProxyToByteArray(image)
                    val frame = Frame(byteArray, image.width, image.height)
                    VeryfiLensHeadless.captureFrame(frame)
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    LogHelper.e(TAG, "Error: ${exception.message}")
                    super.onError(exception)
                }
            }
            )
        }
    }

    private fun startFocusOnClick() {
        viewBinding.cameraPreview.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    viewBinding.cameraPreview.width.toFloat(),
                    viewBinding.cameraPreview.height.toFloat()
                )
                val autoFocusPoint = factory.createPoint(event.x, event.y)
                try {
                    camera?.cameraControl?.startFocusAndMetering(
                        FocusMeteringAction.Builder(
                            autoFocusPoint,
                            FocusMeteringAction.FLAG_AF
                        ).apply {
                            //focus only when the user tap the preview
                            disableAutoCancel()
                        }.build()
                    )
                } catch (e: CameraInfoUnavailableException) {
                    LogHelper.e(TAG, "Can't do manual focus", e)
                }
            }
            viewBinding.cameraPreview.performClick()
        }
    }

    private fun setUpVeryfiLensDelegate() {
        VeryfiLensHeadless.setDelegate(object : VeryfiLensHeadlessDelegate {
            override fun veryfiLensClose(json: JSONObject) {

            }

            override fun veryfiLensError(json: JSONObject) {
                showExtractedData(json)
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

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpClickEvents() {
        viewBinding.cancel.setOnClickListener {
            LogHelper.d(TAG, "btnCancel.setOnClickListener")
            onBackPressed()
        }

        viewBinding.btnCapture.setOnClickListener {
            LogHelper.d(TAG, "btnCapture.setOnClickListener")
            captureImage()
        }
    }

    private fun aspectRatio(): Int {
        this.let {
            val width = ScreenHelper.getScreenWidth(it)
            val height = ScreenHelper.getScreenHeight(it)
            val previewRatio = max(width, height).toDouble() / min(width, height)
            if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
                return AspectRatio.RATIO_4_3
            }
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private const val DATA = "data"
        private const val MIN_LUMEN_FLASH_TRIGGER = 105
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