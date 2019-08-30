package com.example.zukkey.arcoresampleforprimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.zukkey.arcoresampleforprimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private val binding by lazy {
    DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.pager.adapter = MainFragmentPagerAdapter(supportFragmentManager)
  }
}
