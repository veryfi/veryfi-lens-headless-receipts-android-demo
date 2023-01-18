package com.veryfi.lens.headless.receipts.demo.receipts

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class CameraAnalyzer(private val listener: AnalyzerListener) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        listener(ImageProxyUtils.toByteArray(image), image.width, image.height, image.cropRect)
        image.close()
    }

}
