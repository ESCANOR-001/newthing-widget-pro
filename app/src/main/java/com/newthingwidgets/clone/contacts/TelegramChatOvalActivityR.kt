package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.TelegramChatWidgetProvider

class TelegramChatOvalActivityR : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = TelegramChatWidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection3
}