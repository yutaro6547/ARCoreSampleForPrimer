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
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.zukkey.arcoresampleforprimer.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


public class ModelActivity extends AppCompatActivity {
  private static final int CAMERA_PERMISSION_CODE = 0;
  private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
  private ModelRenderable modelRenderable;

  public static Intent createIntent(Context context) {
    return new Intent(context, ModelActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_model);

    ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ModelRenderable.builder()
          .setSource(this, R.raw.rin)
          .build()
          .thenAccept(renderable -> modelRenderable = renderable)
          .exceptionally(
              throwable -> {
                Toast toast =
                    Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return null;
              });
    }

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (modelRenderable == null) {
            return;
          }

          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
          model.setParent(anchorNode);
          model.setRenderable(modelRenderable);
          model.getRotationController();
          model.getScaleController();
          model.getTranslationController();
          model.select();
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
