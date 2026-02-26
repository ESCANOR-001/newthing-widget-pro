package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.RemoteViews
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.contacts.ContactSelectionActivity2R
import com.newthingwidgets.clone.contacts.ContactSelectionActivity3R
import com.newthingwidgets.clone.contacts.ContactSelectionActivity4R
import com.newthingwidgets.clone.contacts.ContactSelectionActivityM
import com.newthingwidgets.clone.contacts.ContactSelectionRoundedActivityR
import com.newthingwidgets.clone.contacts.MassageSelectActivityR
import com.newthingwidgets.clone.contacts.TelegramChatOvalActivityR
import com.newthingwidgets.clone.contacts.ThreeContactSelectionActivityR
import com.newthingwidgets.clone.contacts.WhatsappChatOvalActivityR

abstract class BaseContactWidgetProvider : AppWidgetProvider() {

    protected abstract val widgetDisplayName: String

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            if (widgetDisplayName == CONTACT_1) {
                ContactWidgetPrefs.clearThreeContactWidget(context, appWidgetId)
            } else {
                ContactWidgetPrefs.clearSingleContactWidget(context, appWidgetId)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val spec = ContactWidgetSpecRegistry.getSpec(widgetDisplayName) ?: return
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val views = RemoteViews(context.packageName, spec.layoutResId)

        when (widgetDisplayName) {
            CONTACT_1 -> renderContact1(context, appWidgetManager, appWidgetId, spec.layoutResId, views, options)
            CONTACT_2 -> renderContact2(context, appWidgetManager, appWidgetId, spec.layoutResId, views, options)
            CONTACT_3 -> renderContact3(context, appWidgetManager, appWidgetId, spec.layoutResId, views, options)
            CONTACT_4 -> renderContact4(context, appWidgetId, views, options)
            CONTACT_5 -> renderContact5(context, appWidgetManager, appWidgetId, spec.layoutResId, views, options)
            CONTACT_6 -> renderContact6(context, appWidgetId, views, options)
            MESSAGE_6 -> renderMessage6(context, appWidgetId, views, options)
            WHATSAPP_CHAT -> renderWhatsappChat(context, appWidgetId, views, options)
            TELEGRAM_CHAT -> renderTelegramChat(context, appWidgetId, views, options)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun renderContact1(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutResId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val scale = ContactWidgetSizing.squareScaleDp(context, options)
        val avatarSize = 0.241f * scale
        val textSize = 0.085f * scale
        val avatarMargin = 0.1f * scale
        val textTop = 0.36f * scale
        val textEdge = 0.12f * scale

        val slots = ContactWidgetPrefs.getThreeSlotData(context, appWidgetId)
        val contacts = listOf(slots.slot1, slots.slot2, slots.slot3)

        views.setImageViewResource(R.id.avatar_image1, R.drawable.ct_profile4)
        views.setImageViewResource(R.id.avatar_image2, R.drawable.ct_profile4)
        views.setImageViewResource(R.id.avatar_image3, R.drawable.ct_profile4)

        val avatarViews = intArrayOf(
            R.id.avatar_image1,
            R.id.avatar_image2,
            R.id.avatar_image3,
            R.id.avatar_image1bg,
            R.id.avatar_image2bg,
            R.id.avatar_image3bg
        )
        avatarViews.forEach { viewId ->
            ContactWidgetSizing.setViewHeightDp(views, viewId, avatarSize)
            ContactWidgetSizing.setViewWidthDp(views, viewId, avatarSize)
        }

        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image1bg, SIDE_TOP, avatarMargin)
        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image1bg, SIDE_START, avatarMargin)
        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image2bg, SIDE_TOP, avatarMargin)
        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image3bg, SIDE_TOP, avatarMargin)
        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image3bg, SIDE_BOTTOM, avatarMargin)

        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview1, SIDE_TOP, textTop)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview2, SIDE_TOP, textTop)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview3, SIDE_TOP, textTop)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview1, SIDE_START, textEdge)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview3, SIDE_BOTTOM, textEdge)

        views.setImageViewBitmap(
            R.id.name_textview1,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contacts[0].name, 13),
                baseTextSize = textSize,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
        views.setImageViewBitmap(
            R.id.name_textview2,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contacts[1].name, 13),
                baseTextSize = textSize,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
        views.setImageViewBitmap(
            R.id.name_textview3,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contacts[2].name, 13),
                baseTextSize = textSize,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )

        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image1,
            photoUri = contacts[0].photoUri,
            targetSizeDp = avatarSize,
            fallbackDrawableRes = R.drawable.ct_profile4
        )
        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image2,
            photoUri = contacts[1].photoUri,
            targetSizeDp = avatarSize,
            fallbackDrawableRes = R.drawable.ct_profile4
        )
        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image3,
            photoUri = contacts[2].photoUri,
            targetSizeDp = avatarSize,
            fallbackDrawableRes = R.drawable.ct_profile4
        )

        bindClick(
            context = context,
            views = views,
            appWidgetId = appWidgetId,
            requestCode = 11,
            viewIds = intArrayOf(R.id.avatar_image1, R.id.avatar_image1bg, R.id.top_contact1, R.id.name_textview1),
            intent = contacts[0].phone?.let { ContactWidgetActionRouter.dialIntent(it) }
                ?: ContactWidgetActionRouter.configureIntent(
                    context,
                    appWidgetId,
                    ThreeContactSelectionActivityR::class.java,
                    slot = 1
                )
        )

        bindClick(
            context = context,
            views = views,
            appWidgetId = appWidgetId,
            requestCode = 12,
            viewIds = intArrayOf(R.id.avatar_image2, R.id.avatar_image2bg, R.id.top_contact2, R.id.name_textview2),
            intent = contacts[1].phone?.let { ContactWidgetActionRouter.dialIntent(it) }
                ?: ContactWidgetActionRouter.configureIntent(
                    context,
                    appWidgetId,
                    ThreeContactSelectionActivityR::class.java,
                    slot = 2
                )
        )

        bindClick(
            context = context,
            views = views,
            appWidgetId = appWidgetId,
            requestCode = 13,
            viewIds = intArrayOf(R.id.avatar_image3, R.id.avatar_image3bg, R.id.top_contact3, R.id.name_textview3),
            intent = contacts[2].phone?.let { ContactWidgetActionRouter.dialIntent(it) }
                ?: ContactWidgetActionRouter.configureIntent(
                    context,
                    appWidgetId,
                    ThreeContactSelectionActivityR::class.java,
                    slot = 3
                )
        )
    }

    private fun renderContact2(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutResId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val scale = ContactWidgetSizing.squareScaleDp(context, options)
        val icon = 0.25f * scale
        val avatar = 0.3f * scale
        val padSmall = 0.1f * scale
        val padLarge = 0.2f * scale

        views.setImageViewResource(R.id.avatar_image, R.drawable.ct_profile4)

        ContactWidgetSizing.setViewHeightDp(views, R.id.avatar_image, avatar)
        ContactWidgetSizing.setViewWidthDp(views, R.id.avatar_image, avatar)
        ContactWidgetSizing.setViewHeightDp(views, R.id.avatar_image_bg, avatar)
        ContactWidgetSizing.setViewWidthDp(views, R.id.avatar_image_bg, avatar)
        ContactWidgetSizing.setViewMarginDp(views, R.id.avatar_image_bg, SIDE_START, padSmall)

        val iconViews = intArrayOf(
            R.id.call_icon,
            R.id.message_icon,
            R.id.call_icon2,
            R.id.message_icon2,
            R.id.call_icon_bg,
            R.id.message_icon_bg
        )
        iconViews.forEach { viewId ->
            ContactWidgetSizing.setViewHeightDp(views, viewId, icon)
            ContactWidgetSizing.setViewWidthDp(views, viewId, icon)
        }

        ContactWidgetSizing.setViewMarginDp(views, R.id.message_icon_bg, SIDE_END, padLarge)
        ContactWidgetSizing.setViewMarginDp(views, R.id.message_icon_bg, SIDE_BOTTOM, padSmall)
        ContactWidgetSizing.setViewMarginDp(views, R.id.call_icon_bg, SIDE_TOP, padLarge)
        ContactWidgetSizing.setViewMarginDp(views, R.id.call_icon_bg, SIDE_BOTTOM, padSmall)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 13),
                baseTextSize = 0.085f * scale,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        )

        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image,
            photoUri = contact.photoUri,
            targetSizeDp = avatar,
            fallbackDrawableRes = R.drawable.ct_profile4
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            ContactSelectionActivity4R::class.java
        )

        bindClick(
            context,
            views,
            appWidgetId,
            21,
            intArrayOf(R.id.root_view, R.id.top_contact, R.id.avatar_image_bg, R.id.avatar_image, R.id.name_textview),
            configureIntent
        )

        val callIntent = contact.phone?.let { ContactWidgetActionRouter.dialIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            22,
            intArrayOf(R.id.call_icon_bg, R.id.call_icon2, R.id.call_icon),
            callIntent
        )

        val messageIntent = contact.phone?.let { ContactWidgetActionRouter.smsSendToIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            23,
            intArrayOf(R.id.message_icon_bg, R.id.message_icon2, R.id.message_icon),
            messageIntent
        )
    }

    private fun renderContact3(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutResId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val iconColor = opaqueColor(prefs.getInt("widget_icon_color", DEFAULT_ICON_COLOR))
        val bgColor = opaqueColor(prefs.getInt("widget_bg_color", DEFAULT_BG_COLOR))
        views.setInt(R.id.call_icon, "setColorFilter", iconColor)
        views.setInt(R.id.call_icon2, "setColorFilter", bgColor)

        views.setImageViewResource(R.id.mavatar_imagessss, R.drawable.ct_profile4)
        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image,
            photoUri = contact.photoUri,
            targetSizeDp = 0.28f * ContactWidgetSizing.squareScaleDp(context, options),
            fallbackDrawableRes = R.drawable.ct_profile4
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            ContactSelectionActivity3R::class.java
        )

        bindClick(
            context,
            views,
            appWidgetId,
            31,
            intArrayOf(R.id.root_view, R.id.avatar_image_bg, R.id.avatar_image, R.id.mavatar_imagessss),
            configureIntent
        )

        val callIntent = contact.phone?.let { ContactWidgetActionRouter.dialIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            32,
            intArrayOf(R.id.call_icon_bg, R.id.call_icon2, R.id.call_icon),
            callIntent
        )

        val messageIntent = contact.phone?.let { ContactWidgetActionRouter.smsSendToIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            33,
            intArrayOf(R.id.message_icon_bg, R.id.message_icon2, R.id.message_icon),
            messageIntent
        )

        val whatsappIntent = contact.phone?.let { ContactWidgetActionRouter.whatsappChatIntent(context, it) }
            ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            34,
            intArrayOf(R.id.whatsapp_icon, R.id.whatsapp),
            whatsappIntent
        )
    }

    private fun renderContact4(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContactForContact4(context, appWidgetId)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val iconColor = opaqueColor(prefs.getInt("widget_icon_color", DEFAULT_ICON_COLOR))
        val bgColor = opaqueColor(prefs.getInt("widget_bg_color", DEFAULT_BG_COLOR))
        views.setInt(R.id.call_icon_button, "setColorFilter", iconColor)
        views.setInt(R.id.message_icon, "setColorFilter", iconColor)
        views.setInt(R.id.message, "setColorFilter", bgColor)
        views.setInt(R.id.call_icon, "setColorFilter", bgColor)

        val scale = ContactWidgetSizing.squareScaleDp(context, options)
        ContactWidgetSizing.setViewHeightDp(views, R.id.name_textview, 0.35f * scale)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 13),
                baseTextSize = 0.085f * scale,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            ContactSelectionActivity2R::class.java
        )

        bindClick(
            context,
            views,
            appWidgetId,
            41,
            intArrayOf(R.id.root_view, R.id.top_contact, R.id.name_textview),
            configureIntent
        )

        val callIntent = contact.phone?.let { ContactWidgetActionRouter.dialIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            42,
            intArrayOf(R.id.main2, R.id.call_icon, R.id.call_icon_button),
            callIntent
        )

        val messageIntent = contact.phone?.let { ContactWidgetActionRouter.smsViewIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            43,
            intArrayOf(R.id.main, R.id.message, R.id.message_icon),
            messageIntent
        )
    }

    private fun renderContact5(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutResId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val scale = ContactWidgetSizing.squareScaleDp(context, options)

        views.setImageViewResource(R.id.avatar_image, R.drawable.ct_profile5)
        ContactWidgetSizing.setViewHeightDp(views, R.id.call_icon, scale)
        ContactWidgetSizing.setViewWidthDp(views, R.id.call_icon, scale)

        ContactPhotoLoader.loadIntoWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            layoutResId = layoutResId,
            viewId = R.id.avatar_image,
            photoUri = contact.photoUri,
            targetSizeDp = scale,
            applyBottomFade = true,
            fallbackDrawableRes = R.drawable.ct_profile5
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            ContactSelectionRoundedActivityR::class.java
        )
        bindClick(
            context,
            views,
            appWidgetId,
            51,
            intArrayOf(R.id.root_view, R.id.avatar_image_bg, R.id.avatar_image),
            configureIntent
        )

        val callIntent = contact.phone?.let { ContactWidgetActionRouter.dialIntent(it) } ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            52,
            intArrayOf(R.id.call_icon),
            callIntent
        )
    }

    private fun renderContact6(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val height = ContactWidgetSizing.ovalHeightDp(context, options)

        ContactWidgetSizing.setViewHeightDp(views, R.id.root_view, height)
        ContactWidgetSizing.setViewHeightDp(views, R.id.contect, height)
        ContactWidgetSizing.setViewWidthDp(views, R.id.contect, height)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview, SIDE_TOP, 0.95f * height)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 11),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )

        val intent = contact.phone?.let { ContactWidgetActionRouter.dialIntent(it) }
            ?: ContactWidgetActionRouter.configureIntent(context, appWidgetId, ContactSelectionActivityM::class.java)
        bindClick(
            context,
            views,
            appWidgetId,
            61,
            intArrayOf(R.id.root_view, R.id.contect, R.id.top_contact, R.id.name_textview),
            intent
        )
    }

    private fun renderMessage6(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val height = ContactWidgetSizing.ovalHeightDp(context, options)

        ContactWidgetSizing.setViewHeightDp(views, R.id.root_view, height)
        ContactWidgetSizing.setViewHeightDp(views, R.id.contect, height)
        ContactWidgetSizing.setViewWidthDp(views, R.id.contect, height)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview, SIDE_TOP, 0.95f * height)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 6),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )

        val intent = contact.phone?.let { ContactWidgetActionRouter.smsViewIntent(it) }
            ?: ContactWidgetActionRouter.configureIntent(context, appWidgetId, MassageSelectActivityR::class.java)
        bindClick(
            context,
            views,
            appWidgetId,
            62,
            intArrayOf(R.id.root_view, R.id.contect, R.id.top_contact, R.id.name_textview),
            intent
        )
    }

    private fun renderWhatsappChat(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val height = ContactWidgetSizing.ovalHeightDp(context, options)

        ContactWidgetSizing.setViewHeightDp(views, R.id.root_view, height)
        ContactWidgetSizing.setViewHeightDp(views, R.id.whatsapp_icon, height)
        ContactWidgetSizing.setViewWidthDp(views, R.id.whatsapp_icon, 2.2f * height)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview, SIDE_TOP, 0.95f * height)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 6),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            WhatsappChatOvalActivityR::class.java
        )

        bindClick(
            context,
            views,
            appWidgetId,
            71,
            intArrayOf(R.id.root_view, R.id.top_contact, R.id.name_textview),
            configureIntent
        )

        val whatsappIntent = contact.phone?.let { ContactWidgetActionRouter.whatsappChatIntent(context, it) }
            ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            72,
            intArrayOf(R.id.whatsapp_icon),
            whatsappIntent
        )
    }

    private fun renderTelegramChat(
        context: Context,
        appWidgetId: Int,
        views: RemoteViews,
        options: Bundle?
    ) {
        val contact = ContactWidgetPrefs.getContact(context, appWidgetId)
        val height = ContactWidgetSizing.ovalHeightDp(context, options)

        ContactWidgetSizing.setViewHeightDp(views, R.id.root_view, height)
        ContactWidgetSizing.setViewHeightDp(views, R.id.telegram_icon, height)
        ContactWidgetSizing.setViewWidthDp(views, R.id.telegram_icon, 2.2f * height)
        ContactWidgetSizing.setViewMarginDp(views, R.id.name_textview, SIDE_TOP, 0.95f * height)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(contact.name, 6),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )

        val configureIntent = ContactWidgetActionRouter.configureIntent(
            context,
            appWidgetId,
            TelegramChatOvalActivityR::class.java
        )

        bindClick(
            context,
            views,
            appWidgetId,
            81,
            intArrayOf(R.id.root_view, R.id.top_contact, R.id.name_textview),
            configureIntent
        )

        val telegramIntent = contact.phone?.let { ContactWidgetActionRouter.telegramChatIntent(context, it) }
            ?: configureIntent
        bindClick(
            context,
            views,
            appWidgetId,
            82,
            intArrayOf(R.id.telegram_icon),
            telegramIntent
        )
    }

    private fun bindClick(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        requestCode: Int,
        viewIds: IntArray,
        intent: android.content.Intent
    ) {
        val pendingIntent = pendingIntent(context, appWidgetId, requestCode, intent)
        viewIds.forEach { viewId ->
            views.setOnClickPendingIntent(viewId, pendingIntent)
        }
    }

    private fun pendingIntent(
        context: Context,
        appWidgetId: Int,
        requestCode: Int,
        intent: android.content.Intent
    ): PendingIntent {
        val stableRequestCode = (appWidgetId * 100) + requestCode
        return ContactWidgetActionRouter.pendingIntent(context, stableRequestCode, intent)
    }

    private fun truncate(text: String, maxChars: Int): String {
        return if (text.length > maxChars) {
            "${text.take(maxChars)}.."
        } else {
            text
        }
    }

    private fun opaqueColor(color: Int): Int {
        return android.graphics.Color.argb(
            255,
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    private companion object {
        private const val CONTACT_1 = "Contact 1"
        private const val CONTACT_2 = "Contact 2"
        private const val CONTACT_3 = "Contact 3"
        private const val CONTACT_4 = "Contact 4"
        private const val CONTACT_5 = "Contact 5"
        private const val CONTACT_6 = "Contact 6"
        private const val MESSAGE_6 = "Message 6"
        private const val WHATSAPP_CHAT = "WhatsApp Chat"
        private const val TELEGRAM_CHAT = "Telegram Chat"

        private const val SIDE_START = 1
        private const val SIDE_BOTTOM = 3
        private const val SIDE_TOP = 4
        private const val SIDE_END = 5

        private val DEFAULT_ICON_COLOR = android.graphics.Color.parseColor("#000000")
        private val DEFAULT_BG_COLOR = android.graphics.Color.parseColor("#1283ff")
    }
}
