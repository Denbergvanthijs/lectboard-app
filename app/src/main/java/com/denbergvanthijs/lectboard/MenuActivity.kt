package com.denbergvanthijs.lectboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val button: Button = findViewById(R.id.button_start_drawing)
        button.setOnClickListener {
            val intent = Intent(this, DrawActivity::class.java)
            intent.putExtra("IP", MainMenuIPAdress.text.toString())
            startActivity(intent)
        }
    }
}