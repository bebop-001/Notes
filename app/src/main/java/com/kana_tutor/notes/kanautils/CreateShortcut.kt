package com.kana_tutor.notes.kanautils

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

/*
 * This module asks user if they want a shortcut installed on their
 * home directory.  It is designed to work from API 19 through the
 * current API-29.
 */

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.util.Log
import android.widget.Toast

import android.graphics.drawable.Icon.createWithResource
import com.kana_tutor.notes.R

private lateinit var shortcutId: String

private fun postApi26CreateShortcut(c: Context, scClass: Class<*>) {
    if (Build.VERSION.SDK_INT >= 26) {
        val sm = c.getSystemService(ShortcutManager::class.java)
        if (sm != null && sm.isRequestPinShortcutSupported) {
            var shortcutExists = false
            // We create the shortcut multiple times if given the
            // opportunity.  If the shortcut exists, put up
            // a toast message and exit.
            val shortcuts = sm.pinnedShortcuts
            var i = 0
            while (i < shortcuts.size && !shortcutExists) {
                shortcutExists = shortcuts[i].id == shortcutId
                i++
            }
            if (shortcutExists) {
                Toast.makeText(
                    c, String.format(
                        "Shortcut %s already exists.", shortcutId
                    ), Toast.LENGTH_LONG
                ).show()
            } else {

                // this is the intent that actually creates the shortcut.
                val shortcutIntent = Intent(c, scClass)
                shortcutIntent.action = shortcutId
                // this intent is used to wake up the broadcast receiver.
                // I couldn't get createShortcutResultIntent to work but
                // just a simple intent as used for a normal broadcast
                // intent works fine.
                val broadcastIntent = Intent(shortcutId)
                broadcastIntent.putExtra("msg", "approve")

                val shortcutInfo = ShortcutInfo.Builder(
                    c,
                    shortcutId
                )
                    .setShortLabel(c.getString(R.string.app_name))
                    .setIcon(createWithResource(c, R.drawable.qmark))
                    .setIntent(shortcutIntent)
                    .build()
                val successCallback = PendingIntent.getBroadcast(
                    c, 99, broadcastIntent, 0
                )
                // Shortcut gets created here.
                sm.requestPinShortcut(shortcutInfo, successCallback.intentSender)
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun preApi26CreateShortcut(activity: Activity, scClass: Class<*>) {
    val shortcutIntent = Intent(activity, scClass)
    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    val addIntent = Intent()
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name))
    val icon = Intent.ShortcutIconResource.fromContext(
        activity,
        R.drawable.qmark
    )
    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
    addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
    addIntent.putExtra("duplicate", false)
    activity.sendBroadcast(addIntent)
    Toast.makeText(activity, "Created pre-api26 shortcut", Toast.LENGTH_LONG)
        .show()
}

private fun createShortcut(activity: Activity, scClass: Class<*>) {
    if (Build.VERSION.SDK_INT >= 26)
        postApi26CreateShortcut(activity, scClass)
    else
        preApi26CreateShortcut(activity, scClass)
}

fun promptForShortcut(activity: Activity, scClass: Class<*>) {
    Log.d("promptForShortcut", "Called")
    val buttonIds = intArrayOf(
        R.string.NO,
        R.string.YES
    )
    val buttonCallbacks: Array<() -> Unit> =
        arrayOf(
            {
                // Toast.makeText(activity, "No", Toast.LENGTH_LONG).show()
                Toast.makeText(activity, "declined", Toast.LENGTH_LONG).show()
            },
            {
                // Toast.makeText(activity, "Yes", Toast.LENGTH_LONG).show()
                createShortcut(activity, scClass)
            }
        )
    val promptMess = activity.getString(
        R.string.promptForShortcut, activity.getString(R.string.app_name)
    )
    shortcutId = activity.getString(R.string.app_name)
    yesNoDialog(
        activity,
        false,
        promptMess,
        buttonIds,
        buttonCallbacks
    )
}

