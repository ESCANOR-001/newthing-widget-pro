package com.newthingwidgets.clone

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.adapters.WidgetListAdapter

class WidgetDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var toolbarTitle: TextView

    companion object {
        const val EXTRA_CATEGORY_NAME = "category_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_detail)

        recyclerView = findViewById(R.id.widget_list_recycler)
        btnBack = findViewById(R.id.btn_back)
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Get category name from intent
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Newly Added"
        toolbarTitle.text = categoryName.uppercase()

        btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Newly Added"
        val spanCount = if (categoryName == "Custom Apps") 1 else 2
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        val widgets = getWidgetsForCategory(categoryName)

        recyclerView.adapter = WidgetListAdapter(
            widgets = widgets,
            isCustomAppsCategory = categoryName == "Custom Apps"
        )
    }
    
    private fun getWidgetsForCategory(categoryName: String): List<WidgetItem> {
        return when (categoryName) {
            "Apps" -> listOf(
                // App widgets - all 42 from user's list with corrected icons
                WidgetItem("Amazon", "1x1", R.drawable.amazon),
                WidgetItem("Calculator", "1x1", R.drawable.calculator),
                WidgetItem("Calendar", "1x1", R.drawable.open_cal_m),
                WidgetItem("Camera", "1x1", R.drawable.camera),
                WidgetItem("ChatGPT", "1x1", R.drawable.chatgpt),
                WidgetItem("ChatGPT", "1x1", R.drawable.chatgpt_ai),
                WidgetItem("ChatGpt Assistant", "1x1", R.drawable.chatgpt_ai_voice),
                WidgetItem("Chrome", "1x1", R.drawable.chrome),
                WidgetItem("Clock", "1x1", R.drawable.clock),
                WidgetItem("Contacts", "1x1", R.drawable.contact),
                WidgetItem("Copilot", "1x1", R.drawable.copilot),
                WidgetItem("DeepSeek", "1x1", R.drawable.deepseek),
                WidgetItem("Dialer", "1x1", R.drawable.dial),
                WidgetItem("Discord", "1x1", R.drawable.discord),
                WidgetItem("Email", "1x1", R.drawable.open_email),
                WidgetItem("Facebook", "1x1", R.drawable.facebook),
                WidgetItem("File Manger", "1x1", R.drawable.file_manager),
                WidgetItem("Gallery", "1x1", R.drawable.gallery),
                WidgetItem("Gemini Assistant", "1x1", R.drawable.open_gemini),
                WidgetItem("Google", "1x1", R.drawable.google),
                WidgetItem("Google Maps", "1x1", R.drawable.map),
                WidgetItem("Grok", "1x1", R.drawable.open_grok),
                WidgetItem("Instagram", "1x1", R.drawable.instagram),
                WidgetItem("Messages", "1x1", R.drawable.message),
                WidgetItem("MXPlayer", "1x1", R.drawable.mx_player),
                WidgetItem("Netflix", "1x1", R.drawable.netflix),
                WidgetItem("Photos", "1x1", R.drawable.photos),
                WidgetItem("Play Store", "1x1", R.drawable.playstore),
                WidgetItem("Reddit", "1x1", R.drawable.reddit),
                WidgetItem("Settings", "1x1", R.drawable.settings),
                WidgetItem("Snapchat", "1x1", R.drawable.snapchat),
                WidgetItem("Spotify", "1x1", R.drawable.spotify),
                WidgetItem("Telegram", "1x1", R.drawable.telegram_n),
                WidgetItem("Threads", "1x1", R.drawable.threads),
                WidgetItem("TikTok", "1x1", R.drawable.tiktok),
                WidgetItem("VLC", "1x1", R.drawable.vlc),
                WidgetItem("WhatsApp", "1x1", R.drawable.whatsapp),
                WidgetItem("X (Twitter)", "1x1", R.drawable.x),
                WidgetItem("YouTube", "1x1", R.drawable.youtube),
                WidgetItem("Incognito", "1x1", R.drawable.incognito_tab),
                WidgetItem("Google Lens", "1x1", R.drawable.google_lens),
                WidgetItem("Perplexity", "1x1", R.drawable.perplexity_ai)
            )
            "Battery" -> listOf(
                // Charging widget with real-time updates
                WidgetItem("Charging", "3x2", R.drawable.charging_widget_preview),
                // Square battery widget with segmented bars
                WidgetItem("Battery Square", "2x2", R.drawable.bat_preview),
                // Battery Bolt with lightning bolt fill
                WidgetItem("Battery Bolt", "2x2", R.drawable.bat_preview),
                // Battery Status with progress bar
                WidgetItem("Battery Status", "2x2", R.drawable.bat_preview),
                // Battery Meter with horizontal segments
                WidgetItem("Battery Meter", "2x2", R.drawable.bat_preview),
                // Battery Dot Matrix with dot grid
                WidgetItem("Battery Dot Matrix", "2x2", R.drawable.bat_preview)
            )
            "Calendar" -> listOf(
                // Date Time Matrix with day, time, month, date
                WidgetItem("Date Time Matrix", "2x2", R.drawable.cal_01),
                // Date Clock Widget with AM/PM, large time, day, date
                WidgetItem("Date Clock Widget", "3x2", R.drawable.cal_01),
                // Calendar Widget with month grid
                WidgetItem("Calendar Widget", "2x2", R.drawable.cal_01)
            )
            "Clock" -> listOf(
                // Dot Matrix Clock with large time display
                WidgetItem("Dot Matrix Clock", "2x2", R.drawable.analog_2),
                // Minimalist Analog Clock
                WidgetItem("Minimalist Analog Clock", "2x2", R.drawable.analog_2),
                // Classic Analog Clock with tick marks and dot grid
                WidgetItem("Classic Analog Clock", "2x2", R.drawable.analog_2),
                // Square Analog Clock
                WidgetItem("Square Analog Clock", "2x2", R.drawable.analog_5),
                // Glow Circle Analog Clock (from analog12 style)
                WidgetItem("Glow Circle Analog Clock", "2x2", R.drawable.analog_2),
                // Drop Pulse Analog Clock
                WidgetItem("Drop Pulse Analog Clock", "2x2", R.drawable.drop_pulse_analog_clock_preview)
            )
            "Custom Apps" -> listOf(
                WidgetItem("Social App Combo", "4x1", R.drawable.combo_social_apps),
                WidgetItem("Social App Combo Glass", "4x1", R.drawable.combo_social_apps2),
                WidgetItem("Google App Combo", "4x1", R.drawable.combo_search_bar6),
                WidgetItem("Google App Combo Glass", "4x1", R.drawable.combo_search_bar7),
                WidgetItem("AI App Combo", "4x1", R.drawable.combo_ai_mix_3),
                WidgetItem("AI App Combo Glass", "4x1", R.drawable.combo_ai_mix_4)
            )
            else -> emptyList()
        }
    }
}

data class WidgetItem(
    val name: String,
    val size: String,
    val previewDrawable: Int
)
