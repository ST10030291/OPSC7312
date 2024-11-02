package com.codebenders.opsc7312_budgetbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.codebenders.opsc7312_budgetbuddy.R
import com.codebenders.opsc7312_budgetbuddy.databinding.ActivityNetworkErrorBinding

class NetworkErrorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNetworkErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNetworkErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Glide.with(this).load(R.drawable.no_network).into(binding.networkErrorGif)

        binding.switchToOfflineButton.setOnClickListener {
            val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("offlineMode", true)
            editor.apply()

            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }
    }
}