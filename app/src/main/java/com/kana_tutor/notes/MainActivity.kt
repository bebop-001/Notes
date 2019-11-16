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
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.kana_tutor.notes.kanautils.promptForShortcut

import java.io.FileOutputStream
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var _userPreferences : SharedPreferences
    val userPreferences : SharedPreferences
    get() = _userPreferences

    private val CREATE_REQUEST_CODE = 40
    private val OPEN_REQUEST_CODE = 41
    private val SAVE_REQUEST_CODE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _userPreferences = getSharedPreferences(
            getString(R.string.app_name) + ".user_preferences"
            , Context.MODE_PRIVATE
        )
        var firstRun = userPreferences.getBoolean("firstRun", true)
        if (firstRun) {
            userPreferences.edit()
                .putBoolean("firstRun", false)
                .apply()
            // Ask user if they want a shortcut on their home screen.
            promptForShortcut(this, MainActivity::class.java)
        }

        toolbar.overflowIcon = ContextCompat.getDrawable(
            this, R.drawable.vert_ellipsis_light_img)
        setSupportActionBar(toolbar)

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

    // view declaration necessary for API 19 default button callback
    fun newFile(view: View) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "newfile.txt")

        startActivityForResult(intent, CREATE_REQUEST_CODE)
    }
    // view declaration necessary for API 19 default button callback
    fun saveFile(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        startActivityForResult(intent, SAVE_REQUEST_CODE)
    }

    private fun writeFileContent(uri: Uri) {
        try {
            val pfd = contentResolver.openFileDescriptor(uri, "w")

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

    // view declaration necessary for API 19 default button callback
    fun openFile(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        startActivityForResult(intent, OPEN_REQUEST_CODE)
    }

    private fun readFileContent(uri: Uri): String {

        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(
            inputStream))
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
                    if (resultData != null) {
                        fileText.setText("")
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

    // Menu item selected listener.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rv = true
        return rv
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
