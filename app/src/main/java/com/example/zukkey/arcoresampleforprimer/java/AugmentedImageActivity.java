package com.example.zukkey.arcoresampleforprimer.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;
import com.example.zukkey.arcoresampleforprimer.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import timber.log.Timber;


public class AugmentedImageActivity extends AppCompatActivity {
  private ViewRenderable textViewRenderable;
  private Session session;
  private Boolean sessionConfigured = false;
  private ArSceneView arSceneView;
  private Boolean isAttachedModel = false;

  public static Intent createIntent(Context context) {
    return new Intent(context, AugmentedImageActivity.class);
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
    setContentView(R.layout.activity_augumented_image);
    ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

    if (arFragment != null) {
      // デフォルトだとPlaneの検出が始まるが、Planeを検出しない
      arFragment.getPlaneDiscoveryController().hide();
      arFragment.getPlaneDiscoveryController().setInstructionView(null);
      arSceneView = arFragment.getArSceneView();

      arFragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
        @Override
        public void onUpdate(FrameTime frameTime) {
          Frame frame = arFragment.getArSceneView().getArFrame();
          Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

          for (AugmentedImage img : updatedAugmentedImages) {
            if (img.getTrackingState() == TrackingState.TRACKING) {
              if (img.getName().contains("eure") && !isAttachedModel) {
                setUp3DModel(img.createAnchor(img.getCenterPose()), arFragment);
              }
            }
          }
        }
      });
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (session == null) {
      try {
        session = new Session(this);
      } catch (UnavailableDeviceNotCompatibleException | UnavailableArcoreNotInstalledException | UnavailableApkTooOldException | UnavailableSdkTooOldException e) {
        Log.e("Session Error", e.getMessage());
      }
      sessionConfigured = true;
    }

    if (sessionConfigured) {
      configureSession();
      sessionConfigured = false;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  private void setUp3DModel(Anchor anchor, ArFragment arFragment) {
    ViewRenderable.builder()
        .setView(this, R.layout.item_text)
        .build()
        .thenAccept(renderable -> textViewRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

    if (textViewRenderable == null) {
      return;
    }

    AnchorNode anchorNode = new AnchorNode(anchor);
    anchorNode.setParent(arFragment.getArSceneView().getScene());

    TransformableNode text = new TransformableNode(arFragment.getTransformationSystem());
    text.setParent(anchorNode);
    text.setRenderable(textViewRenderable);
    text.getRotationController();
    text.getScaleController();
    text.select();
    text.setLocalRotation(Quaternion.lookRotation(Vector3.down() ,Vector3.up()));
    Button btn = (Button) textViewRenderable.getView();
    btn.setOnClickListener(view -> {
      Toast.makeText(this, "ボタンをタップしました！詳細へ遷移します", Toast.LENGTH_SHORT).show();
      startActivity(WebActivity.createIntent(this));
      finish();
    });
    isAttachedModel = true;
  }

  private void configureSession() {
    Config config = new Config(session);
    InputStream inputStream;
    try {
      inputStream = getAssets().open("sample_database.imgdb");
      AugmentedImageDatabase imageDatabase = AugmentedImageDatabase.deserialize(session, inputStream);
      config.setAugmentedImageDatabase(imageDatabase);
      config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
      session.configure(config);
    } catch (IOException e) {
      Toast.makeText(this, "Sessionを設定することができません", Toast.LENGTH_SHORT).show();
      Timber.e(e);
    }
    arSceneView.setupSession(session);
  }
}
