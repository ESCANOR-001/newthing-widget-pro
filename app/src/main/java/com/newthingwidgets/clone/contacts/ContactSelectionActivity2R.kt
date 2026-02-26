package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact4WidgetProvider

class ContactSelectionActivity2R : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Contact4WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection2
}