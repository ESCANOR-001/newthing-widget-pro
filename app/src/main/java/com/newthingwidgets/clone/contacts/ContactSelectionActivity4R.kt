package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact2WidgetProvider

class ContactSelectionActivity4R : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Contact2WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection
}