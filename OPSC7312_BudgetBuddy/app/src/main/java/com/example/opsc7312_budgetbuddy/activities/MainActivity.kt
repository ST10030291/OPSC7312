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

/*Code Attribution:
1.
This code was taken from a YouTube Video
Upload by: Fox Android
Titled: RecyclerView using Kotlin 2021 || Android RecyclerView in Kotlin || Kotlin RecyclerView Example
Available at: https://youtu.be/UbP8E6I91NA?si=h8eoep-KrGFlteNe
Accessed: 07 September 2024

2.
This code was taken from a YouTube Video
Upload by: Android Knowledge
Titled: RecyclerView in Android Studio using Kotlin | Source Code | 2024
Available at: https://youtu.be/IYhmpUmeGOQ?si=x7r4X4gGaezJtrgf
Accessed: 10 September 2024

3.
This code was taken from a YouTube Video
Upload by: EDMT Dev
Titled: Android Development Tutorial - Swipe to show button Recycler View
Available at: https://youtu.be/Cam2AvfJilY?si=M_MBB1K0WDt8EGU4
Accessed: 05 September 2024

4.
This code was taken from a YouTube Video
Upload by: CodingSTUFF
Titled: RecyclerView With Item Click Listener in Kotlin : ( Android Tutorial 2022 )
Available at: https://youtu.be/WqrpcWXBz14?si=M__gSJpM5G1RHZmT
Accessed: 10 September 2024

5.
Attribution: "Fragments in Android - A Guide for Beginners"
Link: https://developer.android.com/guide/fragments

6.
Attribution: "ViewBinding with Fragments - Medium Article"
Link: https://medium.com/some-view-binding-article

7.
Attribution: "Mastering Android Fragments with Kotlin - Ray Wenderlich"
Link: https://www.raywenderlich.com/kotlin-fragment-tutorial

8.
Attribution: "RecyclerView inside Fragments - Udacity Android Developer Course"
Link: https://www.udacity.com/course/android-developer-nanodegree--nd801

 */