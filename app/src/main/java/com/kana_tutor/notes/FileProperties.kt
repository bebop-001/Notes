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
package com.kana_tutor.notes

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.system.Os
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

class FileProperties {
    var displayName = ""
    var uri :String = ""
    var size = -1
    var isWritable = false
    var internalWriteProtect = false
    private var lastModified = 0L
    private var lastModifiedDate = Date(0).toString()
    var isEmpty = true
    private var documentId = ""
    private var authority = ""
    var fileName = ""

    // extension functions.
    private fun Cursor.getKeyedString(key: String): String
            = getString(getColumnIndex(key))
    private fun Cursor.getKeyedInt(key: String): Int
            = getInt(getColumnIndex(key))
    private fun Cursor.getKeyedLong(key: String): Long
            = getLong(getColumnIndex(key))
    constructor(context : Context, uri : Uri) {
        val c = context.contentResolver.query(
            uri, null, null, null, null)
        c?.apply {
            moveToFirst()
            size = getKeyedInt(DocumentsContract.Document.COLUMN_SIZE)
            displayName = getKeyedString(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            lastModified = getKeyedLong(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            lastModifiedDate = Date(lastModified).toString()
            isWritable = (getKeyedInt(DocumentsContract.Document.COLUMN_FLAGS) and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0
            documentId = getKeyedString(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            authority = uri.authority.toString()
            isEmpty = false
        }
        c?.close()
        // based on code from https://stackoverflow.com/questions/30546441/
        // android-open-file-with-intent-chooser-from-uri-obtained-by-storage-access-frame
        val pf : ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
        if (pf != null) {
            val procFile = File("/proc/self/fd/" + pf.fd)
            fileName  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Os.readlink(procFile.toString())
            else
                procFile.canonicalPath
        }
        this.uri = uri.toString()
    }
    // return empty file properties.
    constructor()
    override fun toString(): String {
        return String.format(
            "%s:size=%d,isWritable:%b,lastModifiedDate:%s,id=\"%s\"uri=\"%s\", fileName=\"%s\""
            , displayName, size, isWritable, lastModifiedDate,documentId, uri, fileName
        )
    }
    fun formatedProperties(context : Context) : String {
        val writable = !isEmpty && isWritable && !internalWriteProtect
        return String.format(
            context.getString(R.string.file_properties_format)
            , ContextCompat.getColor(
                context, R.color.file_edit_window_font_color) and 0x00FFFFFF
            , displayName, size, lastModifiedDate, writable)
    }
}