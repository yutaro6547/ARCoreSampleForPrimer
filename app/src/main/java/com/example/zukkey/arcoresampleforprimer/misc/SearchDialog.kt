package com.example.zukkey.arcoresampleforprimer.misc

import android.app.AlertDialog
import android.app.Dialog
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import com.example.zukkey.arcoresampleforprimer.R
import com.example.zukkey.arcoresampleforprimer.databinding.SearchDialogBinding


class SearchDialog : DialogFragment() {

  interface PositiveButtonCallBack {
    fun onPositiveButtonClicked(roomCode: String)
  }

  private lateinit var binding: SearchDialogBinding

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(activity)
    binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.search_dialog, null, false)
    builder.setTitle("既存のRoomCodeを入力してください")
        .setView(binding.root)
        .setPositiveButton(
            "OK") { _, _ ->
          if (binding.editText.text.isNotEmpty()) {
            (activity as PositiveButtonCallBack).onPositiveButtonClicked(binding.editText.text.toString())
          } else {
            (activity as PositiveButtonCallBack).onPositiveButtonClicked(0.toString())
          }
        }.setNegativeButton(
            "キャンセル") { _, _ ->
          this.dismiss()
        }
    return builder.create()
  }
}
