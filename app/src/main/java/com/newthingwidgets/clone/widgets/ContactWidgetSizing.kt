package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.TypedValue
import android.widget.RemoteViews
import kotlin.math.min

object ContactWidgetSizing {

    private const val DEFAULT_MIN_HEIGHT_THRESHOLD = 145
    private const val DEFAULT_MAX_WIDTH_FOR_SCALING = 200
    private const val DEFAULT_OVAL_MAX_HEIGHT = 72
    private const val DEFAULT_SQUARE_SCALE_DP = 150f

    fun squareScaleDp(context: Context, options: Bundle?): Float {
        val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        if (minWidth <= 0 || minHeight <= 0) {
            return DEFAULT_SQUARE_SCALE_DP
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val minHeightThreshold = prefs.getInt("widget_min_height_threshold", DEFAULT_MIN_HEIGHT_THRESHOLD)
        val maxWidthForScaling = prefs.getInt("widget_max_width_for_scaling", DEFAULT_MAX_WIDTH_FOR_SCALING)
        return if (minHeight < minHeightThreshold) {
            min(minWidth, maxWidthForScaling).toFloat()
        } else {
            min(minWidth, minHeight).toFloat()
        }
    }

    fun ovalHeightDp(context: Context, options: Bundle?): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val prefMaxHeight = prefs.getInt("pref_oval_max_height", DEFAULT_OVAL_MAX_HEIGHT)
        val maxHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) ?: 0
        val resolved = if (maxHeight > 0) min(maxHeight, prefMaxHeight) else prefMaxHeight
        return resolved.coerceAtLeast(1).toFloat()
    }

    fun setViewHeightDp(views: RemoteViews, viewId: Int, valueDp: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutHeight(viewId, valueDp, TypedValue.COMPLEX_UNIT_DIP)
        }
    }

    fun setViewWidthDp(views: RemoteViews, viewId: Int, valueDp: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutWidth(viewId, valueDp, TypedValue.COMPLEX_UNIT_DIP)
        }
    }

    fun setViewMarginDp(views: RemoteViews, viewId: Int, side: Int, valueDp: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutMargin(viewId, side, valueDp, TypedValue.COMPLEX_UNIT_DIP)
        }
    }
}