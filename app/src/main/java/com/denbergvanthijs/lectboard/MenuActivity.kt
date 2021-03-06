package com.denbergvanthijs.lectboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    companion object {
        var localIpAddress = "192.168.178.66"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val button: Button = findViewById(R.id.MainMenuButton)
        button.setOnClickListener {
            localIpAddress = (if (MainMenuIPAddress.text.toString() == "") {
                "192.168.178.66"
            } else MainMenuIPAddress.text.toString())
            val intent = Intent(this, DrawActivity::class.java)
            startActivity(intent)
        }
    }
}