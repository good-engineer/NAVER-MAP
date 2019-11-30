package com.naver.navermap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_search, container, false)

        var destination = ""
        val editText = rootview.findViewById(R.id.search) as EditText
        editText.setOnEditorActionListener { _, keyCode, _ ->
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                // use editText.text.toString()
                Toast.makeText(
                    context, editText.text.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }


        return rootview
    }
}