package com.veryfi.lens.headless.receipts.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityCaptureBinding
import com.veryfi.lens.headless.receipts.demo.helpers.CameraAnalyzer
import com.veryfi.lens.headless.receipts.demo.helpers.ScreenHelper
import com.veryfi.lens.headless.receipts.demo.helpers.ThemeHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CaptureActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCaptureBinding
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var bytesImageCapture: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()
        ThemeHelper.setSecondaryColorToStatusBar(this, application)
        setUpToolBar()
        setUpClickEvents()
        checkPermissions()
    }

    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        viewBinding.toolbar.setNavigationIcon(R.drawable.ic_vector_close_shape)
        viewBinding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setUpClickEvents() {
        viewBinding.btnCapture.setOnClickListener {
            Snackbar.make(it, bytesImageCapture.toString(), Snackbar.LENGTH_LONG)
                .setAnchorView(viewBinding.btnCapture.id)
                .setAction("Action", null).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.cameraPreview.surfaceProvider)
                }
            val screenAspectRatio = aspectRatio()
            val imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0).build()
            imageAnalyzer.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                CameraAnalyzer { byteArray, width, height ->
                    //Integration with Lens SDK
                    if (viewBinding.cameraPreview.visibility != View.GONE) {
                        bytesImageCapture = byteArray
                    }
                })
            try {
                cameraProvider?.unbindAll()
                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageAnalyzer)
                    .build()
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
            Log.e(TAG, "Can't do auto focus", e)
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
                    Log.e(TAG, "Can't do manual focus", e)
                }
            }
            viewBinding.cameraPreview.performClick()
        }
    }

    private fun aspectRatio(): Int {
        val width = ScreenHelper.getScreenWidth(this)
        val height = ScreenHelper.getScreenHeight(this)
        val previewRatio = kotlin.math.max(width, height).toDouble() / kotlin.math.min(
            width,
            height
        )
        if (kotlin.math.abs(previewRatio - RATIO_4_3_VALUE) <= kotlin.math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val AUTO_FOCUS_TIME = 3L
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }
}