package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Message6WidgetProvider

class MassageSelectActivityR : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Message6WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection
    override val includePhotoAndId: Boolean = false
}