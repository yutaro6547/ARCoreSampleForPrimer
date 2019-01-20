package com.example.zukkey.arcoresampleforprimer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zukkey.arcoresampleforprimer.databinding.FragmentJavaBinding
import com.example.zukkey.arcoresampleforprimer.java.AugmentedImageActivity
import com.example.zukkey.arcoresampleforprimer.java.CloudAnchorActivity
import com.example.zukkey.arcoresampleforprimer.java.ModelActivity
import com.example.zukkey.arcoresampleforprimer.java.ViewActivity


class JavaFragment : Fragment() {

  lateinit var binding: FragmentJavaBinding

  companion object {
    fun newInstance() : Fragment {
      return JavaFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_java, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentJavaBinding.bind(view)

    with(binding) {
      transition3d.setOnClickListener {
        startActivity(ModelActivity.createIntent(requireContext()))
      }

      transitionView.setOnClickListener {
        startActivity(ViewActivity.createIntent(requireContext()))
      }

      transitionArgumentedImage.setOnClickListener {
        startActivity(AugmentedImageActivity.createIntent(requireContext()))
      }

      transitionCloudAnchor.setOnClickListener {
        startActivity(CloudAnchorActivity.createIntent(requireContext()))
      }
    }
  }
}
