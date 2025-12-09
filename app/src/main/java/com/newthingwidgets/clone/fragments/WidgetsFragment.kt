package com.newthingwidgets.clone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.adapters.WidgetCategoriesAdapter

class WidgetsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var clearSearch: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_widgets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.widgets_recycler)
        searchEditText = view.findViewById(R.id.search_edit_text)
        clearSearch = view.findViewById(R.id.clear_search)

        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = WidgetCategoriesAdapter()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                // TODO: Filter widgets based on search query
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearch.setOnClickListener {
            searchEditText.text.clear()
        }
    }
}
