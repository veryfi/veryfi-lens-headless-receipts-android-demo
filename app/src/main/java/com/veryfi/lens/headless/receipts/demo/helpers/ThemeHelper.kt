package com.veryfi.lens.headless.receipts.demo.helpers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.veryfi.lens.headless.receipts.demo.R


object ThemeHelper {

    fun setSecondaryColorToStatusBar(activity: Activity, context: Context) {
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
        if (window != null) {
            context.let {
                window.statusBarColor = ContextCompat.getColor(it, color)
            }
        }
    }

    fun getPrimaryColor(activity: Activity): Int {
        var color = R.color.md_theme_light_background
        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                color = R.color.md_theme_dark_background
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                color = R.color.md_theme_light_background
            }
        }
        return color
    }
}