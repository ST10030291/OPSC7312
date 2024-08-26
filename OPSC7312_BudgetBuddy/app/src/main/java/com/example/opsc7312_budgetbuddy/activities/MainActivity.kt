package com.example.opsc7312_budgetbuddy.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    private fun progressBar(){
        pb = findViewById(R.id.progressBar)

        val t = Timer()
        val tt = object : TimerTask() {
            override fun run() {
                counter++

                pb.progress = counter

                if(counter == 100){
                    t.cancel()

                    val intent = Intent(this@MainActivity, Dashboard::class.java)
                    startActivity(intent)
                }
            }
        }

        t.schedule(tt, 0, 10)

    }
}