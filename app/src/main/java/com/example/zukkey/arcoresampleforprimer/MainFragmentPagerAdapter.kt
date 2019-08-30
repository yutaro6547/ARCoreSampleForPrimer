package com.example.zukkey.arcoresampleforprimer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class MainFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
  override fun getItem(position: Int): Fragment {
    return when(position) {
      0 -> JavaFragment.newInstance()
      1 -> KotlinFragment.newInstance()
      else -> throw NoSuchElementException()
    }
  }

  override fun getCount(): Int {
    return 2
  }
}
