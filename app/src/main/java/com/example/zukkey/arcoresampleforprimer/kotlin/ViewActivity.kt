package com.example.zukkey.arcoresampleforprimer.kotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.example.zukkey.arcoresampleforprimer.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class ViewActivity: AppCompatActivity() {
  private val CAMERA_PERMISSION_CODE = 0
  private val CAMERA_PERMISSION = Manifest.permission.CAMERA
  private var imageViewRenderable: ViewRenderable? = null
  private var textViewRenderable: ViewRenderable? = null
  private var count: Int = 0

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, ViewActivity::class.java)
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

    setContentView(R.layout.activity_view)
    val arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      ViewRenderable.builder()
          .setView(this, R.layout.item_image)
          .build()
          .thenAccept { renderable -> imageViewRenderable = renderable }
          .exceptionally {
            val toast = Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            null
          }

      ViewRenderable.builder()
          .setView(this, R.layout.item_text)
          .build()
          .thenAccept { renderable -> textViewRenderable = renderable }
          .exceptionally {
            val toast = Toast.makeText(this, "レンダリングできません", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            null
          }
    }

    arFragment?.setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
      if (imageViewRenderable == null || textViewRenderable == null) {
        return@setOnTapArPlaneListener
      }

      val anchor = hitResult.createAnchor()
      val anchorNode = AnchorNode(anchor)
      anchorNode.setParent(arFragment.arSceneView?.scene)

      if (count == 0) {
        TransformableNode(arFragment.transformationSystem).apply {
          setParent(anchorNode)
          renderable = textViewRenderable
          rotationController
          scaleController
          select()
        }
        val btn = textViewRenderable?.view as Button
        btn.setOnClickListener {
          Toast.makeText(this, "ボタンをタップしました！詳細へ遷移します", Toast.LENGTH_SHORT).show()
          startActivity(WebActivity.createIntent(this))
          finish()
        }
      } else {
        TransformableNode(arFragment.transformationSystem).apply {
          setParent(anchorNode)
          renderable = imageViewRenderable
          rotationController
          scaleController
          translationController
          select()
        }
      }
      count += 1
    }
  }


  override fun onResume() {
    super.onResume()
    if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
    }
  }
}
