package com.kana_tutor.notes.kanautils

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.widget.SeekBar
import android.widget.TextView
import com.kana_tutor.notes.R


interface FontSizeChangedListener {
    fun fontSizeChanged(newSize:Float)
}

fun selectFontSize(activity : Activity, listener : FontSizeChangedListener) : Float {

    val selectView = activity.layoutInflater.inflate(R.layout.font_size_select, null)
    val sampleView = selectView.findViewById<TextView>(R.id.size_sample_view)

    val slider = selectView.findViewById<SeekBar>(R.id.font_size_slider)

    val b = AlertDialog.Builder(
        activity,
        R.style.rounded_corner_dialog
    )
        .setTitle(R.string.select_font_size)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.select, null)
        .setView(selectView)
        .show()

    var defaultFontSize = sampleView.getTextSize()
    slider.progress = 50
    slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val was = sampleView.textSize
            sampleView.setTextSize(COMPLEX_UNIT_PX, (progress / 50.0f) * defaultFontSize)

            Log.d("SeekBar:changed", String.format("%f:%f:%f:%d",
                was, sampleView.textSize, (progress / 50.0f), progress))

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            Log.d("SeekBar:stop", seekBar!!.progress.toString())
        }
    })


    return defaultFontSize
}