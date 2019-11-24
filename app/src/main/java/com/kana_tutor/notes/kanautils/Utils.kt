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

package com.kana_tutor.notes.kanautils

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.kana_tutor.notes.BuildConfig
import com.kana_tutor.notes.R
import java.io.File
import java.text.SimpleDateFormat

// Return a spanned html string using the appropriate call for
// the user's device.
fun htmlString(htmlString:String) : Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
    else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlString)
    }
}

/**
 * Method takes an app context, a boolean saying if the string in is
 * html and an int array of 1 or 2 buttons and a button listener.
 * If only one, the dialog displays an 'OK' button.  If 2 buttons, one
 * is assumed to be the POSITIVE/yes' and the other is 'no'.  The callback
 * can be null in which case the dialog just exits after the button press.
 *
 * 1/9/2016
 * Tried every way I could think of to make this window transparent and
 * none worked.  Could probably make a custom dialog but probably not
 * worth the effort.  Sigh....
 * d.getWindow().setBackgroundDrawableResource(R.color.transparent_black);
 * failed to do anything.
 * @param a                     application context
 * @param isHtmlQuery           boolean true if query string contains HTML
 * @param queryString           query string to be displayed
 * @param buttonStringResId     array of 1 or 2 int resource ids for string
 * to display on button
 * @param onClick               array of button onclick listeners for dialog
 */
fun yesNoDialog(
    a: Activity,
    isHtmlQuery: Boolean,
    queryString: String,
    buttonStringResId: IntArray,
    onClick: Array<()->Unit>
) {
    if (buttonStringResId.size != onClick.size)
        throw RuntimeException(
            String.format(
                "%s:yesNoDialog: button id and callback counts differ:" + "%d vs %d",
                "yesNoDialog",
                buttonStringResId.size,
                onClick.size
            )
        )
    Log.d("promptForShortcut", "Start")
    Log.d("promptForShortcut", "entered")
    val query = TextView(a)
    if (isHtmlQuery) {
        val html = htmlString(queryString)
        query.text = html
        query.movementMethod = LinkMovementMethod.getInstance()
    } else
        query.text = queryString

    query.setTextColor(Color.WHITE)
    query.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.toFloat())
    val b = AlertDialog.Builder(
        a,
        R.style.rounded_corner_dialog
    )
        .setIcon(R.mipmap.notes_launcher)
        .setView(query)
    if (buttonStringResId.size < 1 || buttonStringResId.size > 2)
        throw RuntimeException(
            String.format(
                "yesNoDialog: expected 1 or 2 buttons.  Received %d", buttonStringResId.size
            )
        )

    b.setPositiveButton(buttonStringResId[0]) { _,_ -> onClick[0]() }
    if (buttonStringResId.size == 2)
        b.setNegativeButton(buttonStringResId[1]) { _,_ -> onClick[1]() }
    b.create()
    b.show()
}

fun Toast._setGravity(gravity: Int) : Toast {
    this.setGravity(gravity, 0, 0)
    return this
}
fun kToast(activity : Activity, message: String) {
    val toast = Toast.makeText(activity, message, Toast.LENGTH_LONG)
        ._setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL)
        .show()
}
// Display info about the build using an AlertDialog.
fun displayBuildInfo(activity : Activity) : Boolean {
    val appInfo = activity.packageManager
        .getApplicationInfo(BuildConfig.APPLICATION_ID, 0)
    val installTimestamp = File(appInfo.sourceDir).lastModified()

    // use html to format our output 'about' message.
    val htmlString = String.format(activity.getString(R.string.build_info_query)
        , activity.getString(R.string.app_name)
        , BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME
        , SimpleDateFormat.getInstance().format(
            java.util.Date(BuildConfig.BUILD_TIMESTAMP))
        , SimpleDateFormat.getInstance().format(
            java.util.Date(installTimestamp))
        , if(BuildConfig.DEBUG) "debug" else "release"
        , BuildConfig.BRANCH_NAME
    )

    // use a text-view in the alert so we can display an html
    // formatted string.
    val aboutTv = TextView(activity)
    aboutTv.apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
        setTypeface(null, Typeface.BOLD)
        text = htmlString(htmlString)
        gravity = Gravity.CENTER
    }

    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(aboutTv)
        .show()
    return true
}

