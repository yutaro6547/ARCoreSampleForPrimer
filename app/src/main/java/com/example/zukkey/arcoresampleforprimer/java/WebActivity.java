package com.example.zukkey.arcoresampleforprimer.java;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zukkey.arcoresampleforprimer.R;
import com.example.zukkey.arcoresampleforprimer.databinding.ActivityWebBinding;


public class WebActivity extends AppCompatActivity {

  private ActivityWebBinding binding;

  public static Intent createIntent(Context context) {
    return new Intent(context, WebActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_web);
    binding.webView.loadUrl("https://booth.pm/ja/items/830454");
  }
}
