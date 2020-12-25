package com.bd.ssishop.api

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bd.ssishop.R
import com.bd.ssishop.market.MarketActivity
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_data_init.view.*
import kotlinx.coroutines.runBlocking


class DataInitFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_data_init, container, false)

        (activity as MarketActivity).toolbar_title.setText(R.string.menu_init)
        (activity as MarketActivity).toolbar_title.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            18.toFloat()
        )

        alertDialogUp(view)

        return view
    }

    fun alertDialogUp(view:View){
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("데이터 초기화 하시겠습니까?")
        builder.setPositiveButton("예", DialogInterface.OnClickListener { dialog, which ->
                doInit(view)
            })
        builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
                view.tv_data_init.setText("데이터 초기화 취소")
            })
        builder.show()
    }
    
    fun doInit(view:View){
        runBlocking {
            val init: ApiResponse<String> = SsiApi.instance.doProductInit()
            view.tv_data_init.setText("데이터 초기화 완료")
        }
    }
}