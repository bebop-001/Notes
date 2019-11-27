package com.kana_tutor.notes

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [EditWindow.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EditWindow.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditWindow : Fragment() {
    private var stringUri: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stringUri = it.getString("stringUri")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.edit_window, container, false)
        val editWindow = view.findViewById<TextView>(R.id.edit_window_tv)
        editWindow.text = stringUri

        editWindow.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d("TextListener", "beforeTextChanged")
            }
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("TextListener", "onTextChanged")
            }
            override fun afterTextChanged(s: Editable) {
                Log.d("TextListener", "afterTextChanged")
            }
        })
        // Things work ok with listener commented out.  for demo only.
        editWindow.setOnTouchListener { v, event ->
            Log.d("OnTouch:", String.format("event=%s", event.toString()))
            // imm.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            false // set false indicating listener handeled event.
        }
        // for demo
        editWindow.setOnFocusChangeListener { v, hasFocus ->
            Log.d("OnFocusChangeListener"
                , String.format("hasFocus:%s", hasFocus.toString()))
        }

        return view
    }

    /*
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

     */

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

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
    }
}
