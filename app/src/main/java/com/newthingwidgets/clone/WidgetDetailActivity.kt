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
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Newly Added"
        
        val widgets = when (categoryName) {
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
            else -> listOf(
                // Clocks
                WidgetItem("Analog Clock 2", "2x2", R.drawable.analog_2),
                WidgetItem("Analog Clock 5", "2x2", R.drawable.analog_5),
                WidgetItem("Analog Clock 6", "2x2", R.drawable.analog_6),
                WidgetItem("Hybrid Clock 4", "2x2", R.drawable.hybrid_clock_info),
                WidgetItem("Hybrid Clock 5", "2x2", R.drawable.hybrid_5),
                WidgetItem("Digital Clock 1", "2x2", R.drawable.digital_clock_rounded_info),
                
                // Calendar
                WidgetItem("Calendar 3", "2x2", R.drawable.cal_01),
                WidgetItem("Calendar 4", "2x2", R.drawable.open_cal),
                
                // Battery
                WidgetItem("Battery 2", "2x2", R.drawable.bat_preview),
                WidgetItem("Battery 4", "2x2", R.drawable.battery_01),
                
                // Folder
                WidgetItem("Folder 3", "2x2", R.drawable.square_folder),
                WidgetItem("Folder 4", "2x2", R.drawable.rounded_folder),
                
                // Search
                WidgetItem("Search 1", "2x2", R.drawable.google_search),
                WidgetItem("Search 2", "2x2", R.drawable.search_box),
                
                // Weather
                WidgetItem("Weather 3", "2x2", R.drawable.weather_squar),
                
                // Alarm
                WidgetItem("Alarm Clock", "2x2", R.drawable.alarm_widget_p)
            )
        }
        
        recyclerView.adapter = WidgetListAdapter(widgets)
    }
}

data class WidgetItem(
    val name: String,
    val size: String,
    val previewDrawable: Int
)
