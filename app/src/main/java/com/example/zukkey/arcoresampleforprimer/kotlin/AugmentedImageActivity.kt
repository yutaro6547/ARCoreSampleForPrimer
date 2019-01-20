package com.example.zukkey.arcoresampleforprimer.kotlin

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import timber.log.Timber
import com.example.zukkey.arcoresampleforprimer.R
import java.io.IOException
import java.io.InputStream


class AugmentedImageActivity: AppCompatActivity() {
  private var textViewRenderable: ViewRenderable? = null
  private var session: Session? = null
  private var sessionConfigured: Boolean = false
  private var arSceneView: ArSceneView? = null
  private var isAttachedModel: Boolean = false

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, AugmentedImageActivity::class.java)
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
    setContentView(R.layout.activity_augumented_image)
    val arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment?

    if (arFragment != null) {
      arFragment.planeDiscoveryController?.hide()
      arFragment.planeDiscoveryController?.setInstructionView(null)
      arSceneView = arFragment.arSceneView

      arFragment.arSceneView?.scene?.addOnUpdateListener {
        val frame = arFragment.arSceneView?.arFrame
        val updatedAugmentedImages = frame?.getUpdatedTrackables(AugmentedImage::class.java) ?: return@addOnUpdateListener

        for (img in updatedAugmentedImages) {
          if (img.trackingState == TrackingState.TRACKING) {
            if (img.name.contains("eure") && !isAttachedModel) {
              setUp3DModel(img.createAnchor(img.centerPose), arFragment)
            }
          }
        }
      }
    }

  }

  override fun onResume() {
    super.onResume()
    if (session == null) {
      try {
        session = Session(this)
      } catch (e: UnavailableArcoreNotInstalledException) {
        Log.e("Session Error", e.message)
      } catch (e: UnavailableApkTooOldException) {
        Log.e("Session Error", e.message)
      } catch (e: UnavailableSdkTooOldException) {
        Log.e("Session Error", e.message)
      }

      sessionConfigured = true
    }

    if (sessionConfigured) {
      configureSession()
      sessionConfigured = false
    }
  }

  @TargetApi(Build.VERSION_CODES.N)
  private fun setUp3DModel(anchor: Anchor, arFragment: ArFragment) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ViewRenderable.builder()
          .setView(this, R.layout.item_text)
          .build()
          .thenAccept { renderable -> textViewRenderable = renderable }
          .exceptionally {
            Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG).apply {
              setGravity(Gravity.CENTER, 0, 0)
              show()
            }
            null
          }
    }

    if (textViewRenderable == null) {
      return
    }

    val anchorNode = AnchorNode(anchor)
    anchorNode.setParent(arFragment.arSceneView?.scene)

    TransformableNode(arFragment.transformationSystem).apply {
      setParent(anchorNode)
      renderable = textViewRenderable
      rotationController
      scaleController
      select()
      localRotation = Quaternion.lookRotation(Vector3.down(), Vector3.up())
    }
    val btn = textViewRenderable?.view as Button
    btn.setOnClickListener {
      Toast.makeText(this, "ボタンをタップしました！詳細へ遷移します", Toast.LENGTH_SHORT).show()
      startActivity(WebActivity.createIntent(this))
      finish()
    }
    isAttachedModel = true
  }

  private fun configureSession() {
    val config = Config(session)
    val inputStream: InputStream
    try {
      inputStream = assets.open("sample_database.imgdb")
      val imageDatabase = AugmentedImageDatabase.deserialize(session, inputStream)
      with(config) {
        augmentedImageDatabase = imageDatabase
        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session?.configure(this)
      }
    } catch (e: IOException) {
      Toast.makeText(this, "Sessionを設定することができません", Toast.LENGTH_SHORT).show()
      Timber.e(e)
    }

    arSceneView?.setupSession(session)
  }
}
