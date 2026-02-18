package com.newthingwidgets.clone.widgets

import com.newthingwidgets.clone.R

class GoogleAppComboGlassWidgetProvider : BaseComboAppWidgetProvider() {
    override val layoutResId: Int = R.layout.search_bar7_r

    override val clickMap: Map<Int, String> = mapOf(
        R.id.crome3 to "Chrome",
        R.id.gamini3 to "Gemini Assistant",
        R.id.youtube4 to "YouTube",
        R.id.photo4 to "Photos",
        R.id.gmail2 to "Email",
        R.id.playstore4 to "Play Store"
    )
}
