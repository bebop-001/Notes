/*
 *  Copyright 2019 Steven Smith kana-tutor.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied.
 *
 *  See the License for the specific language governing permissions
 *  and limitations under the License.
 */
@file:Suppress("SpellCheckingInspection")

package com.kana_tutor.notes

import android.app.Activity
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

import com.kana_tutor.notes.kanautils.displayBuildInfo
import com.kana_tutor.notes.kanautils.promptForShortcut

import kotlinx.android.synthetic.main.activity_main.*
import android.provider.DocumentsContract.Document.*
import android.system.Os
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.kana_tutor.notes.kanautils.displayUsage
import com.kana_tutor.notes.kanautils.kToast
import java.io.*
import java.util.*

class FileProperties {
    var displayName = ""
    var uri :String = ""
    var size = -1
    var isWritable = false
    var internalWriteProtect = false
    private var lastModified = 0L
    private var lastModifiedDate = Date(0).toString()
    var isEmpty = true
    private var documentId = ""
    private var authority = ""
    var fileName = ""

    // extension functions.
    private fun Cursor.getKeyedString(key: String): String
            = getString(getColumnIndex(key))
    private fun Cursor.getKeyedInt(key: String): Int
            = getInt(getColumnIndex(key))
    private fun Cursor.getKeyedLong(key: String): Long
            = getLong(getColumnIndex(key))
    constructor(context : Context, uri : Uri) {
        val c = context.contentResolver.query(
            uri, null, null, null, null)
        c?.apply {
            moveToFirst()
            size = getKeyedInt(COLUMN_SIZE)
            displayName = getKeyedString(COLUMN_DISPLAY_NAME)
            lastModified = getKeyedLong(COLUMN_LAST_MODIFIED)
            lastModifiedDate = Date(lastModified).toString()
            isWritable = (getKeyedInt(COLUMN_FLAGS) and FLAG_SUPPORTS_WRITE) != 0
            documentId = getKeyedString(COLUMN_DOCUMENT_ID)
            authority = uri.authority.toString()
            isEmpty = false
        }
        c?.close()
        // based on code from https://stackoverflow.com/questions/30546441/
        // android-open-file-with-intent-chooser-from-uri-obtained-by-storage-access-frame
        val pf : ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
        if (pf != null) {
            val procFile = File("/proc/self/fd/" + pf.fd)
            fileName  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Os.readlink(procFile.toString())
            else
                procFile.canonicalPath
        }
        this.uri = uri.toString()
    }
    // return empty file properties.
    constructor()
    override fun toString(): String {
        return String.format(
            "%s:size=%d,isWritable:%b,lastModifiedDate:%s,id=\"%s\"uri=\"%s\", fileName=\"%s\""
            , displayName, size, isWritable, lastModifiedDate,documentId, uri, fileName
        )
    }
    fun formatedProperties(activity : Activity) : String {
        val writable = !isEmpty && isWritable && !internalWriteProtect
        return String.format(
            activity.getString(R.string.file_properties_format)
            , ContextCompat.getColor(
                activity, R.color.file_edit_window_font_color) and 0x00FFFFFF
            , displayName, size, lastModifiedDate, writable)
    }
}

const val CREATE_REQUEST_CODE = 40
const val OPEN_REQUEST_CODE = 41
const val SAVE_AS_REQUEST_CODE = 42

const val appPrefsFileName = "userPrefs.xml"
class MainActivity : AppCompatActivity() {

    companion object {
        var displayTheme = 0 // for light or dark theme.
        private lateinit var currentEditWindow : EditWindow
    }

    private lateinit var _userPreferences : SharedPreferences
    private val userPreferences : SharedPreferences
    get() = _userPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _userPreferences = getSharedPreferences(
            appPrefsFileName,
            Context.MODE_PRIVATE
        )
        val firstRun = userPreferences.getBoolean("firstRun", true)
        if (firstRun) {
            userPreferences.edit()
                .putBoolean("firstRun", false)
                .apply()
            // Ask user if they want a shortcut on their home screen.
            promptForShortcut(this, MainActivity::class.java)
        }
        displayTheme = userPreferences.getInt("displayTheme", R.string.light_theme)
        if (displayTheme == R.string.light_theme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        currentEditWindow = EditWindow.newInstance("hello world")

        if (savedInstanceState == null) {
            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.add(R.id.fragment_placeholder, currentEditWindow)
            transaction.commit()
        }

        toolbar.overflowIcon = ContextCompat.getDrawable(
            this, R.drawable.vert_ellipsis_light_img)
        setSupportActionBar(toolbar)
        supportActionBar!!.setLogo(R.mipmap.notes_launcher)
        supportActionBar!!.title = ""
    }
    private fun newFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "")

        startActivityForResult(intent, CREATE_REQUEST_CODE)
    }
    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        startActivityForResult(intent, OPEN_REQUEST_CODE)
    }
    private fun saveFileAs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        startActivityForResult(intent, SAVE_AS_REQUEST_CODE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int,
                                         resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CREATE_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        currentEditWindow.newFile(uri)
                        supportActionBar!!.title =
                            currentEditWindow.currentFileProperties.displayName
                    }
                }
                SAVE_AS_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        currentEditWindow.saveAs(uri)
                        supportActionBar!!.title =
                            currentEditWindow.currentFileProperties.displayName
                    }
                }
                OPEN_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        currentEditWindow.openFile(uri)
                        supportActionBar!!.title =
                            currentEditWindow.currentFileProperties.displayName

                    }
                }
                else -> throw RuntimeException(String.format("onActivityResult" +
                            "Unexpected request code: 0x%08x", requestCode
                    )
                )
            }
        }
    }
    private fun displayFileProperties() {
        val webview = WebView(this)
        webview.setBackgroundColor(ContextCompat.getColor(this, R.color.file_edit_window_bg))
        webview.loadData(currentEditWindow
            .currentFileProperties
            .formatedProperties(this)
            , "text/html", "utf-8"
        )
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(webview)
            .show()

    }

    private fun changeDisplayTheme(newTheme : String) {
        // if currentName is dark, select dark theme.
        displayTheme = R.string.dark_theme
        if (newTheme == resources.getString(R.string.light_theme))
            displayTheme = R.string.light_theme
        getSharedPreferences(appPrefsFileName, Context.MODE_PRIVATE)
            .edit()
            .putInt("displayTheme", displayTheme)
            .apply()
        recreate()
    }
    private fun writeProtectFile() {
        var fp = currentEditWindow.currentFileProperties
        fp.internalWriteProtect = !fp.internalWriteProtect
    }
    // Menu item selected listener.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rv = super.onOptionsItemSelected(item)
        Log.d("MainActivity:", "onOptionsItemSelected called")

        when (item.itemId) {
            R.id.build_info_item -> displayBuildInfo(this)
            R.id.usage_item -> displayUsage(this)
            R.id.select_display_theme -> changeDisplayTheme(item.title.toString())
        }
        Log.d("MainActivity:", "onOptionsItemSelected done")
        return rv
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d("MainActivity:", "onPrepareOptionsMenu called")
        super.onPrepareOptionsMenu(menu)
        menu!!.findItem(R.id.select_display_theme)
            .setTitle(
                if (MainActivity.displayTheme == R.string.light_theme)
                    R.string.dark_theme
                else
                    R.string.light_theme
            )
        Log.d("MainActivity:", "onPrepareOptionsMenu finished")

        return true
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d("MainActivity:", "onCreateOptionsMenu called")
        var rv = super.onCreateOptionsMenu(menu)

        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return false
    }

}


