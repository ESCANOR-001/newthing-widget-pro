package com.newthingwidgets.clone.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.newthingwidgets.clone.R

/**
 * Utility class to render layout XML files as Bitmap images
 * Used for generating widget previews dynamically
 */
object LayoutToBitmapRenderer {

    /**
     * Widget info: layout resource ID and aspect ratio (width:height)
     */
    data class WidgetLayoutInfo(
        val layoutResId: Int,
        val widthCells: Int,
        val heightCells: Int
    )

    /**
     * Maps widget names to their layout resource IDs and cell sizes
     */
    private val widgetLayoutMap = mapOf(
        "Charging" to WidgetLayoutInfo(R.layout.charging_widget, 3, 2),
        "Battery Square" to WidgetLayoutInfo(R.layout.square_battery_widget, 2, 2),
        "Battery Bolt" to WidgetLayoutInfo(R.layout.battery_bolt_widget, 2, 2),
        "Battery Status" to WidgetLayoutInfo(R.layout.battery_status_widget, 2, 2),
        "Battery Meter" to WidgetLayoutInfo(R.layout.battery_meter_widget, 2, 2)
        // Battery 3 and Battery 4 use static drawables
    )

    // Base cell size in dp - use larger size to render elements at proper proportions
    // Actual widget cells are ~74dp, but we render at 150dp for quality, then scale down
    private const val CELL_SIZE_DP = 150

    // Demo battery percentage for previews
    private const val DEMO_BATTERY_PCT = 75

    /**
     * Render a layout XML as a Bitmap at proper proportions
     * Renders at full size then scales down for preview
     * 
     * @param context Application context
     * @param widgetName Name of the widget to render
     * @param targetSizeDp Target preview size in dp (for the larger dimension)
     * @return Bitmap of the rendered layout, or null if widget not found
     */
    fun renderWidgetPreview(
        context: Context,
        widgetName: String,
        targetSizeDp: Int = 145
    ): Bitmap? {
        val info = widgetLayoutMap[widgetName] ?: return null
        
        val density = context.resources.displayMetrics.density
        
        // Calculate render size based on cell dimensions
        val renderWidthDp = info.widthCells * CELL_SIZE_DP
        val renderHeightDp = info.heightCells * CELL_SIZE_DP
        
        val renderWidthPx = (renderWidthDp * density).toInt()
        val renderHeightPx = (renderHeightDp * density).toInt()

        // Inflate the layout
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(info.layoutResId, null)

        // Populate with demo data to show how the widget actually looks
        populateDemoData(view, widgetName, context)

        // Measure the view with exact dimensions matching widget proportions
        val widthSpec = MeasureSpec.makeMeasureSpec(renderWidthPx, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(renderHeightPx, MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)

        // Layout the view
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create bitmap and canvas at full render size
        val fullBitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(fullBitmap)

        // Draw the view onto the canvas
        view.draw(canvas)

        // Scale down to target size while maintaining aspect ratio
        val targetPx = (targetSizeDp * density).toInt()
        val scaleFactor = if (renderWidthPx > renderHeightPx) {
            targetPx.toFloat() / renderWidthPx
        } else {
            targetPx.toFloat() / renderHeightPx
        }
        
        val scaledWidth = (renderWidthPx * scaleFactor).toInt()
        val scaledHeight = (renderHeightPx * scaleFactor).toInt()

        // Scale the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaledWidth, scaledHeight, true)
        
        // Recycle the full-size bitmap to save memory
        if (scaledBitmap != fullBitmap) {
            fullBitmap.recycle()
        }

        return scaledBitmap
    }

    /**
     * Populate the inflated view with demo data to show realistic preview
     */
    private fun populateDemoData(view: View, widgetName: String, context: Context) {
        when (widgetName) {
            "Charging" -> populateChargingWidget(view, context)
            "Battery Square" -> populateBatterySquareWidget(view, context)
            "Battery Bolt" -> populateBatteryBoltWidget(view, context)
            "Battery Status" -> populateBatteryStatusWidget(view)
            "Battery Meter" -> populateBatteryMeterWidget(view, context)
        }
    }

    /**
     * Populate Charging widget with demo data
     */
    private fun populateChargingWidget(view: View, context: Context) {
        // Set charging status text to "Battery" like the actual widget shows when preview
        view.findViewById<TextView>(R.id.charging_status)?.text = "Battery"
        
        // Hide charging icon for preview (show as discharging state)
        view.findViewById<ImageView>(R.id.charging_icon)?.visibility = View.GONE
        
        // Create battery info bitmap with custom font
        val batteryInfoText = "${DEMO_BATTERY_PCT}% • 6h 30min left"
        val batteryInfoBitmap = createTextBitmap(context, batteryInfoText, 64f, android.graphics.Color.WHITE)
        view.findViewById<ImageView>(R.id.battery_info_image)?.setImageBitmap(batteryInfoBitmap)
        
        // Set progress bar
        view.findViewById<ProgressBar>(R.id.progress_bar_view)?.progress = DEMO_BATTERY_PCT
    }
    
    /**
     * Create a bitmap with text rendered using the custom font
     */
    private fun createTextBitmap(context: Context, text: String, textSize: Float, textColor: Int): Bitmap {
        // Load custom font
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            color = textColor
            this.typeface = typeface
        }
        
        // Measure text dimensions
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = paint.measureText(text).toInt()
        val textHeight = textBounds.height()
        
        // Create bitmap with padding
        val paddingH = 16
        val paddingV = 12
        val bitmap = Bitmap.createBitmap(textWidth + paddingH * 2, textHeight + paddingV * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw text centered
        canvas.drawText(text, paddingH.toFloat(), textHeight.toFloat() + paddingV, paint)
        
        return bitmap
    }

    /**
     * Populate Battery Square widget with demo data
     */
    private fun populateBatterySquareWidget(view: View, context: Context) {
        // Set percentage
        view.findViewById<TextView>(R.id.battery_percentage)?.text = "${DEMO_BATTERY_PCT}%"
        
        // Set time remaining
        view.findViewById<TextView>(R.id.time_remaining)?.text = "~2 hours"
        
        // Fill segments based on percentage (75% = 4 segments filled, partial on 4th)
        val segmentIds = listOf(
            R.id.segment_1, R.id.segment_2, R.id.segment_3, R.id.segment_4, R.id.segment_5
        )
        
        // 75% means 3 full segments + partial 4th
        val filledSegments = (DEMO_BATTERY_PCT / 20) // 75/20 = 3
        
        for ((index, segmentId) in segmentIds.withIndex()) {
            val segmentView = view.findViewById<ImageView>(segmentId)
            if (index < filledSegments) {
                segmentView?.setImageResource(R.drawable.battery_segment_filled)
            } else if (index == filledSegments) {
                // Partial fill for the current segment
                val remainderPercent = DEMO_BATTERY_PCT % 20
                val drawableRes = when {
                    remainderPercent >= 15 -> R.drawable.battery_segment_threequarters
                    remainderPercent >= 10 -> R.drawable.battery_segment_half
                    remainderPercent >= 5 -> R.drawable.battery_segment_quarter
                    else -> R.drawable.battery_segment_empty
                }
                segmentView?.setImageResource(drawableRes)
            } else {
                segmentView?.setImageResource(R.drawable.battery_segment_empty)
            }
        }
    }

    /**
     * Populate Battery Bolt widget with demo data
     */
    private fun populateBatteryBoltWidget(view: View, context: Context) {
        // Set percentage number
        view.findViewById<TextView>(R.id.battery_number)?.text = "${DEMO_BATTERY_PCT}%"
        
        // Set status text
        view.findViewById<TextView>(R.id.status_text)?.text = "Charged"
        
        // Set time text
        view.findViewById<TextView>(R.id.time_text)?.text = "1,5 hours\nfor full charge"
        
        // Set bolt icon (75% = threequarters filled)
        view.findViewById<ImageView>(R.id.bolt_icon)?.setImageResource(R.drawable.ic_bolt_threequarters)
    }

    /**
     * Populate Battery Status widget with demo data
     */
    private fun populateBatteryStatusWidget(view: View) {
        // Set percentage
        view.findViewById<TextView>(R.id.battery_percentage)?.text = "${DEMO_BATTERY_PCT}%"
        
        // Set charging status
        view.findViewById<TextView>(R.id.charging_status)?.text = "CHARGING"
        
        // Set time remaining
        view.findViewById<TextView>(R.id.time_remaining)?.text = "1h 30m left"
        
        // Set progress bar
        view.findViewById<ProgressBar>(R.id.battery_progress)?.progress = DEMO_BATTERY_PCT
        
        // Show charging bolt
        view.findViewById<ImageView>(R.id.charging_bolt_small)?.visibility = View.VISIBLE
        
        // Set battery icon to charging
        view.findViewById<ImageView>(R.id.battery_icon)?.setImageResource(R.drawable.ic_battery_charging)
    }

    /**
     * Legacy method for backwards compatibility
     */
    fun renderLayoutToBitmap(
        context: Context,
        layoutResId: Int,
        widthDp: Int = 150,
        heightDp: Int = 150
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layoutResId, null)

        val widthSpec = MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(heightPx, MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    /**
     * Get layout resource ID for a widget name
     * Returns null if the widget uses static drawable instead
     */
    fun getLayoutForWidget(widgetName: String): Int? {
        return widgetLayoutMap[widgetName]?.layoutResId
    }

    /**
     * Check if a widget should use dynamic layout preview
     */
    fun hasDynamicPreview(widgetName: String): Boolean {
        return widgetLayoutMap.containsKey(widgetName)
    }

    /**
     * Populate Battery Meter widget with demo data
     */
    private fun populateBatteryMeterWidget(view: View, context: Context) {
        // Segment IDs for the 10 battery bars
        val segmentIds = listOf(
            R.id.segment_1, R.id.segment_2, R.id.segment_3, R.id.segment_4, R.id.segment_5,
            R.id.segment_6, R.id.segment_7, R.id.segment_8, R.id.segment_9, R.id.segment_10
        )
        
        // Fill segments based on 75% (7 full segments)
        val filledSegments = DEMO_BATTERY_PCT / 10  // 75/10 = 7
        
        for ((index, segmentId) in segmentIds.withIndex()) {
            val segmentView = view.findViewById<ImageView>(segmentId)
            val drawable = if (index < filledSegments) {
                R.drawable.battery_meter_segment_filled
            } else {
                R.drawable.battery_meter_segment_empty
            }
            segmentView?.setImageResource(drawable)
        }
        
        // Create and set battery percentage bitmap with red color
        val percentageBitmap = createBatteryMeterPercentageBitmap(context, DEMO_BATTERY_PCT)
        view.findViewById<ImageView>(R.id.battery_percentage_image)?.setImageBitmap(percentageBitmap)
        
        // Set time value
        view.findViewById<TextView>(R.id.time_value)?.text = "4H20M"
    }

    /**
     * Create percentage bitmap for Battery Meter with red color
     */
    private fun createBatteryMeterPercentageBitmap(context: Context, batteryPct: Int): Bitmap {
        val text = "$batteryPct"
        val percentSign = "%"
        
        // Load custom font
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        // Red color for percentage
        val redColor = context.getColor(R.color.red_color)
        
        // Paint for main number
        val mainPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 72f
            color = redColor
            this.typeface = typeface
        }
        
        // Paint for percent sign (smaller)
        val percentPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 36f
            color = redColor
            this.typeface = typeface
        }
        
        // Measure text dimensions
        val mainBounds = android.graphics.Rect()
        mainPaint.getTextBounds(text, 0, text.length, mainBounds)
        val mainWidth = mainPaint.measureText(text).toInt()
        
        val percentBounds = android.graphics.Rect()
        percentPaint.getTextBounds(percentSign, 0, percentSign.length, percentBounds)
        val percentWidth = percentPaint.measureText(percentSign).toInt()
        
        // Create bitmap
        val totalWidth = mainWidth + percentWidth + 8
        val totalHeight = mainBounds.height() + 20
        
        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw main number
        canvas.drawText(text, 0f, mainBounds.height().toFloat() + 10, mainPaint)
        
        // Draw percent sign
        canvas.drawText(percentSign, mainWidth.toFloat() + 4, mainBounds.height().toFloat() + 10, percentPaint)
        
        return bitmap
    }
}
