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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kana_tutor.notes.kanautils.*
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader


private const val CREATE_REQUEST_CODE   = 40
private const val OPEN_REQUEST_CODE = CREATE_REQUEST_CODE  + 1
private const val SAVE_AS_REQUEST_CODE = OPEN_REQUEST_CODE + 1

class EditWindow : Fragment(), FontSizeChangedListener {
    private var stringUri: String? = null
    companion object {
        /*
         * Use this factory method to create a new instance of
         * this fragment.
         */
        @JvmStatic
        fun newInstance(stringUri: String) =
            EditWindow().apply {
                arguments = Bundle().apply {
                    putString("stringUri", stringUri)
                }
            }
        private var currentFileProperties = FileProperties()
        private var editWindowTextChanges = 0
        private var fontDefaultSize = 0.0f
    }
    private var _currentFileTitle = ""
    val currentFileTitle : String
        get() {
            _currentFileTitle = if (currentFileProperties.displayName == "")
                ""
            else {
                val changed = if (editWindowTextChanges > 0) "✔️" else " "
                val writeProtected =
                    if (currentFileProperties.internalWriteProtect) "🔒" else "\uD83D\uDD13"
                changed + writeProtected + currentFileProperties.displayName
            }
            return _currentFileTitle
        }


    interface EditWinEventListener {
        fun titleChanged (title:String)
    }
    var titleListener : EditWinEventListener? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            titleListener = activity as  EditWinEventListener
            titleListener!!.titleChanged(currentFileTitle)
        }
        catch (e:ClassCastException) {
            throw ClassCastException(activity.toString()
                    + ": must implement EditWinEventListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        titleListener = null
    }
    private fun setTitleBar(title : String) {
        titleListener?.apply {
            titleChanged(title)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stringUri = it.getString("stringUri")
        }
    }


    private lateinit var editWindowTV: TextView
    private lateinit var scrollView: ScrollView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.edit_window, container, false)
        editWindowTV = view.findViewById(R.id.edit_window_tv)
        scrollView = view.findViewById(R.id.edit_window_scrollview)
        val prefs = context!!
            .getSharedPreferences(editWinPrefsName, Context.MODE_PRIVATE)
        writeProtectedFiles  =
            prefs.getStringSet("writeProtected", HashSet<String>())
        fontDefaultSize = prefs.getFloat("fontDefaultSize", -1.0f)
        if (fontDefaultSize < 0) {
            fontDefaultSize = editWindowTV.textSize
            prefs.edit().putFloat("fontDefaultSize", fontDefaultSize).apply()
        }

        editWindowTV.addOnLayoutChangeListener(
            fun (v: View,
                left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                Log.d("onLayoutChanged:",
                    String.format("left%d, top:%d, right:%d, bottom:%d",
                        left, top, right, bottom))
                Log.d("onLayoutChanged:OLD",
                    String.format("left%d, top:%d, right:%d, bottom:%d",
                        oldLeft, oldTop, oldRight, oldBottom))
            }
        )
        editWindowTV.addTextChangedListener(object : TextWatcher {
            /*
            fun CharSequence.shortChangeMess(start:Int, count:Int) : String {
                var rv = subSequence(start, start + count)
                if (count > 6) {
                    rv = subSequence(start, start + 3).toString() +
                            "…" +
                            subSequence(start + count - 3, start + count).toString()
                }
                return rv.toString()
            } */
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int) {
                /*
                Log.d(
                    "TextListener", String.format(
                        "before: start %d, count %d, after %d: \"%s\":len %d"
                        ,
                        start, count, after, s.shortChangeMess(start, count), s.length
                    )
                )
                 */
            }
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int) {
                /*
                Log.d("TextListener", String.format(
                    "onChange: start %d, count %d, before %d : \"%s\":len %d"
                    , start, count, before, s.shortChangeMess(start, count), s.length)
                )
                 */
                // count > 0, chars added.  before > 0 chars removed.
                if (count > 0 || before > 0) {
                    editWindowTextChanges++
                    titleListener?.titleChanged(currentFileTitle)

                }
            }
            override fun afterTextChanged(s: Editable) {
                // Log.d("TextListener", "afterTextChanged")
            }
        })
        // Things work ok with listener commented out.  for demo only.
        editWindowTV.setOnTouchListener { v, event ->
            Log.d("OnTouch:", String.format("event=%s", event.toString()))
            // imm.showSoftInput(v, InputMethodManager.SHOW_FORCED)

            false // set false indicating listener handled event.
        }
        // for demo
        editWindowTV.setOnFocusChangeListener { v, hasFocus ->
            Log.d("OnFocusChangeListener"
                , String.format("hasFocus:%s", hasFocus.toString()))
        }

        setHasOptionsMenu(true)
        return view
    }
    private fun saveToUri(uri : Uri, whoResId : Int) {
        if (writeProtectedFiles!!.contains(uri.path)) {
            val p = FileProperties(context!!, uri)
            kToast(context!!, getString(R.string.is_write_protected, p.displayName))
        }
        else {
            val pfd = context!!
                .contentResolver
                .openFileDescriptor(uri, "w")
            if (pfd != null) {
                try {
                    val fileOutputStream = FileOutputStream(
                        pfd.fileDescriptor
                    )
                    val textContent = editWindowTV.text.toString()
                    fileOutputStream.write(textContent.toByteArray())
                    fileOutputStream.close()
                    editWindowTextChanges = 0
                    currentFileProperties = FileProperties(context!!, uri)
                    titleListener?.titleChanged(currentFileTitle)
                    Log.d("saveToUri:", currentFileProperties.toString())
                    kToast(
                        this.context!!, getString(
                            R.string.read_write_toast_fmt, getString(whoResId)
                            , getString(R.string.wrote), currentFileTitle
                            , currentFileProperties.size
                        )
                    )
                } catch (e: IOException) {
                    throw RuntimeException(
                        "Open for write failed:" + e.message + e.stackTrace
                    )
                }
            }
            else
                throw RuntimeException(
                    this.context!!.getString(R.string.save_uri_failed, uri.toString())
                )
        }
    }
    private fun newFile(uri: Uri) {
        // unless there is a file open, clear the edit text view.
        if (! currentFileProperties.isEmpty) editWindowTV.text = ""
        if (writeProtectedFiles!!.contains(uri.path)) {
            val p = FileProperties(context!!, uri)
            kToast(context!!, getString(R.string.is_write_protected, p.displayName))
        }
        else {
            saveToUri(uri, R.string.new_file)
            currentFileProperties = FileProperties(context!!, uri)
        }

        Log.d("newFileContents:", currentFileProperties.toString())
    }
    private fun openFile(uri:Uri) {
        try {
            val inputStream = context!!.contentResolver.openInputStream(uri)!!
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()

            var currentLine = reader.readLine()

            while (currentLine != null) {
                stringBuilder.append(currentLine + "\n")
                currentLine = reader.readLine()
            }
            inputStream.close()

            // set the font size.  Use the default font size if user hasn't previously
            // selected a font size.  If user selected a fontSize for the current file,
            // it was saved to the user preferences using the uri path + ".fontSize" as a key.
            editWindowTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context!!
                    .getSharedPreferences(editWinPrefsName, Context.MODE_PRIVATE)
                    .getFloat(uri.path + ".fontSize", fontDefaultSize))

            editWindowTV.text = stringBuilder.toString()

            currentFileProperties = FileProperties(context!!, uri)
            editWindowTextChanges = 0
            currentFileProperties.internalWriteProtect =
                writeProtectedFiles!!.contains(currentFileProperties.uriPath)
            kToast(
                this.context!!, getString(
                    R.string.read_write_toast_fmt, getString(R.string.open_file)
                    , getString(R.string.read)
                    , currentFileTitle, currentFileProperties.size
                )
            )

            Log.d("openFile:", currentFileProperties.toString())
        } catch (e: IOException) {
            throw RuntimeException(
                "Open for read failed:" + e.message + e.stackTrace)
        }
    }
    private fun getSaveFile() {
        val currentUri = Uri.parse(currentFileProperties.uri)
        saveToUri(currentUri, R.string.save_file)
    }
    private fun saveAs(uri : Uri) = saveToUri(uri, R.string.save_as_file)

    private fun displayFileProperties() {
        val webView = WebView(activity)
        webView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.file_edit_window_bg))
        webView.loadData(
                currentFileProperties.formatedProperties(context!!)
            , "text/html", "utf-8"
        )
        androidx.appcompat.app.AlertDialog.Builder(context!!)
            .setView(webView)
            .show()
    }
    // If contents of edit window have been altered and user is about to overwrite,
    // give them an opportunity to abort the open/new file.
    private fun overwriteFileChanges(intent:Intent, requestCode:Int) {
        if (editWindowTextChanges > 0) {
            val buttonIds = intArrayOf(R.string.NO, R.string.YES)
            val buttonCallbacks: Array<() -> Unit> =
                arrayOf(
                    { Toast.makeText(
                        context!!, getString(R.string.overwrite_aborted), Toast.LENGTH_SHORT)
                        .show()
                    },
                    { startActivityForResult(intent, requestCode) }
                )
            val promptMess = context!!.getString(
                R.string.discard_file_changes, currentFileProperties.displayName
            )
            yesNoDialog(
                context!! ,false, promptMess,
                buttonIds, buttonCallbacks
            )
        }
        else
            startActivityForResult(intent, requestCode)
    }
    private fun getNewFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "")

        overwriteFileChanges(intent, CREATE_REQUEST_CODE)
    }
    private fun getOpenFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        overwriteFileChanges(intent, OPEN_REQUEST_CODE)
    }
    private fun getSaveFileAs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        startActivityForResult(intent, SAVE_AS_REQUEST_CODE)
    }
    // name of shared prefs file for this fragment
    private val editWinPrefsName = "EditWindowPrefs"
    private var writeProtectedFiles : MutableSet<String> ? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                         resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CREATE_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        newFile(uri)
                        setTitleBar(currentFileProperties.displayName)
                    }
                }
                SAVE_AS_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        saveAs(uri)
                        setTitleBar(currentFileProperties.displayName)
                    }
                }
                OPEN_REQUEST_CODE -> {
                    if (resultData != null && resultData.data != null) {
                        val uri = resultData.data!!
                        openFile(uri)
                        setTitleBar(currentFileProperties.displayName)
                    }
                }
                else -> throw RuntimeException(String.format("onActivityResult" +
                        "Unexpected request code: 0x%08x", requestCode)
                )
            }
        }
    }
    private fun writeProtectFile() {
        val prefs = context!!
            .getSharedPreferences(editWinPrefsName, Context.MODE_PRIVATE)
        writeProtectedFiles  =
            prefs.getStringSet("writeProtected", HashSet<String>())
        val fp = currentFileProperties
        writeProtectedFiles!!.add(currentFileProperties.uriPath)
        fp.internalWriteProtect = !fp.internalWriteProtect
        if (! fp.internalWriteProtect)
            writeProtectedFiles!!.remove(currentFileProperties.uriPath)
        titleListener?.titleChanged(currentFileTitle)
        prefs.edit()
            .putStringSet("writeProtected", writeProtectedFiles)
            .apply()
    }

    // callback for FontSizeChangedListener.  If user selected a fontSize
    // for the current file save it to user preferences using the
    // uri path + ".fontSize" as a key.
    override fun fontSizeChanged(newSize: Float) {
        editWindowTV.invalidate()
        context!!
            .getSharedPreferences(editWinPrefsName, Context.MODE_PRIVATE)
            .edit()
            .putFloat(currentFileProperties.uriPath + ".fontSize", newSize)
            .apply()
        editWindowTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
    }

    // Menu item selected listener.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var rv = true
        Log.d("EditWindow:", "onOptionsItemSelected called")
        when (item.itemId) {
            R.id.save_file_item -> getSaveFile()
            R.id.save_as_file_item -> getSaveFileAs()
            R.id.open_file_item -> getOpenFile()
            R.id.new_file_item -> getNewFile()
            R.id.write_protect_file_item -> writeProtectFile()
            R.id.file_properties_item -> displayFileProperties()
            R.id.select_font_size -> selectFontSize(this.activity as Activity, this)
            else -> rv = super.onOptionsItemSelected(item)
        }
        Log.d("EditWindow:", "onOptionsItemSelected done")
        return rv
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.d("EditWindow:", "onPrepareOptionsMenu called")
        menu.apply {
            val fp = currentFileProperties
            fp.apply {
                val readOnly =
                    writeProtectedFiles!!.contains(currentFileProperties.uriPath)
                    || currentFileProperties.displayName == ""
                // disable save unless we have a file.
                findItem(R.id.save_file_item).isEnabled = !readOnly
                val writeProtectItem = findItem(R.id.write_protect_file_item)!!
                writeProtectItem.isEnabled = !isEmpty
                writeProtectItem.title = getString(
                    if (readOnly) R.string.is_read_only
                    else R.string.is_writable
                )
                findItem(R.id.select_font_size).isEnabled =
                    ! currentFileProperties.isEmpty
            }
        }
        Log.d("EditWindow:", "onPrepareOptionsMenu done")
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d("EditWindow:", "onCreateOptionsMenu called")
        inflater.inflate(R.menu.edit_win_menu, menu)
    }
}
