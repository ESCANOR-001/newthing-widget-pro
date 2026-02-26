package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

data class SelectedContact(
    val name: String,
    val phone: String?,
    val photoUri: String?,
    val contactId: Long?
)

data class ThreeSlotContactData(
    val slot1: SelectedContact,
    val slot2: SelectedContact,
    val slot3: SelectedContact
)

object ContactWidgetPrefs {
    const val PREFS_NAME = "widget_prefs"
    const val EXTRA_SLOT = "slot"

    private const val NAME_PREFIX = "contact_name_"
    private const val PHONE_PREFIX = "contact_phone_"
    private const val PHOTO_PREFIX = "contact_photo_"
    private const val CONTACT_ID_PREFIX = "contact_id_"

    private const val DEFAULT_SLOT_TEXT = "Tap to select"
    private const val DEFAULT_CONTACT_TEXT = "Tap to select a contact"
    private const val DEFAULT_CONTACT_TEXT_CAP_C = "Tap to select a Contact"

    fun sharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getContact(
        context: Context,
        appWidgetId: Int,
        defaultName: String = DEFAULT_CONTACT_TEXT
    ): SelectedContact {
        val prefs = sharedPrefs(context)
        return SelectedContact(
            name = prefs.getString("${NAME_PREFIX}$appWidgetId", defaultName) ?: defaultName,
            phone = prefs.getString("${PHONE_PREFIX}$appWidgetId", null),
            photoUri = prefs.getString("${PHOTO_PREFIX}$appWidgetId", null),
            contactId = if (prefs.contains("${CONTACT_ID_PREFIX}$appWidgetId")) {
                prefs.getLong("${CONTACT_ID_PREFIX}$appWidgetId", -1L).takeIf { it >= 0L }
            } else {
                null
            }
        )
    }

    fun getContactForContact4(context: Context, appWidgetId: Int): SelectedContact {
        return getContact(context, appWidgetId, DEFAULT_CONTACT_TEXT_CAP_C)
    }

    fun putContact(
        context: Context,
        appWidgetId: Int,
        contact: SelectedContact,
        includePhotoAndId: Boolean = true
    ) {
        sharedPrefs(context).edit().apply {
            putString("${NAME_PREFIX}$appWidgetId", contact.name)
            putString("${PHONE_PREFIX}$appWidgetId", contact.phone)
            if (includePhotoAndId) {
                putString("${PHOTO_PREFIX}$appWidgetId", contact.photoUri)
                if (contact.contactId != null) {
                    putLong("${CONTACT_ID_PREFIX}$appWidgetId", contact.contactId)
                } else {
                    remove("${CONTACT_ID_PREFIX}$appWidgetId")
                }
            } else {
                remove("${PHOTO_PREFIX}$appWidgetId")
                remove("${CONTACT_ID_PREFIX}$appWidgetId")
            }
        }.apply()
    }

    fun getSlotContact(
        context: Context,
        appWidgetId: Int,
        slot: Int
    ): SelectedContact {
        val safeSlot = slot.coerceIn(1, 3)
        val prefs = sharedPrefs(context)
        val suffix = "${appWidgetId}_$safeSlot"
        return SelectedContact(
            name = prefs.getString("$NAME_PREFIX$suffix", DEFAULT_SLOT_TEXT) ?: DEFAULT_SLOT_TEXT,
            phone = prefs.getString("$PHONE_PREFIX$suffix", null),
            photoUri = prefs.getString("$PHOTO_PREFIX$suffix", null),
            contactId = if (prefs.contains("$CONTACT_ID_PREFIX$suffix")) {
                prefs.getLong("$CONTACT_ID_PREFIX$suffix", -1L).takeIf { it >= 0L }
            } else {
                null
            }
        )
    }

    fun getThreeSlotData(context: Context, appWidgetId: Int): ThreeSlotContactData {
        return ThreeSlotContactData(
            slot1 = getSlotContact(context, appWidgetId, 1),
            slot2 = getSlotContact(context, appWidgetId, 2),
            slot3 = getSlotContact(context, appWidgetId, 3)
        )
    }

    fun putSlotContact(
        context: Context,
        appWidgetId: Int,
        slot: Int,
        contact: SelectedContact
    ) {
        val safeSlot = slot.coerceIn(1, 3)
        val suffix = "${appWidgetId}_$safeSlot"
        sharedPrefs(context).edit().apply {
            putString("$NAME_PREFIX$suffix", contact.name)
            putString("$PHONE_PREFIX$suffix", contact.phone)
            putString("$PHOTO_PREFIX$suffix", contact.photoUri)
            if (contact.contactId != null) {
                putLong("$CONTACT_ID_PREFIX$suffix", contact.contactId)
            } else {
                remove("$CONTACT_ID_PREFIX$suffix")
            }
        }.apply()
    }

    fun clearSingleContactWidget(context: Context, appWidgetId: Int) {
        sharedPrefs(context).edit().apply {
            remove("${NAME_PREFIX}$appWidgetId")
            remove("${PHONE_PREFIX}$appWidgetId")
            remove("${PHOTO_PREFIX}$appWidgetId")
            remove("${CONTACT_ID_PREFIX}$appWidgetId")
        }.apply()
    }

    fun clearThreeContactWidget(context: Context, appWidgetId: Int) {
        sharedPrefs(context).edit().apply {
            for (slot in 1..3) {
                val suffix = "${appWidgetId}_$slot"
                remove("$NAME_PREFIX$suffix")
                remove("$PHONE_PREFIX$suffix")
                remove("$PHOTO_PREFIX$suffix")
                remove("$CONTACT_ID_PREFIX$suffix")
            }
        }.apply()
    }

    fun requestWidgetRefresh(
        context: Context,
        providerClass: Class<*>,
        appWidgetId: Int
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass))
        if (ids.isEmpty()) return

        val targetIds = if (ids.contains(appWidgetId)) intArrayOf(appWidgetId) else ids
        val intent = Intent(context, providerClass).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, targetIds)
        }
        context.sendBroadcast(intent)
    }
}
