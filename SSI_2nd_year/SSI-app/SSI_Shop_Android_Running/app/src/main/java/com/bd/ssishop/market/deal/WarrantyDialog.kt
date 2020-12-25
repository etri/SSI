package com.bd.ssishop.market.deal

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bd.ssishop.R
import kotlinx.android.synthetic.main.dialog_warranty.view.*

/**
 * 보증서 수신 팝업 다이얼로그
 */
class WarrantyDialog(
    val onOkClickListener: View.OnClickListener,
    val onConfirmClickListener: View.OnClickListener
) : DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_warranty, null)
            view.btn_ok.setOnClickListener(onOkClickListener)
            view.btn_confirm.setOnClickListener(onConfirmClickListener)
            builder.setView(view)
            builder.create()
        }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog!!
    }
}