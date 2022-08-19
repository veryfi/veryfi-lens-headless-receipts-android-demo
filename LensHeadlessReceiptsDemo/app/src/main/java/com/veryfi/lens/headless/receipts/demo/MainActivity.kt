package com.veryfi.lens.headless.receipts.demo

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityMainBinding

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
            startActivity(intent)
        }
    }
}