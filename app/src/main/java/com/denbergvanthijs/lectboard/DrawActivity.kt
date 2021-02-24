package com.denbergvanthijs.lectboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myCanvasView = MyCanvasView(this)
        // No XML file; just one custom view created programmatically.
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        setContentView(myCanvasView)
    }
}
