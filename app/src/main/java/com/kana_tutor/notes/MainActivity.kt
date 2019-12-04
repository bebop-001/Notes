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

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

import com.kana_tutor.notes.kanautils.displayBuildInfo
import com.kana_tutor.notes.kanautils.promptForShortcut

import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.app.AppCompatDelegate
import com.kana_tutor.notes.kanautils.displayUsage



const val appPrefsFileName = "userPrefs.xml"
class MainActivity : AppCompatActivity(), EditWindow.EditWinEventListener {

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

    override fun onResume() {
        super.onResume()
        supportActionBar!!.title = currentEditWindow.getCurrentDisplayName()
    }

    private fun changeDisplayTheme(newTheme : String) : Boolean {
        // if currentName is dark, select dark theme.
        displayTheme = R.string.dark_theme
        if (newTheme == resources.getString(R.string.light_theme))
            displayTheme = R.string.light_theme
        getSharedPreferences(appPrefsFileName, Context.MODE_PRIVATE)
            .edit()
            .putInt("displayTheme", displayTheme)
            .apply()
        recreate()
        return true
    }
    // Menu item selected listener.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("MainActivity:", "onOptionsItemSelected called")

        when (item.itemId) {
            R.id.build_info_item        -> return displayBuildInfo(this)
            R.id.usage_item             -> return displayUsage(this)
            R.id.select_display_theme   -> return changeDisplayTheme(item.title.toString())
            else                        -> return super.onOptionsItemSelected(item)
        }
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

    override fun titleChanged(title: String) {
        if (supportActionBar != null) {
            supportActionBar!!.title = title
        }
    }

}


