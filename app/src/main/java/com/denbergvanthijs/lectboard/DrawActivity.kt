package com.denbergvanthijs.lectboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_draw.*

class DrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_draw)
        val myCanvasView = MyCanvasView(this)
        content.addView(myCanvasView)

        val colorRed = ResourcesCompat.getColor(resources, R.color.colorMarkerRed, null)
        val colorGreen = ResourcesCompat.getColor(resources, R.color.colorMarkerGreen, null)
        val colorBlue = ResourcesCompat.getColor(resources, R.color.colorMarkerBlue, null)
        val colorBlack = ResourcesCompat.getColor(resources, R.color.colorBlack, null)

        buttonRed.setOnClickListener { myCanvasView.paint.color = colorRed }
        buttonGreen.setOnClickListener { myCanvasView.paint.color = colorGreen }
        buttonBlue.setOnClickListener { myCanvasView.paint.color = colorBlue }
        buttonBlack.setOnClickListener { myCanvasView.paint.color = colorBlack }
        buttonSmaller.setOnClickListener { myCanvasView.paint.strokeWidth -= 1 }
        buttonBigger.setOnClickListener { myCanvasView.paint.strokeWidth += 1 }

    }
}
