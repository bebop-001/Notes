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

package com.kana_tutor.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private val CREATE_SHORTCUT = 99
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_SHORTCUT) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getStringExtra("result")
                Log.d("MainActivity", "CreateShortcut result:" + result!!)
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    private lateinit var _userPreferences : SharedPreferences
    val userPreferences : SharedPreferences
    get() = _userPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _userPreferences = getSharedPreferences(
            getString(R.string.app_name) + ".user_preferences", Context.MODE_PRIVATE
        )
        var firstRun = userPreferences.getBoolean("firstRun", true)
        if (firstRun || true) {
            userPreferences.edit()
                .putBoolean("firstRun", false)
                .apply()
            com.kana_tutor.notes.kanautils.promptForShortcut(this, MainActivity::class.java)
        }
    }
}
