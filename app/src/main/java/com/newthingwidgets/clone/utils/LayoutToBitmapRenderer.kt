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
        "Battery Meter" to WidgetLayoutInfo(R.layout.battery_meter_widget, 2, 2),
        "Battery Dot Matrix" to WidgetLayoutInfo(R.layout.battery_dot_matrix_widget, 2, 2),
        "Date Time Matrix" to WidgetLayoutInfo(R.layout.date_time_matrix_widget, 2, 2),
        "Date Clock Widget" to WidgetLayoutInfo(R.layout.date_clock_widget, 3, 2),
        "Weekly Calendar Widget" to WidgetLayoutInfo(R.layout.weekly_calendar_widget, 2, 2),
        "Calendar Widget" to WidgetLayoutInfo(R.layout.calendar_widget, 2, 2),
        "Dot Matrix Clock" to WidgetLayoutInfo(R.layout.dot_matrix_clock_widget, 2, 2),
        "Minimalist Analog Clock" to WidgetLayoutInfo(R.layout.minimalist_clock_widget, 2, 2),
        "Classic Analog Clock" to WidgetLayoutInfo(R.layout.classic_clock_widget, 2, 2),
        "Hybrid Clock Widget" to WidgetLayoutInfo(R.layout.hybrid_clock_widget, 2, 2)
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
            "Battery Dot Matrix" -> populateBatteryDotMatrixWidget(view, context)
            "Date Time Matrix" -> populateDateTimeMatrixWidget(view, context)
            "Date Clock Widget" -> populateDateClockWidget(view, context)
            "Weekly Calendar Widget" -> populateWeeklyCalendarWidget(view, context)
            "Calendar Widget" -> populateWeeklyCalendarWidget(view, context)
            "Dot Matrix Clock" -> populateDotMatrixClockWidget(view, context)
            "Minimalist Analog Clock" -> populateMinimalistClockWidget(view, context)
            "Classic Analog Clock" -> populateClassicClockWidget(view, context)
            "Hybrid Clock Widget" -> populateHybridClockWidget(view, context)
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

    /**
     * Populate Battery Dot Matrix widget with demo data
     */
    private fun populateBatteryDotMatrixWidget(view: View, context: Context) {
        // Create dot matrix bitmap for preview
        val dotMatrixBitmap = createDotMatrixPreviewBitmap(context, DEMO_BATTERY_PCT)
        view.findViewById<ImageView>(R.id.dot_matrix_image)?.setImageBitmap(dotMatrixBitmap)
    }

    /**
     * Create dot matrix bitmap for preview (10 columns × 10 rows = 100 dots)
     */
    private fun createDotMatrixPreviewBitmap(context: Context, batteryPct: Int): Bitmap {
        val columns = 10
        val rows = 10
        
        val density = context.resources.displayMetrics.density
        val dotRadius = 8f * density
        val dotSpacing = 18f * density
        
        // Calculate bitmap dimensions
        val width = (columns * dotSpacing).toInt()
        val height = (rows * dotSpacing).toInt()
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Paint for filled dots (red)
        val filledPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = context.getColor(R.color.red_color)
            style = android.graphics.Paint.Style.FILL
        }
        
        // Paint for empty dots (gray)
        val emptyPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF404040.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        
        // Calculate how many dots should be EMPTY (from top)
        val emptyDots = 100 - batteryPct
        
        // Draw dots row by row, empty from top, filled from bottom
        var dotIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val cx = (col * dotSpacing) + (dotSpacing / 2)
                val cy = (row * dotSpacing) + (dotSpacing / 2)
                
                val paint = if (dotIndex < emptyDots) emptyPaint else filledPaint
                canvas.drawCircle(cx, cy, dotRadius, paint)
                
                dotIndex++
            }
        }
        
        return bitmap
    }

    /**
     * Populate Date Time Matrix widget with demo data
     */
    private fun populateDateTimeMatrixWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        
        // Demo data - show example date/time
        val dayOfWeek = "Wed"
        val time = "12:12am"
        val month = "February"
        val dateWithSuffix = "19th"
        
        // Load custom font
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        // Create bitmaps for each text element
        val whiteColor = 0xFFFFFFFF.toInt()
        val redColor = context.getColor(R.color.red_color)
        
        val dayBitmap = createPreviewTextBitmap(dayOfWeek, 28f * density, whiteColor, typeface)
        val timeBitmap = createPreviewTextBitmap(time, 42f * density, redColor, typeface)
        val monthBitmap = createPreviewTextBitmap(month, 28f * density, whiteColor, typeface)
        val dateBitmap = createPreviewTextBitmap(dateWithSuffix, 28f * density, whiteColor, typeface)
        
        // Set bitmaps to ImageViews
        view.findViewById<ImageView>(R.id.day_of_week_image)?.setImageBitmap(dayBitmap)
        view.findViewById<ImageView>(R.id.time_image)?.setImageBitmap(timeBitmap)
        view.findViewById<ImageView>(R.id.month_image)?.setImageBitmap(monthBitmap)
        view.findViewById<ImageView>(R.id.date_image)?.setImageBitmap(dateBitmap)
    }

    /**
     * Create text bitmap for preview with specified font
     */
    private fun createPreviewTextBitmap(
        text: String,
        textSize: Float,
        textColor: Int,
        typeface: android.graphics.Typeface?
    ): Bitmap {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            color = textColor
            this.typeface = typeface
        }
        
        // Measure text
        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = paint.measureText(text).toInt()
        val textHeight = bounds.height()
        
        // Create bitmap with padding
        val padding = 4
        val bitmap = Bitmap.createBitmap(
            (textWidth + padding * 2).coerceAtLeast(1),
            (textHeight + padding * 2).coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        
        // Draw text
        canvas.drawText(text, padding.toFloat(), textHeight.toFloat() + padding, paint)
        
        return bitmap
    }

    /**
     * Populate Date Clock Widget with demo data
     */
    private fun populateDateClockWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        
        // Demo data
        val amPm = "PM"
        val time = "6:26"
        val dayOfWeek = "Monday"
        val dateText = "NOVEMBER 25"
        
        // Set text views
        view.findViewById<TextView>(R.id.ampm_pill)?.text = amPm
        view.findViewById<TextView>(R.id.day_text)?.text = dayOfWeek
        view.findViewById<TextView>(R.id.date_text)?.text = dateText
        
        // Create time bitmap with Nothing font and reduced opacity red
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        val timeColor = 0x66E53935.toInt()  // 40% opacity red
        val textSize = 80f * density
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            color = timeColor
            this.typeface = typeface
        }
        
        val bounds = android.graphics.Rect()
        paint.getTextBounds(time, 0, time.length, bounds)
        val textWidth = paint.measureText(time).toInt()
        val textHeight = bounds.height()
        
        val padding = (8 * density).toInt()
        val bitmap = Bitmap.createBitmap(
            (textWidth + padding * 2).coerceAtLeast(1),
            (textHeight + padding * 2).coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawText(time, padding.toFloat(), textHeight.toFloat() + padding, paint)
        
        view.findViewById<ImageView>(R.id.time_image)?.setImageBitmap(bitmap)
    }

    /**
     * Populate Weekly Calendar Widget with demo data (full month view)
     */
    private fun populateWeeklyCalendarWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        
        // Demo data - December 21
        val monthName = "December"
        val currentDay = 21
        val daysInMonth = 31
        val firstDayOffset = 0 // December 2024 starts on Sunday (0 offset)
        
        // Create month bitmap with Nothing font
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        val whiteColor = 0xFFFFFFFF.toInt()
        val textSize = 36f * density
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            color = whiteColor
            this.typeface = typeface
        }
        
        val bounds = android.graphics.Rect()
        paint.getTextBounds(monthName, 0, monthName.length, bounds)
        val textWidth = paint.measureText(monthName).toInt()
        val textHeight = bounds.height()
        
        val padding = (4 * density).toInt()
        val bitmap = Bitmap.createBitmap(
            (textWidth + padding * 2).coerceAtLeast(1),
            (textHeight + padding * 2).coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawText(monthName, padding.toFloat(), textHeight.toFloat() + padding, paint)
        
        view.findViewById<ImageView>(R.id.month_image)?.setImageBitmap(bitmap)
        
        // Set day number (red)
        view.findViewById<TextView>(R.id.day_number)?.text = currentDay.toString()
        
        // Populate 31 grid cells (only show current month days)
        val dayIds = listOf(
            R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6, R.id.day_7,
            R.id.day_8, R.id.day_9, R.id.day_10, R.id.day_11, R.id.day_12, R.id.day_13, R.id.day_14,
            R.id.day_15, R.id.day_16, R.id.day_17, R.id.day_18, R.id.day_19, R.id.day_20, R.id.day_21,
            R.id.day_22, R.id.day_23, R.id.day_24, R.id.day_25, R.id.day_26, R.id.day_27, R.id.day_28,
            R.id.day_29, R.id.day_30, R.id.day_31
        )
        
        for (i in 0 until 31) {
            val dayView = view.findViewById<TextView>(dayIds[i])
            val dayNum = i + 1
            
            if (dayNum <= daysInMonth) {
                dayView?.text = dayNum.toString()
                if (dayNum == currentDay) {
                    dayView?.setBackgroundResource(R.drawable.current_day_bg)
                    dayView?.setTextColor(0xFF1A1A1A.toInt())
                } else {
                    dayView?.setTextColor(whiteColor)
                }
            } else {
                dayView?.text = ""
            }
        }
    }

    /**
     * Populate Dot Matrix Clock Widget with demo data
     */
    private fun populateDotMatrixClockWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        
        // Demo data
        val dayOfWeek = "SUNDAY"
        val time = "10:51"
        val date = "21 DEC 2025"
        
        // Set day of week
        view.findViewById<TextView>(R.id.day_of_week)?.text = dayOfWeek
        
        // Set date
        view.findViewById<TextView>(R.id.date_text)?.text = date
        
        // Create time bitmap with Nothing font
        val typeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
        } catch (e: Exception) {
            null
        }
        
        val redColor = 0xFFE53935.toInt()
        val textSize = 72f * density
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            color = redColor
            this.typeface = typeface
        }
        
        val bounds = android.graphics.Rect()
        paint.getTextBounds(time, 0, time.length, bounds)
        val textWidth = paint.measureText(time).toInt()
        val textHeight = bounds.height()
        
        val padding = (8 * density).toInt()
        val bitmap = Bitmap.createBitmap(
            (textWidth + padding * 2).coerceAtLeast(1),
            (textHeight + padding * 2).coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawText(time, padding.toFloat(), textHeight.toFloat() + padding, paint)
        
        view.findViewById<ImageView>(R.id.time_image)?.setImageBitmap(bitmap)
    }

    /**
     * Populate Minimalist Analog Clock Widget with demo data
     */
    private fun populateMinimalistClockWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        val size = (150 * density).toInt()
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val centerX = size / 2f
        val centerY = size / 2f
        val radius = kotlin.math.min(centerX, centerY) - 4f
        
        // Demo time: 10:10:15
        val hours = 10
        val minutes = 10
        val seconds = 15
        
        // Draw dark circular background
        val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF1A1A1A.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        
        // Calculate angles
        val hourAngle = Math.toRadians(((hours % 12) * 30.0 + minutes * 0.5) - 90)
        val minuteAngle = Math.toRadians((minutes * 6.0 + seconds * 0.1) - 90)
        val secondAngle = Math.toRadians((seconds * 6.0) - 90)
        
        // Draw minute hand FIRST (gray, thinner, extends across)
        val minutePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF888888.toInt()
            strokeWidth = radius * 0.035f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val minuteLength = radius * 0.80f
        val minuteEndX = centerX + (minuteLength * kotlin.math.cos(minuteAngle)).toFloat()
        val minuteEndY = centerY + (minuteLength * kotlin.math.sin(minuteAngle)).toFloat()
        val minuteBackLength = radius * 0.12f
        val minuteBackX = centerX - (minuteBackLength * kotlin.math.cos(minuteAngle)).toFloat()
        val minuteBackY = centerY - (minuteBackLength * kotlin.math.sin(minuteAngle)).toFloat()
        canvas.drawLine(minuteBackX, minuteBackY, minuteEndX, minuteEndY, minutePaint)
        
        // Draw hour hand ON TOP (white, thick, no back extension)
        val hourPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = radius * 0.07f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val hourLength = radius * 0.40f
        val hourStartOffset = radius * 0.05f
        val hourStartX = centerX + (hourStartOffset * kotlin.math.cos(hourAngle)).toFloat()
        val hourStartY = centerY + (hourStartOffset * kotlin.math.sin(hourAngle)).toFloat()
        val hourEndX = centerX + (hourLength * kotlin.math.cos(hourAngle)).toFloat()
        val hourEndY = centerY + (hourLength * kotlin.math.sin(hourAngle)).toFloat()
        canvas.drawLine(hourStartX, hourStartY, hourEndX, hourEndY, hourPaint)
        
        // Draw second indicator (red dot)
        val secondPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        val secondDotRadius = radius * 0.045f
        val secondDistance = radius * 0.80f
        val secondX = centerX + (secondDistance * kotlin.math.cos(secondAngle)).toFloat()
        val secondY = centerY + (secondDistance * kotlin.math.sin(secondAngle)).toFloat()
        canvas.drawCircle(secondX, secondY, secondDotRadius, secondPaint)
        
        // Draw center pivot point (small red circle)
        val centerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        val centerRadius = radius * 0.04f
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint)
        
        view.findViewById<ImageView>(R.id.clock_face)?.setImageBitmap(bitmap)
    }

    /**
     * Populate Classic Analog Clock Widget with demo data
     */
    private fun populateClassicClockWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        val size = (150 * density).toInt()
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val centerX = size / 2f
        val centerY = size / 2f
        val radius = kotlin.math.min(centerX, centerY) - 4f
        
        // Demo time: 10:10:15
        val hours = 10
        val minutes = 10
        val seconds = 15
        
        // Draw dark circular background
        val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF1A1A1A.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        
        // Draw inner ring
        val ringPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF2A2A2A.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = radius * 0.08f
        }
        canvas.drawCircle(centerX, centerY, radius * 0.92f, ringPaint)
        
        // Draw tick marks at 12, 3, 6, 9
        val tickPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF666666.toInt()
            strokeWidth = 2f * density
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0) - 90)
            val innerR = radius * 0.75f
            val outerR = radius * 0.85f
            val x1 = centerX + (innerR * kotlin.math.cos(angle)).toFloat()
            val y1 = centerY + (innerR * kotlin.math.sin(angle)).toFloat()
            val x2 = centerX + (outerR * kotlin.math.cos(angle)).toFloat()
            val y2 = centerY + (outerR * kotlin.math.sin(angle)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }
        
        // Calculate angles
        val hourAngle = Math.toRadians(((hours % 12) * 30.0 + minutes * 0.5) - 90)
        val minuteAngle = Math.toRadians((minutes * 6.0 + seconds * 0.1) - 90)
        val secondAngle = Math.toRadians((seconds * 6.0) - 90)
        
        // Draw minute hand (white, thin, long)
        val minutePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = radius * 0.03f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val minuteLength = radius * 0.70f
        val minuteEndX = centerX + (minuteLength * kotlin.math.cos(minuteAngle)).toFloat()
        val minuteEndY = centerY + (minuteLength * kotlin.math.sin(minuteAngle)).toFloat()
        canvas.drawLine(centerX, centerY, minuteEndX, minuteEndY, minutePaint)
        
        // Draw hour hand (white, thick, short)
        val hourPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = radius * 0.06f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val hourLength = radius * 0.45f
        val hourEndX = centerX + (hourLength * kotlin.math.cos(hourAngle)).toFloat()
        val hourEndY = centerY + (hourLength * kotlin.math.sin(hourAngle)).toFloat()
        canvas.drawLine(centerX, centerY, hourEndX, hourEndY, hourPaint)
        
        // Draw second hand (red, thin)
        val secondPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            strokeWidth = radius * 0.015f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val secondLength = radius * 0.75f
        val secondEndX = centerX + (secondLength * kotlin.math.cos(secondAngle)).toFloat()
        val secondEndY = centerY + (secondLength * kotlin.math.sin(secondAngle)).toFloat()
        val secondBackLength = radius * 0.15f
        val secondBackX = centerX - (secondBackLength * kotlin.math.cos(secondAngle)).toFloat()
        val secondBackY = centerY - (secondBackLength * kotlin.math.sin(secondAngle)).toFloat()
        canvas.drawLine(secondBackX, secondBackY, secondEndX, secondEndY, secondPaint)
        
        // Draw center pivot (red)
        val centerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius * 0.04f, centerPaint)
        
        view.findViewById<ImageView>(R.id.clock_face)?.setImageBitmap(bitmap)
    }

    /**
     * Populate Hybrid Clock Widget with demo data
     */
    private fun populateHybridClockWidget(view: View, context: Context) {
        val density = context.resources.displayMetrics.density
        val size = (150 * density).toInt()
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val centerX = size / 2f
        val centerY = size / 2f
        val minDim = kotlin.math.min(centerX, centerY)
        
        // Demo time: 10:10:30
        val hours = 10
        val minutes = 10
        val seconds = 30
        
        // Draw rounded square background
        val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFF1A1A1A.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        val cornerRadius = minDim * 0.15f
        canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), cornerRadius, cornerRadius, bgPaint)
        
        // Draw hour tick marks at 12, 3, 6, 9
        val tickPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = 2f * density
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        val innerR = minDim * 0.75f
        val outerR = minDim * 0.90f
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0) - 90)
            val x1 = centerX + (innerR * kotlin.math.cos(angle)).toFloat()
            val y1 = centerY + (innerR * kotlin.math.sin(angle)).toFloat()
            val x2 = centerX + (outerR * kotlin.math.cos(angle)).toFloat()
            val y2 = centerY + (outerR * kotlin.math.sin(angle)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }
        
        // Calculate angles
        val hourAngle = Math.toRadians(((hours % 12) * 30.0 + minutes * 0.5) - 90)
        val minuteAngle = Math.toRadians((minutes * 6.0 + seconds * 0.1) - 90)
        val secondAngle = Math.toRadians((seconds * 6.0) - 90)
        
        // Draw minute hand (solid white)
        val minutePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = minDim * 0.02f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val minuteLength = minDim * 0.70f
        val minuteEndX = centerX + (minuteLength * kotlin.math.cos(minuteAngle)).toFloat()
        val minuteEndY = centerY + (minuteLength * kotlin.math.sin(minuteAngle)).toFloat()
        canvas.drawLine(centerX, centerY, minuteEndX, minuteEndY, minutePaint)
        
        // Draw hour hand (outline/hollow)
        val hourPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            strokeWidth = minDim * 0.03f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val hourLength = minDim * 0.45f
        val hourEndX = centerX + (hourLength * kotlin.math.cos(hourAngle)).toFloat()
        val hourEndY = centerY + (hourLength * kotlin.math.sin(hourAngle)).toFloat()
        canvas.drawLine(centerX, centerY, hourEndX, hourEndY, hourPaint)
        
        // Draw second hand (red)
        val secondPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            strokeWidth = minDim * 0.015f
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }
        val secondLength = minDim * 0.70f
        val secondEndX = centerX + (secondLength * kotlin.math.cos(secondAngle)).toFloat()
        val secondEndY = centerY + (secondLength * kotlin.math.sin(secondAngle)).toFloat()
        canvas.drawLine(centerX, centerY, secondEndX, secondEndY, secondPaint)
        
        // Draw center pivot (red with white outline)
        val centerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = 0xFFE53935.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, minDim * 0.04f, centerPaint)
        
        view.findViewById<ImageView>(R.id.clock_face)?.setImageBitmap(bitmap)
    }
}
