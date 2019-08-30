package com.example.zukkey.arcoresampleforprimer.kotlin

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import com.example.zukkey.arcoresampleforprimer.R
import com.example.zukkey.arcoresampleforprimer.misc.AnchorState
import com.example.zukkey.arcoresampleforprimer.misc.EditTextDialog
import com.example.zukkey.arcoresampleforprimer.misc.SearchDialog
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.database.DatabaseError
import timber.log.Timber

class CloudAnchorActivity : AppCompatActivity(), SearchDialog.PositiveButtonCallBack, EditTextDialog.EditTextPositiveButtonCallBack {

  private lateinit var arFragment: CloudAnchorFragment
  private var anchor: Anchor? = null
  private lateinit var anchorState: AnchorState
  private lateinit var firebaseManager: FirebaseManager
  private var memoViewRenderable: ViewRenderable? = null
  private lateinit var inputCodeForm: EditText
  private lateinit var header: TextView
  private lateinit var progressBar: ProgressBar
  private var inputCode: Long = 0L
  private var inputText: EditText? = null
  private lateinit var editorMemo: String

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, CloudAnchorActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val availability = ArCoreApk.getInstance().checkAvailability(this)
    if (availability.isSupported) {
      Toast.makeText(this, "AR機能が利用できます", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(this, "AR機能を利用することができません", Toast.LENGTH_SHORT).show()
      finish()
      return
    }

    setContentView(R.layout.activity_cloud_anchor_kotlin)
    setUpArFragment()

    inputCodeForm = findViewById(R.id.room_code_edit)
    header = findViewById(R.id.room_header)
    progressBar = findViewById(R.id.progress_bar)
    editorMemo = ""

    firebaseManager = FirebaseManager(this)
    anchorState = AnchorState.None

    val clearButton = findViewById<Button>(R.id.clear_button)
    clearButton.setOnClickListener {
      initializedAnchor(null)
      anchorState = AnchorState.None
      header.setText(R.string.default_room)
    }

    val sendButton = findViewById<Button>(R.id.send_button)
    sendButton.setOnClickListener {
      inputCode = java.lang.Long.parseLong(inputCodeForm.text.toString())
      inputCodeForm.text.clear()
      Toast.makeText(this, "Planeをタップしてください", Toast.LENGTH_SHORT).show()
    }

    val searchButton = findViewById<FloatingActionButton>(R.id.search_button)
    searchButton.setOnClickListener {
      val dialog = SearchDialog()
      dialog.show(supportFragmentManager, "Resolve")
    }

    arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
      val newAnchor = arFragment.arSceneView.session?.hostCloudAnchor(hitResult.createAnchor()) ?: return@setOnTapArPlaneListener
      initializedAnchor(newAnchor)
      setUpRendering(newAnchor)
      anchorState = AnchorState.Hosting
      progressBar.visibility = View.VISIBLE
    }

  }

  private fun setUpArFragment() {
    arFragment = (supportFragmentManager.findFragmentById(R.id.ar_fragment) as CloudAnchorFragment).apply {
      planeDiscoveryController.hide()
      planeDiscoveryController.setInstructionView(null)
      arSceneView.scene.addOnUpdateListener { this@CloudAnchorActivity.onUpdatingFrame() }
    }
  }

  fun initializedAnchor(newAnchor: Anchor?) {
    if (anchor != null) {
      anchor?.detach()
    }
    anchor = newAnchor
    anchorState = AnchorState.None
  }

  fun setUpRendering(newAnchor: Anchor) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ViewRenderable.builder()
          .setView(this, R.layout.item_memo)
          .build()
          .thenAccept { renderable -> memoViewRenderable = renderable }
          .exceptionally {
            val toast = Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            null
          }
    }

    if (memoViewRenderable == null) {
      return
    }

    val anchorNode = AnchorNode(newAnchor)
    anchorNode.setParent(arFragment.arSceneView.scene)
    val memo = TransformableNode(arFragment.transformationSystem)
    memo.setParent(anchorNode)
    memo.renderable = memoViewRenderable
    memo.rotationController
    memo.scaleController
    memo.translationController
    memo.select()

    inputText = memoViewRenderable?.view?.findViewById(R.id.memo_edit)
    inputText?.apply {
      if (editorMemo != "") {
        setText(editorMemo)
      }
      setOnClickListener { _ ->
        val dialog = EditTextDialog()
        if (text != null || text.toString() != "") {
          dialog.setDefaultText(text.toString())
        }
        dialog.show(supportFragmentManager, "Edit")
      }
    }
  }

  @Synchronized
  private fun onUpdatingFrame() {
    if (anchorState == AnchorState.Hosting) {
      Timber.i("Hosting中")
    }
    if (anchorState != AnchorState.Hosting && anchorState != AnchorState.Resolving) {
      Timber.i("HostingかResolvingが失敗しました")
      return
    }

    val state = anchor?.cloudAnchorState ?: return
    if (anchorState == AnchorState.Hosting) {
      if (state.isError) {
        Toast.makeText(this, "Hostingエラーです", Toast.LENGTH_SHORT).show()
        anchorState = AnchorState.None
        progressBar.visibility = View.GONE
      } else if (state == Anchor.CloudAnchorState.SUCCESS) {
        Toast.makeText(this, "Hosting成功しました", Toast.LENGTH_SHORT).show()
        firebaseManager.createNewRoom(inputCode, object : FirebaseManager.RoomCodeListener {

          override fun onNewRoomCode(newRoomCode: Long?) {
            if (newRoomCode == null) {
              Toast.makeText(this@CloudAnchorActivity, "Room Codeが存在しません", Toast.LENGTH_SHORT).show()
            }
            header.text = newRoomCode.toString()
            firebaseManager.storeAnchorIdInRoom(inputCode, anchor!!.cloudAnchorId, editorMemo)
            progressBar.visibility = View.GONE
            anchorState = AnchorState.None
          }

          override fun onError(databaseError: DatabaseError?) {
            Toast.makeText(this@CloudAnchorActivity, "Databaseエラーです", Toast.LENGTH_SHORT).show()
            Log.e("Database Error", databaseError?.message)
            header.setText(R.string.default_room)
            progressBar.visibility = View.GONE
            anchorState = AnchorState.None
          }
        })
      }
    }

    if (anchorState == AnchorState.Resolving) {
      if (state.isError) {
        Toast.makeText(this, "Resolvingエラーです", Toast.LENGTH_SHORT).show()
        anchorState = AnchorState.None
      } else if (state == Anchor.CloudAnchorState.SUCCESS) {
        Toast.makeText(this, "Resolvingが成功しました", Toast.LENGTH_SHORT).show()
        anchorState = AnchorState.None
      }
    }
  }

  override fun onPositiveButtonClicked(roomCode: String) {
    firebaseManager.resolvingRoom(java.lang.Long.parseLong(roomCode), object : FirebaseManager.CloudAnchorIdListener {
      override fun onNewCloudAnchorId(cloudAnchorId: String) {
        header.text = roomCode
        val resolvedAnchor = arFragment.arSceneView.session?.resolveCloudAnchor(cloudAnchorId) ?: return
        initializedAnchor(resolvedAnchor)
        setUpRendering(resolvedAnchor)
        anchorState = AnchorState.Resolving
      }

      override fun onSetMemo(memo: String, cloudAnchorId: String) {
        inputText?.setText(memo)
      }
    })
  }

  override fun onEditTextPositiveButtonClicked(text: String) {
    editorMemo = text
    inputText?.setText(editorMemo)
  }
}
