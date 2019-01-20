package com.example.zukkey.arcoresampleforprimer.java;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.zukkey.arcoresampleforprimer.R;
import com.example.zukkey.arcoresampleforprimer.kotlin.FirebaseManager;
import com.example.zukkey.arcoresampleforprimer.misc.AnchorState;
import com.example.zukkey.arcoresampleforprimer.misc.EditTextDialog;
import com.example.zukkey.arcoresampleforprimer.misc.SearchDialog;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DatabaseError;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class CloudAnchorActivity extends AppCompatActivity implements SearchDialog.PositiveButtonCallBack, EditTextDialog.EditTextPositiveButtonCallBack {

  public CloudAnchorFragment arFragment;
  public Anchor anchor;
  public AnchorState anchorState;
  private FirebaseManager firebaseManager;
  private ViewRenderable memoViewRenderable;
  private EditText inputCodeForm;
  private TextView header;
  private ProgressBar progressBar;
  private Long inputCode = 0L;
  private EditText inputText;
  private String editorMemo;

  public static Intent createIntent(Context context) {
    return new Intent(context, CloudAnchorActivity.class);
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

    setContentView(R.layout.activity_cloud_anchor);
    setUpArFragment();

    inputCodeForm = findViewById(R.id.room_code_edit);
    header = findViewById(R.id.room_header);
    progressBar = findViewById(R.id.progress_bar);
    editorMemo = "";

    firebaseManager = new FirebaseManager(this);
    anchorState = AnchorState.None.INSTANCE;

    Button clearButton = findViewById(R.id.clear_button);
    clearButton.setOnClickListener(view -> {
      initializedAnchor(null);
      anchorState = AnchorState.None.INSTANCE;
      header.setText(R.string.default_room);
    });

    Button sendButton = findViewById(R.id.send_button);
    sendButton.setOnClickListener(view -> {
      inputCode = Long.parseLong(inputCodeForm.getText().toString());
      inputCodeForm.getText().clear();
      Toast.makeText(this, "Planeをタップしてください", Toast.LENGTH_SHORT).show();
    });

    FloatingActionButton searchButton = findViewById(R.id.search_button);
    searchButton.setOnClickListener(view -> {
      SearchDialog dialog = new SearchDialog();
      dialog.show(getSupportFragmentManager(), "Resolve");
    });

    arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
      Anchor newAnchor = arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());
      initializedAnchor(newAnchor);
      setUpRendering(newAnchor);
      anchorState = AnchorState.Hosting.INSTANCE;
      progressBar.setVisibility(View.VISIBLE);
    });

  }

  public void setUpArFragment() {
    arFragment = (CloudAnchorFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
    arFragment.getPlaneDiscoveryController().hide();
    arFragment.getPlaneDiscoveryController().setInstructionView(null);
    arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> onUpdatingFrame());
  }

  public void initializedAnchor(Anchor newAnchor) {
    if (anchor != null) {
      anchor.detach();
    }
    anchor = newAnchor;
    anchorState = AnchorState.None.INSTANCE;
  }

  public void setUpRendering(Anchor newAnchor) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ViewRenderable.builder()
          .setView(this, R.layout.item_memo)
          .build()
          .thenAccept(renderable -> memoViewRenderable = renderable);
    }

    if (memoViewRenderable == null) {
      return;
    }

    AnchorNode anchorNode = new AnchorNode(newAnchor);
    anchorNode.setParent(arFragment.getArSceneView().getScene());
    TransformableNode memo = new TransformableNode(arFragment.getTransformationSystem());
    memo.setParent(anchorNode);
    memo.setRenderable(memoViewRenderable);
    memo.getRotationController();
    memo.getScaleController();
    memo.getTranslationController();
    memo.select();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      inputText = memoViewRenderable.getView().findViewById(R.id.memo_edit);
    }
    if (!editorMemo.equals("")) {
      inputText.setText(editorMemo);
    }
    inputText.setOnClickListener(view -> {
      EditTextDialog dialog = new EditTextDialog();
      if (inputText.getText() != null || !inputText.getText().toString().equals("")) {
        dialog.setDefaultText(inputText.getText().toString());
      }
      dialog.show(getSupportFragmentManager(), "Edit");
    });
  }

  public synchronized void onUpdatingFrame() {
    if (anchorState == AnchorState.Hosting.INSTANCE) {
      Timber.i("Hosting中");
    }
    if (anchorState != AnchorState.Hosting.INSTANCE && anchorState != AnchorState.Resolving.INSTANCE) {
      Timber.i("HostingかResolvingが失敗しました");
      return;
    }
    Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
    if (anchorState == AnchorState.Hosting.INSTANCE) {
      if (state.isError()) {
        Toast.makeText(this, "Hostingエラーです", Toast.LENGTH_SHORT).show();
        anchorState = AnchorState.None.INSTANCE;
        progressBar.setVisibility(View.GONE);
      } else if (state == Anchor.CloudAnchorState.SUCCESS) {
        Toast.makeText(this, "Hosting成功しました", Toast.LENGTH_SHORT).show();
        firebaseManager.createNewRoom(inputCode, new FirebaseManager.RoomCodeListener() {
          @Override
          public void onNewRoomCode(Long newRoomCode) {
            if (newRoomCode == null) {
              Toast.makeText(CloudAnchorActivity.this, "Room Codeが存在しません", Toast.LENGTH_SHORT).show();
            }
            header.setText(String.valueOf(newRoomCode));
            firebaseManager.storeAnchorIdInRoom(inputCode, anchor.getCloudAnchorId(), editorMemo);
            progressBar.setVisibility(View.GONE);
            anchorState = AnchorState.None.INSTANCE;
          }

          @Override
          public void onError(DatabaseError databaseError) {
            Toast.makeText(CloudAnchorActivity.this, "Databaseエラーです", Toast.LENGTH_SHORT).show();
            Log.e("Database Error", databaseError.getMessage());
            header.setText(R.string.default_room);
            progressBar.setVisibility(View.GONE);
            anchorState = AnchorState.None.INSTANCE;
          }
        });
      }
    }

    if (anchorState == AnchorState.Resolving.INSTANCE) {
      if (state.isError()) {
        Toast.makeText(this, "Resolvingエラーです", Toast.LENGTH_SHORT).show();
        anchorState = AnchorState.None.INSTANCE;
      } else if (state == Anchor.CloudAnchorState.SUCCESS) {
        Toast.makeText(this, "Resolvingが成功しました", Toast.LENGTH_SHORT).show();
        anchorState = AnchorState.None.INSTANCE;
      }
    }
  }

  @Override
  public void onPositiveButtonClicked(@NotNull String roomCode) {
    firebaseManager.resolvingRoom(Long.parseLong(roomCode), new FirebaseManager.CloudAnchorIdListener() {
      @Override
      public void onNewCloudAnchorId(String cloudAnchorId) {
        header.setText(roomCode);
        Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId);
        initializedAnchor(resolvedAnchor);
        setUpRendering(resolvedAnchor);
        anchorState = AnchorState.Resolving.INSTANCE;
      }

      @Override
      public void onSetMemo(String memo, String cloudAnchorId) {
        inputText.setText(memo);
      }
    });
  }

  @Override
  public void onEditTextPositiveButtonClicked(@NotNull String text) {
    editorMemo = text;
    inputText.setText(editorMemo);
  }
}
