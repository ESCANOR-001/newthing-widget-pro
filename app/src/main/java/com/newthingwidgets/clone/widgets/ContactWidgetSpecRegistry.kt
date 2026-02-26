package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import com.newthingwidgets.clone.R
import kotlin.math.min

data class ContactTextBinding(
    val viewId: Int,
    val text: String,
    val defaultTextSize: Float,
    val textColor: Int = Color.WHITE,
    val font: WidgetTextBitmapFactory.WidgetFont = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
)

data class ContactImageBinding(
    val viewId: Int,
    val drawableRes: Int
)

data class ContactWidgetSpec(
    val displayName: String,
    val layoutResId: Int,
    val widthCells: Int,
    val heightCells: Int,
    val clickMap: Map<Int, String>,
    val textBindings: List<ContactTextBinding> = emptyList(),
    val imageBindings: List<ContactImageBinding> = emptyList()
)

object ContactWidgetSpecRegistry {

    private const val SIDE_START = 1
    private const val SIDE_BOTTOM = 3
    private const val SIDE_TOP = 4
    private const val SIDE_END = 5

    private val defaultIconColor = Color.parseColor("#000000")
    private val defaultBgColor = Color.parseColor("#1283ff")

    private val contact1Spec = ContactWidgetSpec(
        displayName = "Contact 1",
        layoutResId = R.layout.three_contact_widget_r,
        widthCells = 2,
        heightCells = 2,
        clickMap = mapOf(
            R.id.root_view to "Contacts",
            R.id.top_contact1 to "Contacts",
            R.id.top_contact2 to "Contacts",
            R.id.top_contact3 to "Contacts",
            R.id.avatar_image1bg to "Contacts",
            R.id.avatar_image2bg to "Contacts",
            R.id.avatar_image3bg to "Contacts",
            R.id.avatar_image1 to "Contacts",
            R.id.avatar_image2 to "Contacts",
            R.id.avatar_image3 to "Contacts"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview1,
                text = "Tap to select",
                defaultTextSize = 14f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            ),
            ContactTextBinding(
                viewId = R.id.name_textview2,
                text = "Tap to select",
                defaultTextSize = 14f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            ),
            ContactTextBinding(
                viewId = R.id.name_textview3,
                text = "Tap to select",
                defaultTextSize = 14f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        ),
        imageBindings = listOf(
            ContactImageBinding(R.id.avatar_image1, R.drawable.ct_profile4),
            ContactImageBinding(R.id.avatar_image2, R.drawable.ct_profile4),
            ContactImageBinding(R.id.avatar_image3, R.drawable.ct_profile4)
        )
    )

    private val contact2Spec = ContactWidgetSpec(
        displayName = "Contact 2",
        layoutResId = R.layout.contact_widget_provider4_r,
        widthCells = 2,
        heightCells = 2,
        clickMap = mapOf(
            R.id.root_view to "Contacts",
            R.id.top_contact to "Contacts",
            R.id.avatar_image_bg to "Contacts",
            R.id.avatar_image to "Contacts",
            R.id.call_icon_bg to "Dialer",
            R.id.call_icon2 to "Dialer",
            R.id.call_icon to "Dialer",
            R.id.message_icon_bg to "Messages",
            R.id.message_icon2 to "Messages",
            R.id.message_icon to "Messages"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Tap to select",
                defaultTextSize = 14f,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        ),
        imageBindings = listOf(
            ContactImageBinding(R.id.avatar_image, R.drawable.ct_profile4)
        )
    )

    private val contact3Spec = ContactWidgetSpec(
        displayName = "Contact 3",
        layoutResId = R.layout.contact_widget_provider3_r,
        widthCells = 2,
        heightCells = 2,
        clickMap = mapOf(
            R.id.root_view to "Contacts",
            R.id.avatar_image_bg to "Contacts",
            R.id.avatar_image to "Contacts",
            R.id.mavatar_imagessss to "Contacts",
            R.id.message_icon_bg to "Messages",
            R.id.message_icon2 to "Messages",
            R.id.message_icon to "Messages",
            R.id.whatsapp_icon to "WhatsApp",
            R.id.whatsapp to "WhatsApp",
            R.id.call_icon_bg to "Dialer",
            R.id.call_icon2 to "Dialer",
            R.id.call_icon to "Dialer"
        ),
        imageBindings = emptyList()
    )

    private val contact4Spec = ContactWidgetSpec(
        displayName = "Contact 4",
        layoutResId = R.layout.contact_widget_provider2_r,
        widthCells = 2,
        heightCells = 2,
        clickMap = mapOf(
            R.id.root_view to "Contacts",
            R.id.top_contact to "Contacts",
            R.id.name_textview to "Contacts",
            R.id.main2 to "Dialer",
            R.id.call_icon to "Dialer",
            R.id.call_icon_button to "Dialer",
            R.id.main to "Messages",
            R.id.message to "Messages",
            R.id.message_icon to "Messages"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Contact Name",
                defaultTextSize = 14f,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        )
    )

    private val contact5Spec = ContactWidgetSpec(
        displayName = "Contact 5",
        layoutResId = R.layout.contact_rounded_widget_r,
        widthCells = 2,
        heightCells = 2,
        clickMap = mapOf(
            R.id.root_view to "Contacts",
            R.id.avatar_image_bg to "Contacts",
            R.id.avatar_image to "Contacts",
            R.id.call_icon to "Dialer"
        ),
        imageBindings = listOf(
            ContactImageBinding(R.id.avatar_image, R.drawable.ct_profile5)
        )
    )

    private val contact6Spec = ContactWidgetSpec(
        displayName = "Contact 6",
        layoutResId = R.layout.contact_name_widget_provider,
        widthCells = 2,
        heightCells = 1,
        clickMap = mapOf(
            R.id.root_view to "Dialer",
            R.id.contect to "Dialer",
            R.id.top_contact to "Dialer",
            R.id.name_textview to "Dialer"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Call",
                defaultTextSize = 20f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
    )

    private val message6Spec = ContactWidgetSpec(
        displayName = "Message 6",
        layoutResId = R.layout.massage_name_widget_ovel,
        widthCells = 2,
        heightCells = 1,
        clickMap = mapOf(
            R.id.root_view to "Messages",
            R.id.contect to "Messages",
            R.id.top_contact to "Messages",
            R.id.name_textview to "Messages"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Chat",
                defaultTextSize = 20f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
    )

    private val whatsappChatSpec = ContactWidgetSpec(
        displayName = "WhatsApp Chat",
        layoutResId = R.layout.whatsapp_chat_oval_widget_r,
        widthCells = 2,
        heightCells = 1,
        clickMap = mapOf(
            R.id.root_view to "WhatsApp",
            R.id.whatsapp_icon to "WhatsApp",
            R.id.top_contact to "WhatsApp",
            R.id.name_textview to "WhatsApp"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Whatsapp",
                defaultTextSize = 20f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
    )

    private val telegramChatSpec = ContactWidgetSpec(
        displayName = "Telegram Chat",
        layoutResId = R.layout.telegram_chat_oval_widget_r,
        widthCells = 2,
        heightCells = 1,
        clickMap = mapOf(
            R.id.root_view to "Telegram",
            R.id.telegram_icon to "Telegram",
            R.id.top_contact to "Telegram",
            R.id.name_textview to "Telegram"
        ),
        textBindings = listOf(
            ContactTextBinding(
                viewId = R.id.name_textview,
                text = "Telegram",
                defaultTextSize = 20f,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
    )

    private val specsByName: Map<String, ContactWidgetSpec> = listOf(
        contact1Spec,
        contact2Spec,
        contact3Spec,
        contact4Spec,
        contact5Spec,
        contact6Spec,
        message6Spec,
        whatsappChatSpec,
        telegramChatSpec
    ).associateBy { it.displayName }

    fun getSpec(displayName: String): ContactWidgetSpec? = specsByName[displayName]

    fun hasSpec(displayName: String): Boolean = specsByName.containsKey(displayName)

    fun applyRemoteViewsContent(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        appWidgetOptions: Bundle?
    ) {
        spec.imageBindings.forEach { binding ->
            views.setImageViewResource(binding.viewId, binding.drawableRes)
        }

        spec.textBindings.forEach { binding ->
            val bitmap = WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = binding.text,
                baseTextSize = binding.defaultTextSize,
                textColor = binding.textColor,
                font = binding.font,
                applyLandscapeAdjustment = spec.heightCells == 1
            )
            views.setImageViewBitmap(binding.viewId, bitmap)
        }

        applyWidgetRuntimeStyling(context, views, spec, appWidgetOptions)
    }

    fun applyPreviewContent(context: Context, rootView: View, displayName: String) {
        val spec = getSpec(displayName) ?: return

        spec.imageBindings.forEach { binding ->
            rootView.findViewById<ImageView>(binding.viewId)?.setImageResource(binding.drawableRes)
        }

        spec.textBindings.forEach { binding ->
            val bitmap = WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = binding.text,
                baseTextSize = binding.defaultTextSize,
                textColor = binding.textColor,
                font = binding.font,
                applyLandscapeAdjustment = spec.heightCells == 1
            )
            rootView.findViewById<ImageView>(binding.viewId)?.setImageBitmap(bitmap)
        }
    }

    private fun applyWidgetRuntimeStyling(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        when (spec.displayName) {
            "Contact 1" -> applyContact1Runtime(context, views, spec, options)
            "Contact 2" -> applyContact2Runtime(context, views, spec, options)
            "Contact 3" -> applyContact3Runtime(context, views)
            "Contact 4" -> applyContact4Runtime(context, views, spec, options)
            "Contact 5" -> applyContact5Runtime(context, views, options)
            "Contact 6" -> applyContact6Runtime(context, views, spec, options)
            "Message 6" -> applyMessage6Runtime(context, views, spec, options)
            "WhatsApp Chat" -> applyWhatsappRuntime(context, views, spec, options)
            "Telegram Chat" -> applyTelegramRuntime(context, views, spec, options)
        }
    }

    private fun applyContact1Runtime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val scale = squareScaleDp(context, options) ?: return
        val avatar = 0.241f * scale
        val textBase = 0.085f * scale
        val avatarMargin = 0.1f * scale
        val textTop = 0.36f * scale
        val textEdge = 0.12f * scale

        val avatarViews = intArrayOf(
            R.id.avatar_image1,
            R.id.avatar_image2,
            R.id.avatar_image3,
            R.id.avatar_image1bg,
            R.id.avatar_image2bg,
            R.id.avatar_image3bg
        )
        avatarViews.forEach {
            views.setDpHeight(it, avatar)
            views.setDpWidth(it, avatar)
        }

        views.setDpMargin(R.id.avatar_image1bg, SIDE_TOP, avatarMargin)
        views.setDpMargin(R.id.avatar_image1bg, SIDE_START, avatarMargin)
        views.setDpMargin(R.id.avatar_image2bg, SIDE_TOP, avatarMargin)
        views.setDpMargin(R.id.avatar_image3bg, SIDE_TOP, avatarMargin)
        views.setDpMargin(R.id.avatar_image3bg, SIDE_BOTTOM, avatarMargin)

        views.setDpMargin(R.id.name_textview1, SIDE_TOP, textTop)
        views.setDpMargin(R.id.name_textview2, SIDE_TOP, textTop)
        views.setDpMargin(R.id.name_textview3, SIDE_TOP, textTop)
        views.setDpMargin(R.id.name_textview1, SIDE_START, textEdge)
        views.setDpMargin(R.id.name_textview3, SIDE_BOTTOM, textEdge)

        views.setImageViewBitmap(
            R.id.name_textview1,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context,
                truncate(textFor(spec, R.id.name_textview1, "Contact1"), 13),
                textBase,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
        views.setImageViewBitmap(
            R.id.name_textview2,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context,
                truncate(textFor(spec, R.id.name_textview2, "Contact2"), 13),
                textBase,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
        views.setImageViewBitmap(
            R.id.name_textview3,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context,
                truncate(textFor(spec, R.id.name_textview3, "Contact3"), 13),
                textBase,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG
            )
        )
    }

    private fun applyContact2Runtime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val scale = squareScaleDp(context, options) ?: return
        val icon = 0.25f * scale
        val avatar = 0.3f * scale
        val padSmall = 0.1f * scale
        val padLarge = 0.2f * scale

        views.setDpHeight(R.id.avatar_image, avatar)
        views.setDpWidth(R.id.avatar_image, avatar)
        views.setDpHeight(R.id.avatar_image_bg, avatar)
        views.setDpWidth(R.id.avatar_image_bg, avatar)
        views.setDpMargin(R.id.avatar_image_bg, SIDE_START, padSmall)

        val iconViews = intArrayOf(
            R.id.call_icon,
            R.id.message_icon,
            R.id.whatsapp_icon,
            R.id.call_icon2,
            R.id.message_icon2,
            R.id.call_icon_bg,
            R.id.message_icon_bg
        )
        iconViews.forEach {
            views.setDpHeight(it, icon)
            views.setDpWidth(it, icon)
        }

        views.setDpMargin(R.id.message_icon_bg, SIDE_END, padLarge)
        views.setDpMargin(R.id.message_icon_bg, SIDE_BOTTOM, padSmall)
        views.setDpMargin(R.id.call_icon_bg, SIDE_TOP, padLarge)
        views.setDpMargin(R.id.call_icon_bg, SIDE_BOTTOM, padSmall)

        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(textFor(spec, R.id.name_textview, "Contact Name"), 13),
                baseTextSize = 0.085f * scale,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        )
    }

    private fun applyContact3Runtime(context: Context, views: RemoteViews) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val iconColor = opaqueColor(prefs.getInt("widget_icon_color", defaultIconColor))
        val bgColor = opaqueColor(prefs.getInt("widget_bg_color", defaultBgColor))
        views.setInt(R.id.call_icon, "setColorFilter", iconColor)
        views.setInt(R.id.call_icon2, "setColorFilter", bgColor)
    }

    private fun applyContact4Runtime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val iconColor = opaqueColor(prefs.getInt("widget_icon_color", defaultIconColor))
        val bgColor = opaqueColor(prefs.getInt("widget_bg_color", defaultBgColor))
        views.setInt(R.id.call_icon_button, "setColorFilter", iconColor)
        views.setInt(R.id.message_icon, "setColorFilter", iconColor)
        views.setInt(R.id.message, "setColorFilter", bgColor)
        views.setInt(R.id.call_icon, "setColorFilter", bgColor)

        val scale = squareScaleDp(context, options) ?: return
        views.setDpHeight(R.id.name_textview, 0.35f * scale)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(textFor(spec, R.id.name_textview, "Contact Name"), 13),
                baseTextSize = 0.085f * scale,
                font = WidgetTextBitmapFactory.WidgetFont.PRODUCT_MED
            )
        )
    }

    private fun applyContact5Runtime(context: Context, views: RemoteViews, options: Bundle?) {
        val scale = squareScaleDp(context, options) ?: return
        views.setDpHeight(R.id.call_icon, scale)
        views.setDpWidth(R.id.call_icon, scale)
    }

    private fun applyContact6Runtime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val height = ovalHeightDp(context, options) ?: return
        views.setDpHeight(R.id.root_view, height)
        views.setDpHeight(R.id.contect, height)
        views.setDpWidth(R.id.contect, height)
        views.setDpMargin(R.id.name_textview, SIDE_TOP, 0.95f * height)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = truncate(textFor(spec, R.id.name_textview, "Call"), 11),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )
    }

    private fun applyMessage6Runtime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val height = ovalHeightDp(context, options) ?: return
        views.setDpHeight(R.id.root_view, height)
        views.setDpHeight(R.id.contect, height)
        views.setDpWidth(R.id.contect, height)
        views.setDpMargin(R.id.name_textview, SIDE_TOP, 0.95f * height)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = textFor(spec, R.id.name_textview, "Chat"),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )
    }

    private fun applyWhatsappRuntime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val height = ovalHeightDp(context, options) ?: return
        views.setDpHeight(R.id.root_view, height)
        views.setDpHeight(R.id.whatsapp_icon, height)
        views.setDpWidth(R.id.whatsapp_icon, 2.2f * height)
        views.setDpMargin(R.id.name_textview, SIDE_TOP, 0.95f * height)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = textFor(spec, R.id.name_textview, "Whatsapp"),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )
    }

    private fun applyTelegramRuntime(
        context: Context,
        views: RemoteViews,
        spec: ContactWidgetSpec,
        options: Bundle?
    ) {
        val height = ovalHeightDp(context, options) ?: return
        views.setDpHeight(R.id.root_view, height)
        views.setDpHeight(R.id.telegram_icon, height)
        views.setDpWidth(R.id.telegram_icon, 2.2f * height)
        views.setDpMargin(R.id.name_textview, SIDE_TOP, 0.95f * height)
        views.setImageViewBitmap(
            R.id.name_textview,
            WidgetTextBitmapFactory.createAdaptiveTextBitmap(
                context = context,
                text = textFor(spec, R.id.name_textview, "Telegram"),
                baseTextSize = 0.17f * height,
                font = WidgetTextBitmapFactory.WidgetFont.PROD_SANS_REG,
                applyLandscapeAdjustment = true
            )
        )
    }

    private fun squareScaleDp(context: Context, options: Bundle?): Float? {
        if (options == null) return null
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        if (minWidth <= 0 || minHeight <= 0) return null

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val minHeightThreshold = prefs.getInt("widget_min_height_threshold", 145)
        val maxWidthForScaling = prefs.getInt("widget_max_width_for_scaling", 200)
        return if (minHeight < minHeightThreshold) {
            min(minWidth, maxWidthForScaling).toFloat()
        } else {
            min(minWidth, minHeight).toFloat()
        }
    }

    private fun ovalHeightDp(context: Context, options: Bundle?): Float? {
        if (options == null) return null
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val prefMaxHeight = prefs.getInt("pref_oval_max_height", 72)
        val resolved = if (maxHeight > 0) min(maxHeight, prefMaxHeight) else prefMaxHeight
        return if (resolved > 0) resolved.toFloat() else null
    }

    private fun textFor(spec: ContactWidgetSpec, viewId: Int, fallback: String): String {
        return spec.textBindings.firstOrNull { it.viewId == viewId }?.text ?: fallback
    }

    private fun truncate(text: String, maxChars: Int): String {
        return if (text.length > maxChars) "${text.take(maxChars)}.." else text
    }

    private fun opaqueColor(color: Int): Int {
        return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun RemoteViews.setDpHeight(viewId: Int, value: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setViewLayoutHeight(viewId, value, TypedValue.COMPLEX_UNIT_DIP)
        }
    }

    private fun RemoteViews.setDpWidth(viewId: Int, value: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setViewLayoutWidth(viewId, value, TypedValue.COMPLEX_UNIT_DIP)
        }
    }

    private fun RemoteViews.setDpMargin(viewId: Int, side: Int, value: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setViewLayoutMargin(viewId, side, value, TypedValue.COMPLEX_UNIT_DIP)
        }
    }
}
