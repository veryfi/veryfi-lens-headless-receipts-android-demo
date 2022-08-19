package com.veryfi.lens.headless.receipts.demo.helpers

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.veryfi.lens.headless.receipts.demo.R

object ThemeHelper {
    fun setSecondaryColorToStatusBar(activity: Activity, application: Application) {
        var color = R.color.md_theme_light_secondaryContainer
        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                color = R.color.md_theme_dark_secondaryContainer
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                color = R.color.md_theme_light_secondaryContainer
            }
        }
        val window = activity.window
        window.statusBarColor =
            ContextCompat.getColor(application, color)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(activity, color))
        )
    }
}