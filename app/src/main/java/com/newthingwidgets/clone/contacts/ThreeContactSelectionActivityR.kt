package com.newthingwidgets.clone.contacts

import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.Contact1WidgetProvider
import com.newthingwidgets.clone.widgets.ContactWidgetPrefs
import com.newthingwidgets.clone.widgets.SelectedContact

class ThreeContactSelectionActivityR : BaseContactSelectionActivity() {

    override val providerClass: Class<*> = Contact1WidgetProvider::class.java
    override val selectionLayoutResId: Int = R.layout.activity_contact_selection3

    private val slot: Int
        get() = intent.getIntExtra(ContactWidgetPrefs.EXTRA_SLOT, 1).coerceIn(1, 3)

    override fun persistContactSelection(contact: SelectedContact) {
        ContactWidgetPrefs.putSlotContact(this, appWidgetId, slot, contact)
    }
}