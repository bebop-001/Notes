@file:Suppress("unused", "unused", "unused")

package com.kana_tutor.notes

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast


@Suppress("unused")
private const val TAG = "DialogUtils"

// Suppress is for Activity which actually is necessary.
@Suppress("unused")
fun Activity.dpToPix(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}
@Suppress("unused")
fun Activity.spToPix(sp: Int): Int {
    return (sp * Resources.getSystem().displayMetrics.scaledDensity).toInt()
}

fun View.setPadding(padding: Int) {
    setPadding(padding,padding,padding,padding)
}

fun Activity.monospaceDialog(
    title: String, mess: String, fontSize:Int = 12
) : Dialog {
    val tv = TextView(this)
    tv.typeface = Typeface.MONOSPACE
    tv.setPadding(this.dpToPix(10))
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    tv.setTextColor(Color.parseColor(
        if (MainActivity.isNightMode) "#FFFFFF" else "#000000"
    ))
    tv.text = mess
    val dialog = android.app.AlertDialog.Builder(this)
        .setTitle(title)
        .setView(tv)
        .create()
    dialog.show()
    return dialog
}

// Hide the keyboard when the fragment starts up.
@SuppressLint("ServiceCast")
fun Activity.hideKeyboard() {
    val im = getSystemService(Activity.INPUT_METHOD_SERVICE)
            as InputMethodManager
    val windowToken = this.currentFocus?.windowToken
    if (windowToken != null)
        im.hideSoftInputFromWindow(windowToken, 0)
}
fun Activity.hideKeyboard(tv:TextView): TextView {
    val im = getSystemService(Activity.INPUT_METHOD_SERVICE)
            as InputMethodManager

    im.hideSoftInputFromWindow(tv.windowToken, 0)
    tv.clearFocus()
    return tv
}
// to be called after Activity.hideKeyboard(tv:TextView): TextView.
fun TextView.clearTextView() { text = "" }

fun Context.toastMess(subject:String, maxMessLen:Int = 20) {
    val mess =
        if (subject.length < maxMessLen) subject
        else "${subject.substring(0..maxMessLen)}…"
    Toast.makeText(this, mess, Toast.LENGTH_SHORT)
        .show()
}
