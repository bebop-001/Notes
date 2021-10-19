package com.kana_tutor.notes

import android.app.Application
import android.util.Log
import java.io.File
import java.lang.RuntimeException

lateinit var HOME : File
lateinit var TMP: File

@Suppress("unused")
private const val TAG = "Notes"
@Suppress("unused")
class Notes : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!::HOME.isInitialized) {
            HOME = File(filesDir, "../$TAG")
            if (!HOME.exists() && !HOME.mkdir()) {
                throw RuntimeException("$TAG: Failed to create directory $HOME")
            }
        }
        if (!::TMP.isInitialized) {
            TMP = File(filesDir, "../tmp")
            if (!TMP.exists() && !TMP.mkdir()) {
                throw RuntimeException("$TAG: Failed to create directory $TMP")
            }
        }
        val oneWeekAgo = System.currentTimeMillis() -
                7 * 24 * 60 * 60 * 1000
        // files in the TMP dir are removed after one week.
        val rmFiles  = TMP.list().map{File(it!!)}
            .filter{it.isFile && it.lastModified() < oneWeekAgo }
        if (rmFiles.isNotEmpty()) {
            Log.d(TAG, "Removing old TMP files:${
                rmFiles.map{it.toString()}.joinToString { ", " }
            }")
            rmFiles.map{it.delete() ||
                throw RuntimeException("$TAG: Failed to delete old tmp file $it")
            }
        }
    }
}