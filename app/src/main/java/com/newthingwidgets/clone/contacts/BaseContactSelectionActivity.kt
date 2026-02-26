package com.newthingwidgets.clone.contacts

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.ContactWidgetPrefs
import com.newthingwidgets.clone.widgets.SelectedContact
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.R as AppCompatR
import java.util.concurrent.Executors

abstract class BaseContactSelectionActivity : AppCompatActivity() {

    protected abstract val providerClass: Class<*>
    protected open val selectionLayoutResId: Int = R.layout.activity_contact_selection
    protected open val includePhotoAndId: Boolean = true

    protected var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private val contactsExecutor = Executors.newSingleThreadExecutor()
    private lateinit var adapter: ContactSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)
        setContentView(selectionLayoutResId)

        appWidgetId = extractAppWidgetId(intent)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        adapter = ContactSelectionAdapter(::onContactSelected)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.search_view)
        makeSearchViewFullyClickable(searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })

        ensureContactsPermissionAndLoad()
    }

    private fun makeSearchViewFullyClickable(searchView: SearchView) {
        // Expand it so the whole bar is visible/clickable (not just the icon),
        // but don't auto-pop the keyboard on activity start.
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.clearFocus()

        val searchText =
            searchView.findViewById<EditText?>(AppCompatR.id.search_src_text)

        fun focusAndShowKeyboard() {
            searchView.isIconified = false
            val target: View = (searchText ?: searchView)
            target.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            if (imm != null) {
                imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Make the entire SearchView area click-to-focus (not just the icon).
        val clickTargets = listOfNotNull(
            searchView,
            searchView.findViewById(AppCompatR.id.search_plate),
            searchView.findViewById(AppCompatR.id.submit_area),
            searchView.findViewById(AppCompatR.id.search_edit_frame),
            searchText
        )
        clickTargets.forEach { v ->
            v.isClickable = true
            v.isFocusable = true
            v.isFocusableInTouchMode = true
            v.setOnClickListener { focusAndShowKeyboard() }
        }
    }

    override fun onDestroy() {
        contactsExecutor.shutdownNow()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CONTACTS) {
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    protected open fun persistContactSelection(contact: SelectedContact) {
        ContactWidgetPrefs.putContact(
            context = this,
            appWidgetId = appWidgetId,
            contact = contact,
            includePhotoAndId = includePhotoAndId
        )
    }

    private fun onContactSelected(contact: SelectedContact) {
        persistContactSelection(contact)
        ContactWidgetPrefs.requestWidgetRefresh(this, providerClass, appWidgetId)

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("appWidgetId", appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun ensureContactsPermissionAndLoad() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            loadContacts()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS
            )
        }
    }

    private fun loadContacts() {
        contactsExecutor.execute {
            val contacts = queryContacts()
            runOnUiThread {
                adapter.submitContacts(contacts)
            }
        }
    }

    private fun queryContacts(): List<SelectedContact> {
        val contacts = mutableListOf<SelectedContact>()
        val dedupe = HashSet<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )

        val sortOrder =
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

            while (cursor.moveToNext()) {
                val name = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
                val phone = if (phoneIndex >= 0) cursor.getString(phoneIndex) else null
                val photoUri = if (photoIndex >= 0) cursor.getString(photoIndex) else null
                val contactId = if (idIndex >= 0) cursor.getLong(idIndex) else -1L

                if (name.isBlank()) {
                    continue
                }

                val dedupeKey = "$contactId:${phone.orEmpty()}"
                if (!dedupe.add(dedupeKey)) {
                    continue
                }

                contacts.add(
                    SelectedContact(
                        name = name,
                        phone = phone,
                        photoUri = photoUri,
                        contactId = if (contactId >= 0L) contactId else null
                    )
                )
            }
        }

        return contacts
    }

    private fun extractAppWidgetId(intent: Intent): Int {
        val fromSystem = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (fromSystem != AppWidgetManager.INVALID_APPWIDGET_ID) {
            return fromSystem
        }
        return intent.getIntExtra("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    private companion object {
        const val REQUEST_CONTACTS = 100
    }
}