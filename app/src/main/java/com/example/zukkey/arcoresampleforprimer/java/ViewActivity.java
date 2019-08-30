package com.example.zukkey.arcoresampleforprimer.java;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
import com.example.zukkey.arcoresampleforprimer.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ViewActivity extends AppCompatActivity {
  private static final int CAMERA_PERMISSION_CODE = 0;
  private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
  private ViewRenderable imageViewRenderable;
  private ViewRenderable textViewRenderable;
  Integer count = 0;

  public static Intent createIntent(Context context) {
    return new Intent(context, ViewActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ArCoreApk.Availability availability =
        ArCoreApk.getInstance().checkAvailability(this);
    if (availability.isSupported()) {
      Toast.makeText(this, "AR機能が利用できます", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, "AR機能を利用することができません", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    setContentView(R.layout.activity_view);
    ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ViewRenderable.builder()
          .setView(this, R.layout.item_image)
          .build()
          .thenAccept(renderable -> imageViewRenderable = renderable);


      ViewRenderable.builder()
          .setView(this, R.layout.item_text)
          .build()
          .thenAccept(renderable -> textViewRenderable = renderable);
    }

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (imageViewRenderable == null || textViewRenderable == null) {
            return;
          }

          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          if (count == 0) {
            TransformableNode text = new TransformableNode(arFragment.getTransformationSystem());
            text.setParent(anchorNode);
            text.setRenderable(textViewRenderable);
            text.getRotationController();
            text.getScaleController();
            text.select();
            Button btn = (Button) textViewRenderable.getView();
            btn.setOnClickListener(view -> {
              Toast.makeText(this, "ボタンをタップしました！詳細へ遷移します", Toast.LENGTH_SHORT).show();
              startActivity(WebActivity.createIntent(this));
              finish();
            });
          } else {
            TransformableNode image = new TransformableNode(arFragment.getTransformationSystem());
            image.setParent(anchorNode);
            image.setRenderable(imageViewRenderable);
            image.getRotationController();
            image.getScaleController();
            image.getTranslationController();
            image.select();
          }
          count += 1;
        });
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[] {CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
    }
  }
}

