package com.newthingwidgets.clone.widgets

import com.newthingwidgets.clone.R

class SocialAppComboWidgetProvider : BaseComboAppWidgetProvider() {
    override val layoutResId: Int = R.layout.social_icons_widget

    override val clickMap: Map<Int, String> = mapOf(
        R.id.instagram to "Instagram",
        R.id.whatsapp to "WhatsApp",
        R.id.telegram to "Telegram",
        R.id.x to "X (Twitter)",
        R.id.facebook to "Facebook",
        R.id.snapchat to "Snapchat"
    )
}
