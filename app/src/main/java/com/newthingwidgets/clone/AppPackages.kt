package com.newthingwidgets.clone

import android.content.Context
import android.content.pm.PackageManager
import com.newthingwidgets.clone.R

/**
 * Maps app names to their package names for launching and Play Store redirects.
 * Includes search keywords to dynamically find regional variants of apps.
 */
object AppPackages {
    
    data class AppInfo(
        val name: String,
        val packageName: String, // Default/fallback package for Play Store
        val drawableRes: Int,
        val packageSearchKeywords: List<String> = emptyList() // Keywords to search in installed packages
    )
    
    val apps: Map<String, AppInfo> = mapOf(
        "Amazon" to AppInfo("Amazon", "com.amazon.mShop.android.shopping", R.drawable.amazon, 
            listOf("amazon.mshop", "amazon.shopping")),
        "Calculator" to AppInfo("Calculator", "com.google.android.calculator", R.drawable.calculator, 
            listOf("calculator")),
        "Calendar" to AppInfo("Calendar", "com.google.android.calendar", R.drawable.open_cal_m, 
            listOf(".calendar")),
        "Camera" to AppInfo("Camera", "com.android.camera2", R.drawable.camera, 
            listOf(".camera")),
        "ChatGPT" to AppInfo("ChatGPT", "com.openai.chatgpt", R.drawable.chatgpt, 
            listOf("openai.chatgpt")),
        "ChatGpt Assistant" to AppInfo("ChatGpt Assistant", "com.openai.chatgpt", R.drawable.chatgpt_ai_voice, 
            listOf("openai.chatgpt")),
        "Chrome" to AppInfo("Chrome", "com.android.chrome", R.drawable.chrome, 
            listOf("android.chrome")),
        "Clock" to AppInfo("Clock", "com.google.android.deskclock", R.drawable.clock, 
            listOf("deskclock", ".clock")),
        "Contacts" to AppInfo("Contacts", "com.google.android.contacts", R.drawable.contact, 
            listOf(".contacts")),
        "Copilot" to AppInfo("Copilot", "com.microsoft.copilot", R.drawable.copilot, 
            listOf("microsoft.copilot")),
        "DeepSeek" to AppInfo("DeepSeek", "com.deepseek.chat", R.drawable.deepseek, 
            listOf("deepseek")),
        "Dialer" to AppInfo("Dialer", "com.google.android.dialer", R.drawable.dial, 
            listOf(".dialer")),
        "Discord" to AppInfo("Discord", "com.discord", R.drawable.discord, 
            listOf("discord")),
        "Email" to AppInfo("Email", "com.google.android.gm", R.drawable.open_email, 
            listOf("android.gm", ".email", "gmail")),
        "Facebook" to AppInfo("Facebook", "com.facebook.katana", R.drawable.facebook, 
            listOf("facebook.katana")),
        "File Manger" to AppInfo("File Manger", "com.google.android.apps.nbu.files", R.drawable.file_manager, 
            listOf("nbu.files", "documentsui", "filemanager")),
        "Gallery" to AppInfo("Gallery", "com.google.android.apps.photos", R.drawable.gallery, 
            listOf("gallery", ".photos")),
        "Gemini Assistant" to AppInfo("Gemini Assistant", "com.google.android.apps.bard", R.drawable.open_gemini, 
            listOf("apps.bard", "gemini")),
        "Google" to AppInfo("Google", "com.google.android.googlequicksearchbox", R.drawable.google, 
            listOf("googlequicksearchbox")),
        "Google Maps" to AppInfo("Google Maps", "com.google.android.apps.maps", R.drawable.map, 
            listOf("apps.maps")),
        "Grok" to AppInfo("Grok", "com.x.grok", R.drawable.open_grok, 
            listOf("x.grok", "grok")),
        "Instagram" to AppInfo("Instagram", "com.instagram.android", R.drawable.instagram, 
            listOf("instagram.android")),
        "Messages" to AppInfo("Messages", "com.google.android.apps.messaging", R.drawable.message, 
            listOf("messaging", ".mms")),
        "MXPlayer" to AppInfo("MXPlayer", "com.mxtech.videoplayer.ad", R.drawable.mx_player, 
            listOf("mxtech.videoplayer")),
        "Netflix" to AppInfo("Netflix", "com.netflix.mediaclient", R.drawable.netflix, 
            listOf("netflix")),
        "Photos" to AppInfo("Photos", "com.google.android.apps.photos", R.drawable.photos, 
            listOf("apps.photos")),
        "Play Store" to AppInfo("Play Store", "com.android.vending", R.drawable.playstore, 
            listOf("android.vending")),
        "Reddit" to AppInfo("Reddit", "com.reddit.frontpage", R.drawable.reddit, 
            listOf("reddit")),
        "Settings" to AppInfo("Settings", "com.android.settings", R.drawable.settings, 
            listOf("android.settings")),
        "Snapchat" to AppInfo("Snapchat", "com.snapchat.android", R.drawable.snapchat, 
            listOf("snapchat")),
        "Spotify" to AppInfo("Spotify", "com.spotify.music", R.drawable.spotify, 
            listOf("spotify")),
        "Telegram" to AppInfo("Telegram", "org.telegram.messenger", R.drawable.telegram_n, 
            listOf("telegram")),
        "Threads" to AppInfo("Threads", "com.instagram.barcelona", R.drawable.threads, 
            listOf("instagram.barcelona")),
        "TikTok" to AppInfo("TikTok", "com.zhiliaoapp.musically", R.drawable.tiktok, 
            listOf("musically", "tiktok")),
        "WhatsApp" to AppInfo("WhatsApp", "com.whatsapp", R.drawable.whatsapp, 
            listOf("whatsapp")),
        "X (Twitter)" to AppInfo("X (Twitter)", "com.twitter.android", R.drawable.x, 
            listOf("twitter")),
        "YouTube" to AppInfo("YouTube", "com.google.android.youtube", R.drawable.youtube, 
            listOf("android.youtube")),
        "Google Lens" to AppInfo("Google Lens", "com.google.ar.lens", R.drawable.google_lens, 
            listOf("ar.lens", "google.lens")),
        "Incognito Tab" to AppInfo("Incognito Tab", "com.opera.browser", R.drawable.incognito_tab, 
            listOf("opera.browser")),
        "Messenger" to AppInfo("Messenger", "com.facebook.orca", R.drawable.facebook, 
            listOf("facebook.orca")),
        "Perplexity AI" to AppInfo("Perplexity AI", "ai.perplexity.app.android", R.drawable.perplexity_ai, 
            listOf("perplexity"))
    )
    
    /**
     * Get app info by name
     */
    fun getAppInfo(appName: String): AppInfo? = apps[appName]
    
    /**
     * Get package name by app name
     */
    fun getPackageName(appName: String): String? = apps[appName]?.packageName
    
    /**
     * Find installed app package by searching through user's installed apps.
     * Returns the first matching package name, or null if not found.
     */
    fun findInstalledPackage(context: Context, appName: String): String? {
        val appInfo = apps[appName] ?: return null
        val pm = context.packageManager
        
        // Get all installed packages
        val installedPackages = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            return null
        }
        
        // Search for matching packages using keywords
        for (keyword in appInfo.packageSearchKeywords) {
            val keywordLower = keyword.lowercase()
            for (pkg in installedPackages) {
                val pkgNameLower = pkg.packageName.lowercase()
                if (pkgNameLower.contains(keywordLower)) {
                    // Verify this package can be launched
                    if (pm.getLaunchIntentForPackage(pkg.packageName) != null) {
                        return pkg.packageName
                    }
                }
            }
        }
        
        // Fallback: try default package name
        if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
            return appInfo.packageName
        }
        
        return null
    }
    
    /**
     * Get the package to use for Play Store redirect
     */
    fun getPlayStorePackage(appName: String): String? = apps[appName]?.packageName
}
