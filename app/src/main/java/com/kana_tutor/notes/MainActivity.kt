/*
 *  Copyright 2018 Steven Smith kana-tutor.com
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
import android.graphics.Typeface
import android.view.View
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

import com.kana_tutor.notes.kanautils.displayBuildInfo
import com.kana_tutor.notes.kanautils.promptForShortcut

import kotlinx.android.synthetic.main.activity_main.*
import android.provider.DocumentsContract.Document.*
import android.system.Os
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.kana_tutor.notes.kanautils.kToast
import java.io.*
import java.net.URLEncoder.encode
import java.text.SimpleDateFormat
import java.util.*

val appPrefsFileName = "userPrefs.xml"
class MainActivity : AppCompatActivity() {

    companion object {
        var displayTheme = 0 // for light or dark theme.
    }

        private lateinit var _userPreferences : SharedPreferences
    val userPreferences : SharedPreferences
    get() = _userPreferences

    var currentFileProperties = FileProperties()

    private val CREATE_REQUEST_CODE = 40
    private val OPEN_REQUEST_CODE = 41
    private val SAVE_REQUEST_CODE = 42
    private val SAVE_AS_REQUEST_CODE = 43

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _userPreferences = getSharedPreferences(
            appPrefsFileName,
            Context.MODE_PRIVATE
        )
        var firstRun = userPreferences.getBoolean("firstRun", true)
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

        toolbar.overflowIcon = ContextCompat.getDrawable(
            this, R.drawable.vert_ellipsis_light_img)
        setSupportActionBar(toolbar)
        supportActionBar!!.setLogo(R.mipmap.notes_launcher)
        supportActionBar!!.title = ""

        fileText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d("TextListener", "beforeTextChanged")
            }
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("TextListener", "onTextChanged")
            }
            override fun afterTextChanged(s: Editable) {
                Log.d("TextListener", "afterTextChanged")
            }
        })
        // Things work ok with listener commented out.  for demo only.
        fileText.setOnTouchListener { v, event ->
            Log.d("OnTouch:", String.format("event=%s", event.toString()))
            // imm.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            false // set false indicating listener handeled event.
        }
        // for demo
        fileText.setOnFocusChangeListener { v, hasFocus ->
            Log.d("OnFocusChangeListener"
                , String.format("hasFocus:%s", hasFocus.toString()))
        }
    }

    fun newFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "newfile.txt")

        startActivityForResult(intent, CREATE_REQUEST_CODE)
    }
    fun saveFile() {
        val currentUri = Uri.parse(currentFileProperties.uri)
        val pfd = contentResolver.openFileDescriptor(currentUri, "w")
        if (pfd != null) {
            val fileOutputStream = FileOutputStream(
                pfd.fileDescriptor
            )
            val textContent = fileText.text.toString()
            fileOutputStream.write(textContent.toByteArray())
            fileOutputStream.close()
            currentFileProperties = FileProperties(this,currentUri)
            kToast(this, getString(R.string.saveFormat
                , currentFileProperties.displayName, currentFileProperties.size))
        }
        else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"

            startActivityForResult(intent, SAVE_REQUEST_CODE)
        }
    }
    fun saveFileAs() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        startActivityForResult(intent, SAVE_AS_REQUEST_CODE)
    }
    private fun newFileContent(uri: Uri) {
        currentFileProperties = FileProperties(this, uri)
        supportActionBar!!.title = currentFileProperties.displayName
        Log.d("newFileContents:", currentFileProperties.toString())
        fileText.setText("")
    }

    private fun writeFileContent(uri: Uri) {
        try {
            val pfd = contentResolver.openFileDescriptor(uri, "w")
            currentFileProperties = FileProperties(this, uri)
            Log.d("writeFileContent", currentFileProperties.toString())

            supportActionBar!!.title = currentFileProperties.displayName

            val fileOutputStream = FileOutputStream(
                pfd?.fileDescriptor)

            val textContent = fileText.text.toString()

            fileOutputStream.write(textContent.toByteArray())

            fileOutputStream.close()
            pfd?.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        startActivityForResult(intent, OPEN_REQUEST_CODE)
    }

    private fun Cursor.getKeyedString(key: String): String
            = getString(getColumnIndex(key))
    private fun Cursor.getKeyedInt(key: String): Int
            = getInt(getColumnIndex(key))
    private fun Cursor.getKeyedLong(key: String): Long
            = getLong(getColumnIndex(key))

    inner class FileProperties {
        var displayName = ""
        var uri :String = ""
        var size = -1
        var isWritable = false
        var lastModified = 0L
        var lastModifiedDate = Date(0).toString()
        var isInitialized = false
        var documentId = ""
        var authority = ""
        var fileName = ""

        constructor(a : AppCompatActivity, uri : Uri) {
            val c = a.contentResolver.query(
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
                isInitialized = true
            }
            // based on code from https://stackoverflow.com/questions/30546441/
            // android-open-file-with-intent-chooser-from-uri-obtained-by-storage-access-frame
            val pf : ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
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
            return super.toString()
        }
        fun formatedProperties(activity : Activity) : String {
            return String.format(activity.getString(R.string.file_properties_format)
                , displayName, size, lastModifiedDate, isWritable)
        }
    }


    private fun readFileContent(uri: Uri): String {
        currentFileProperties = FileProperties(this, uri)
        Log.d("readFileContent", currentFileProperties.toString())
        supportActionBar!!.title = currentFileProperties.displayName
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()

        var currentline = reader.readLine()

        while (currentline != null) {
            stringBuilder.append(currentline + "\n")
            currentline = reader.readLine()
        }
        inputStream?.close()
        return stringBuilder.toString()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int,
                                         resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        var currentUri: Uri?

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CREATE_REQUEST_CODE -> {
                    resultData?.let {
                        currentUri = it.data
                        currentUri?.let {
                            newFileContent(it)
                        }
                    }
                }
                SAVE_REQUEST_CODE -> {
                    resultData?.let {
                        currentUri = it.data
                        currentUri?.let {
                            writeFileContent(it)
                        }
                    }
                }
                SAVE_AS_REQUEST_CODE -> {
                    resultData?.let {
                        currentUri = it.data
                        currentUri?.let {
                            writeFileContent(it)
                        }
                    }
                }
                OPEN_REQUEST_CODE -> {
                    resultData?.let {
                        currentUri = it.data
                        currentUri?.let {
                            try {
                                val content = readFileContent(it)
                                fileText.setText(content)
                                // fileText.requestFocus()
                            } catch (e: IOException) {
                                throw RuntimeException("Open failed:" + e.message + e.stackTrace)
                            }
                        }
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
        val htmlString = currentFileProperties
            .formatedProperties(this)
        val textView = TextView(this)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
            setTypeface(null, Typeface.BOLD)
            text = com.kana_tutor.notes.kanautils.htmlString(htmlString)
            gravity = Gravity.CENTER
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(textView)
            .show()

    }
    private fun changeDisplayTheme(newTheme : String) {
        // if currentName is dark, select dark theme.
        val n = newTheme
        Log.d("menu:new 1", String.format("%s -> %s, 0x%08x",n, resources.getString(displayTheme),  displayTheme))

        if (newTheme == resources.getString(R.string.light_theme))
            displayTheme = R.string.light_theme
        else
            displayTheme = R.string.dark_theme
        getSharedPreferences(appPrefsFileName, Context.MODE_PRIVATE)
            .edit()
            .putInt("displayTheme", displayTheme)
            .commit()
        recreate()
        Log.d("menu:new 2", String.format("%s -> %s, 0x%08x",n, resources.getString(displayTheme), displayTheme))
    }
    // Menu item selected listener.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rv = true
        when (item.itemId) {
            R.id.save_file_item -> saveFile()
            R.id.save_as_file_item -> saveFileAs()
            R.id.open_file_item -> openFile()
            R.id.new_file_item -> newFile()
            R.id.file_properties_item -> displayFileProperties()
            R.id.build_info_item -> displayBuildInfo(this)
            R.id.select_display_theme -> changeDisplayTheme(item.title.toString())
            else -> rv = super.onOptionsItemSelected(item);
        }
        return rv
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply() {
            Log.d("menu:pre", String.format("=%s", findItem(R.id.select_display_theme).title))

            // disable save unless we have a file.
            findItem(R.id.save_file_item)
                .isEnabled = currentFileProperties.uri != ""
            findItem(R.id.save_as_file_item)
                .isEnabled = currentFileProperties.uri != ""
            findItem(R.id.select_display_theme)
                .setTitle(
                    if (displayTheme == R.string.light_theme)
                        R.string.dark_theme
                    else
                        R.string.light_theme
                )
            Log.d("menu:post", String.format("=%s. 0x%08x", findItem(R.id.select_display_theme).title, displayTheme))
        }
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}


