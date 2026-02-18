package com.newthingwidgets.clone.widgets

import com.newthingwidgets.clone.R

class GoogleAppComboWidgetProvider : BaseComboAppWidgetProvider() {
    override val layoutResId: Int = R.layout.search_bar6_r

    override val clickMap: Map<Int, String> = mapOf(
        R.id.crome2 to "Chrome",
        R.id.gamini2 to "Gemini Assistant",
        R.id.youtube3 to "YouTube",
        R.id.photo3 to "Photos",
        R.id.gmail to "Email",
        R.id.playstore3 to "Play Store"
    )
}
