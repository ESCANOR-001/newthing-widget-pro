package com.newthingwidgets.clone.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.newthingwidgets.clone.AppPackages

object AppLaunchRouter {

    fun launchOrInstallApp(context: Context, appName: String) {
        val pm = context.packageManager
        val installedPackage = AppPackages.findInstalledPackage(context, appName)

        if (installedPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(installedPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }
        }

        val playStorePackage = AppPackages.getPlayStorePackage(appName) ?: return
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$playStorePackage")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$playStorePackage")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
