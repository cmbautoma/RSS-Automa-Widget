package com.moncho.feedlywidget

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.moncho.feedlywidget.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val pickOpmlRequest = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        findViewById<Button>(R.id.btn_import_opml).setOnClickListener {
            pickOPML()
        }
        findViewById<Button>(R.id.btn_sync_now).setOnClickListener {
            WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
            Toast.makeText(this, "Sincronizando…", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickOPML() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, pickOpmlRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickOpmlRequest && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val count = OpmlImporter.importFromUri(this@SettingsActivity, uri)
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@SettingsActivity, "Importadas $count fuentes", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
