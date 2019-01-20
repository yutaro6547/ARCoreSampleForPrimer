package com.example.zukkey.arcoresampleforprimer.kotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import com.example.zukkey.arcoresampleforprimer.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class ModelActivity: AppCompatActivity() {
  private val CAMERA_PERMISSION_CODE = 0
  private val CAMERA_PERMISSION = Manifest.permission.CAMERA
  private var modelRenderable: ModelRenderable? = null

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, ModelActivity::class.java)
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

    setContentView(R.layout.activity_model)
    val arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as? ArFragment

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ModelRenderable.builder()
          .setSource(this, R.raw.rin)
          .build()
          .thenAccept { renderable -> modelRenderable = renderable }
          .exceptionally {
            val toast = Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            null
          }
    }

    arFragment?.setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
      if (modelRenderable == null) {
        return@setOnTapArPlaneListener
      }

      val anchor = hitResult.createAnchor()
      val anchorNode = AnchorNode(anchor)
      anchorNode.setParent(arFragment.arSceneView?.scene)

      TransformableNode(arFragment.transformationSystem).apply {
        setParent(anchorNode)
        renderable = modelRenderable
        rotationController
        scaleController
        translationController
        select()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
    }
  }
}
