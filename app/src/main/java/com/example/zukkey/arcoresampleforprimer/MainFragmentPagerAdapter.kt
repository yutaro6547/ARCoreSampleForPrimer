package com.example.zukkey.arcoresampleforprimer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


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
