package com.veryfi.lens.headless.receipts.demo

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityMainBinding
import com.veryfi.lens.headless.receipts.demo.receipts.CaptureActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private var autoRotateIsOn = true
    private var autoDocDetectionAndCropIsOn = true
    private var blurDetectionIsOn = true
    private var autoSkewCorrectionIsOn = true
    private var autoCropGalleryIsOn = true

    override fun onStart() {
        super.onStart()
        initVeryfiLogo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        initVeryfiSettings()
        setUpClickEvents()
    }

    private fun initVeryfiLogo() {
        when (applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                viewBinding.veryfiLogo.setImageResource(R.drawable.ic_vector_veryfi_white)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                viewBinding.veryfiLogo.setImageResource(R.drawable.ic_vector_veryfi_black)
            }
        }
    }

    private fun initVeryfiSettings() {
        viewBinding.switchAutoRotate.isChecked = autoRotateIsOn
        viewBinding.switchAutoDocDetection.isChecked = autoDocDetectionAndCropIsOn
        viewBinding.switchBlur.isChecked = blurDetectionIsOn
        viewBinding.switchSkew.isChecked = autoSkewCorrectionIsOn
        viewBinding.switchAutoCropGallery.isChecked = autoCropGalleryIsOn
    }

    private fun setUpClickEvents() {
        viewBinding.switchAutoRotate.setOnCheckedChangeListener { _, isChecked ->
            autoRotateIsOn = isChecked
        }

        viewBinding.switchAutoDocDetection.setOnCheckedChangeListener { _, isChecked ->
            autoDocDetectionAndCropIsOn = isChecked
        }

        viewBinding.switchBlur.setOnCheckedChangeListener { _, isChecked ->
            blurDetectionIsOn = isChecked
        }

        viewBinding.switchSkew.setOnCheckedChangeListener { _, isChecked ->
            autoSkewCorrectionIsOn = isChecked
        }

        viewBinding.switchAutoCropGallery.setOnCheckedChangeListener { _, isChecked ->
            autoCropGalleryIsOn = isChecked
        }

        viewBinding.btnScan.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            intent.putExtra(AUTO_ROTATE, autoRotateIsOn)
            intent.putExtra(AUTO_DOC_DETECTION_CROP, autoDocDetectionAndCropIsOn)
            intent.putExtra(BLUR_DETECTION, blurDetectionIsOn)
            intent.putExtra(AUTO_SKEW_CORRECTION, autoSkewCorrectionIsOn)
            intent.putExtra(AUTO_CROP_GALLERY, autoCropGalleryIsOn)
            startActivity(intent)
        }

        viewBinding.txtPrivacyPolice.setOnClickListener {
            val uris = Uri.parse(resources.getString(R.string.privacy_police_link))
            val intents = Intent(Intent.ACTION_VIEW, uris)
            val b = Bundle()
            b.putBoolean(NEW_WINDOW, true)
            intents.putExtras(b)
            this@MainActivity.startActivity(intents)
        }
    }

    companion object {
        const val NEW_WINDOW = "new_window"
        const val AUTO_ROTATE = "auto_rotate"
        const val AUTO_DOC_DETECTION_CROP = "auto_doc_detection_crop"
        const val BLUR_DETECTION = "blur_detection"
        const val AUTO_SKEW_CORRECTION = "auto_skew_correction"
        const val AUTO_CROP_GALLERY = "auto_crop_gallery"
    }
}