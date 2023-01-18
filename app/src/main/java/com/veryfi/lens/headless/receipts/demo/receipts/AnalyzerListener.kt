package com.veryfi.lens.headless.receipts.demo.receipts

import android.graphics.Rect

typealias AnalyzerListener = (byteArray: ByteArray, width: Int, height: Int, cropRect: Rect) -> Unit