package com.example.opsc7312_budgetbuddy.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.opsc7312_budgetbuddy.R
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var pb: ProgressBar
    private var counter : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        progressBar()
    }

    //Simple progress bar to simulate 10 seconds loading
    private fun progressBar(){
        pb = findViewById(R.id.progressBar)

        val t = Timer()
        val tt = object : TimerTask() {
            override fun run() {
                counter++

                pb.progress = counter

                if(counter == 100){
                    t.cancel()

                    //Redirects to the Login Activity after progress counter reaches 100
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        t.schedule(tt, 0, 10)

    }
}