package com.denbergvanthijs.lectboard

import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity

/**
 * The main activity
 */
class DrawActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myCanvasView = MyCanvasView(this)
        // No XML file; just one custom view created programmatically.
        // Request the full available screen for layout.
        myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        setContentView(myCanvasView)
    }
}
