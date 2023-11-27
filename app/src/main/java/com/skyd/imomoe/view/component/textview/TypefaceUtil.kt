package com.skyd.imomoe.view.component.textview

import android.graphics.Typeface
import com.skyd.imomoe.App

object TypefaceUtil {
    const val BPR_TYPEFACE = 1

    private var bPRTypeface: Typeface? = null

    fun getBPRTypeface(): Typeface {
        bPRTypeface?.let {
            return it
        }
        return try {
            Typeface.createFromAsset(App.context.assets, "fonts/BPreplay.otf")
        } catch (e: RuntimeException) {
            Typeface.DEFAULT
        }
    }
}