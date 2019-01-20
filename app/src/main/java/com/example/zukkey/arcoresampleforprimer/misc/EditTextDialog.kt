package com.example.zukkey.arcoresampleforprimer.misc

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import com.example.zukkey.arcoresampleforprimer.R
import com.example.zukkey.arcoresampleforprimer.databinding.EditDialogBinding


class EditTextDialog : DialogFragment() {

  interface EditTextPositiveButtonCallBack {
    fun onEditTextPositiveButtonClicked(text: String)
  }

  private lateinit var binding: EditDialogBinding
  private var defaultText = ""

  fun setDefaultText(text: String) {
    defaultText = text
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(activity)
    binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.edit_dialog, null, false)
    if (defaultText.isNotEmpty()) {
      binding.editText.setText(defaultText)
    }
    builder.setTitle("好きな文字を入力してください")
        .setView(binding.root)
        .setPositiveButton(
            "OK") { _, _ ->
          (activity as EditTextPositiveButtonCallBack).onEditTextPositiveButtonClicked(binding.editText.text.toString())
        }.setNegativeButton(
            "キャンセル") { _, _ ->
          this.dismiss()
        }
    return builder.create()
  }
}
