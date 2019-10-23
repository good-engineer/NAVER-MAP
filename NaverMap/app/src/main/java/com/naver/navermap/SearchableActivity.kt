package com.naver.navermap

import android.app.ListActivity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView



class SearchableActivity : ListActivity() {

    public override fun onCreate(savedInstanceState:Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.ativity_searchable)
        handleIntent(intent)
}

    override fun onNewIntent(intent:Intent) {
        setIntent(intent)
        handleIntent(intent)
}

    private fun handleIntent(intent:Intent) {
       if (Intent.ACTION_SEARCH == intent.action) {
           val query = intent.getStringExtra(SearchManager.QUERY)
           doMySearch(query!!)
       }
}

    private fun doMySearch(query:String) {
        //get place information
    }

}


