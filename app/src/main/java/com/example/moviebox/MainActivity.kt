package com.example.moviebox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moviebox.databinding.MainActivityBinding

class MainActivity: AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, HomeFragment())
                .commit()
        }

       binding.bottomNavigation.setOnItemSelectedListener {
           val fragment = when (it.itemId) {
               R.id.home -> HomeFragment()
               R.id.search -> SearchFragment()
               R.id.saved -> SaveFragment()
               else -> null
           }
           fragment?.let {
               supportFragmentManager.beginTransaction()
                   .replace(R.id.container_fragment, it)
                   .commit()
           }
           true
       }






    }
}
