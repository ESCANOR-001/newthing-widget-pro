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
        toolbarTitle.text = categoryName

        btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        
        // All Newly Added widgets (excluding those requiring permissions)
        val widgets = listOf(
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
        
        recyclerView.adapter = WidgetListAdapter(widgets)
    }
}

data class WidgetItem(
    val name: String,
    val size: String,
    val previewDrawable: Int
)
