package com.kana_tutor.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kana_tutor.notes.kanautils.CreateShortcut

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

            val i = Intent(applicationContext, CreateShortcut::class.java)
            i.putExtra("shortcutId", getString(R.string.app_name))
            startActivityForResult(i, CREATE_SHORTCUT)
        }
    }
}
