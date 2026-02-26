package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact6WidgetProvider

class ContactSelectionActivityM : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Contact6WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection
    override val includePhotoAndId: Boolean = false
}