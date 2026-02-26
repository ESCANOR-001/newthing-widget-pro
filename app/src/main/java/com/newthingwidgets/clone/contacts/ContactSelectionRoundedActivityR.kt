package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact5WidgetProvider

class ContactSelectionRoundedActivityR : BaseContactSelectionActivity() {
    override val providerClass: Class<*> = Contact5WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection3
}