package com.bd.ssishop.login

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bd.ssishop.R
import kotlinx.android.synthetic.main.dialog_warranty.view.*

class VcloginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_warranty, null)
//            view.btn_confirm.setOnClickListener(onConfirmClickListener)
            builder.setView(view)
            builder.create()
        }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog!!
    }
}