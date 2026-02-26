package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact3WidgetProvider

class ContactSelectionActivity3R : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Contact3WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection3
}