package com.example.moviebox

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moviebox.databinding.ActivitySecondBinding


class SecondActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //animation
        binding.constraint.post {
            binding.constraint.alpha = 0f
            binding.constraint.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(null)
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isSecondActivityOpened = sharedPreferences.getBoolean("isSecondActivityOpened", false)
        if (isSecondActivityOpened) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            sharedPreferences.edit().putBoolean("isSecondActivityOpened", true).apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}

