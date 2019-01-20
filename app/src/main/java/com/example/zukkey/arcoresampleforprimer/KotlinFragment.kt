package com.example.zukkey.arcoresampleforprimer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zukkey.arcoresampleforprimer.databinding.FragmentKotlinBinding
import com.example.zukkey.arcoresampleforprimer.kotlin.AugmentedImageActivity
import com.example.zukkey.arcoresampleforprimer.kotlin.CloudAnchorActivity
import com.example.zukkey.arcoresampleforprimer.kotlin.ModelActivity
import com.example.zukkey.arcoresampleforprimer.kotlin.ViewActivity


class KotlinFragment : Fragment() {

  lateinit var binding: FragmentKotlinBinding

  companion object {
    fun newInstance() : Fragment {
      return KotlinFragment()
    }
  }


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_kotlin, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentKotlinBinding.bind(view)

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
