package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.WhatsappChatWidgetProvider

class WhatsappChatOvalActivityR : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = WhatsappChatWidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection3
}