package com.example.zukkey.arcoresampleforprimer.kotlin

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.zukkey.arcoresampleforprimer.R
import com.example.zukkey.arcoresampleforprimer.databinding.ActivityWebBinding


class WebActivity: AppCompatActivity() {

  private var binding: ActivityWebBinding? = null

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, WebActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_web)
    binding?.webView?.loadUrl("https://booth.pm/ja/items/830454")
  }
}
