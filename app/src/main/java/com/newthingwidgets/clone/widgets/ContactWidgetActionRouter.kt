package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object ContactWidgetActionRouter {

    private const val TELEGRAM_PACKAGE = "org.telegram.messenger"
    private const val TELEGRAM_ALT_PACKAGE = "org.thunderdog.challegram"
    private const val WHATSAPP_PACKAGE = "com.whatsapp"

    private const val PENDING_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun configureIntent(
        context: Context,
        appWidgetId: Int,
        activityClass: Class<*>,
        slot: Int? = null
    ): Intent {
        return Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("appWidgetId", appWidgetId)
            if (slot != null) {
                putExtra(ContactWidgetPrefs.EXTRA_SLOT, slot)
            }
        }
    }

    fun dialIntent(phone: String): Intent {
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun smsSendToIntent(phone: String): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun smsViewIntent(phone: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun whatsappChatIntent(context: Context, rawPhone: String): Intent {
        val sanitizedPhone = sanitizePhone(rawPhone)
        return if (isPackageInstalled(context, WHATSAPP_PACKAGE)) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$sanitizedPhone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            playStoreIntent(WHATSAPP_PACKAGE)
        }
    }

    fun telegramChatIntent(context: Context, rawPhoneOrUsername: String): Intent {
        val hasTelegram = isPackageInstalled(context, TELEGRAM_PACKAGE) ||
            isPackageInstalled(context, TELEGRAM_ALT_PACKAGE)
        if (!hasTelegram) {
            return playStoreIntent(TELEGRAM_PACKAGE)
        }

        val uri = if (rawPhoneOrUsername.startsWith("@")) {
            Uri.parse("tg://resolve?domain=${rawPhoneOrUsername.removePrefix("@")}")
        } else {
            Uri.parse("tg://resolve?phone=${sanitizePhone(rawPhoneOrUsername)}")
        }
        return Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun pendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getActivity(context, requestCode, intent, PENDING_FLAGS)
    }

    private fun sanitizePhone(rawPhone: String): String {
        return rawPhone.replace(Regex("[^0-9+]"), "")
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun playStoreIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}