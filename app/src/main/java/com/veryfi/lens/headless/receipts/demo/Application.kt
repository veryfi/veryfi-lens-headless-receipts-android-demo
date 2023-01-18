package com.veryfi.lens.headless.receipts.demo

import android.app.Application

class Application : Application() {

    companion object {
        //REPLACE YOUR KEYS HERE
        const val CLIENT_ID = BuildConfig.VERYFI_CLIENT_ID
        const val AUTH_USERNAME = BuildConfig.VERYFI_USERNAME
        const val AUTH_API_KEY = BuildConfig.VERYFI_API_KEY
        const val TAG = "ReceiptsDemo"
    }

}