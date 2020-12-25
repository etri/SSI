package com.bd.ssishop.api

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.service.SsiHelper
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.runBlocking


class DealDetailPushFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_data_init, container, false)
        val progDialog = ProgressDialog(context)
        progDialog.setCancelable(false)
        progDialog.setMessage("VC 목록 생성중...")

        val step = arguments?.getString("step")
        val rcvDealId = arguments?.getString("dealId")

        (activity as MarketActivity).toolbar_title.setText(R.string.nav_push_detail_deal)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())

        if(rcvDealId != null) {
            val response = runBlocking {
                SsiApi.instance.getSellListByDealId(SsiShopApplication.token, rcvDealId.toInt())
            }

            if (response.code == 1000) {
                val deal_one = response.data
                if (deal_one != null) {
                    Thread.sleep(1500)
                    SsiShopApplication.dealId = rcvDealId
                    when(step){
                        "pub_cert" -> {
                            progDialog.show()
                            SsiHelper.sendVclistForUsedByPush(this@DealDetailPushFragment, deal_one.etc!!)
                            progDialog.dismiss()
                        }
                    }
                }
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            //중고품.물품보증서발급확인. 푸시 알람. ssi_url://vclist
            if (requestCode == SsiHelper.VC_USED_PUSH) {
                val bundle = Bundle()
                bundle.putString("step", "pub_cert")
                bundle.putString("dealId", SsiShopApplication.dealId)
                findNavController().navigate(R.id.nav_pushend_detail_deal, bundle)
            }
        }
    }
}