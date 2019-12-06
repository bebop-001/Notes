package com.kana_tutor.notes.kanautils
/*
 * Copyright 2019 Steven Smith kana-tutor.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kana_tutor.notes.R

// used for communication between method and invoker.
interface FontSizeChangedListener {
    fun fontSizeChanged(newSize:Float)
}

// dialog used to select font size for textview.
@SuppressLint("InflateParams")
fun selectFontSize(activity : Activity, listener : FontSizeChangedListener) {
    val selectView = activity.layoutInflater.inflate(R.layout.font_size_select, null)

    val sampleView = selectView.findViewById<TextView>(R.id.size_sample_view)
    sampleView.setBackgroundColor(ContextCompat.getColor(activity, R.color.file_edit_window_bg))
    sampleView.setTextColor(ContextCompat.getColor(activity, R.color.file_edit_window_font_color))

    val slider = selectView.findViewById<SeekBar>(R.id.font_size_slider)
    slider.setBackgroundColor(ContextCompat.getColor(activity, R.color.file_edit_window_bg))

    AlertDialog.Builder(
            activity,
            R.style.rounded_corner_dialog
        )
        .setTitle(R.string.select_font_size)
        // positive is the left button which I associate with cancel
        .setPositiveButton(R.string.cancel, null)
        .setNegativeButton(R.string.select) {
                dialog,which ->
            listener.fontSizeChanged(sampleView.textSize)
            dialog.dismiss()
        }
        .setView(selectView)
        .show()

    val defaultFontSize = sampleView.textSize
    slider.progress = 50
    slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        // update text size on slider change.  Note that 50 is default font size.
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            sampleView.setTextSize(COMPLEX_UNIT_PX, (progress / 50.0f) * defaultFontSize)
            /*
            Log.d("SeekBar:changed", String.format("%f:%f:%f:%d",
                was, sampleView.textSize, (progress / 50.0f), progress))
             */

        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //To change body of created functions use File | Settings | File Templates.
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // Log.d("SeekBar:stop", seekBar!!.progress.toString())
        }
    })
}