package com.bd.ssishop.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.data.Issuer
import com.bd.ssishop.data.PreData_UserVC
import com.bd.ssishop.data.VCsTemplate


class SsiHelper {

    companion object {
        const val VC_LOGIN = 10000
        const val VP_PURCHASE = 50000
        const val REQUEST_DID = 50001
        const val VC_SSI_VCLIST = 50002
        const val GET_REGI_PRODUCT_DID = 50003
        const val VP_USED_REGI = 50004
        const val REQUEST_PRODUCT_VP_FOR_USED = 5005
        const val VC_SSI_VCLIST_FOR_USED = 5006
        const val VC_USED_PUSH = 5007
        const val REQUEST_PRODUCT_VP_FOR_USED_PUSH = 5008

        /**
         * 새상품 결제 시 SSI APP에 VP요청
         */
        fun getPurchaseVP(fragment: Fragment, valueMap: MutableMap<String, String>){
            val uuid = SsiShopApplication.uuid

            valueMap.put("ISSUER_DID", Issuer.ISSUER_DID)
            if( valueMap.get("selectedCard") == "한국카드" ) {
                //한국카드 인 경우
                valueMap.put("CARD_ISSUER_DID", Issuer.CARD_ISSUER_DID_01)
            }else{
                //서울카드 인 경우
                valueMap.put("CARD_ISSUER_DID", Issuer.CARD_ISSUER_DID_02)
            }
            valueMap.put("ISSUE_TIME", "")
            valueMap.put("UUID", uuid)
            valueMap.put("URL", "")
            valueMap.put("payment", valueMap.get("totalPrice").toString())
            valueMap.put("BUYER_DID", SsiShopApplication.user.did)

            val vcsTemp = VCsTemplate(valueMap)
            val sendVCs: String = vcsTemp.getSendVcsForBuying()
            val ssi_url = "ssi://requestVp?requestVP=${sendVCs}"

            val ssiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            fragment.startActivityForResult(ssiIntent, VP_PURCHASE)
        }

        /**
         * 중고품 등록 시 SSI APP에 VP요청
         */
        fun getPurchaseVPForUsed(fragment: Fragment, valueMap: MutableMap<String, String>){
            val uuid = SsiShopApplication.uuid

            valueMap.put("ISSUER_DID", Issuer.ISSUER_DID)
            valueMap.put("ISSUE_TIME", "")
            valueMap.put("UUID", uuid)
            valueMap.put("URL", "")
            valueMap.put("payment", valueMap.get("totalPrice").toString())
            valueMap.put("usedStepName", "Product-Description-Post-Request");

            val vcsTemp = VCsTemplate(valueMap)
            val reqVp: String = vcsTemp.getReqVpForUsedRegi()
            val ssi_url = "ssi://requestVp?requestVP=${reqVp}"

            val ssiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            fragment.startActivityForResult(ssiIntent, VP_USED_REGI)
        }

        /**
         * param : ssiAppUrl = "DID"버튼인 경우 "ssi://requestdid", "물품보증서요청"인 경우 "ssi://requestdid?productDID={did}"
         */
        fun requestDID(fragment: Fragment, ssiAppUrl: String){
            val ssiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ssiAppUrl))
            fragment.startActivityForResult(ssiIntent, REQUEST_DID)
        }

        fun sendVclist(fragment: Fragment, vcs: String){
            val ssi_url = "ssi://vclist?vc=${vcs}"
            val pi = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            fragment.startActivityForResult(pi, VC_SSI_VCLIST)
        }

        fun sendVclistForUsed(fragment: Fragment, vcs: String){
            val ssi_url = "ssi://vclist?vc=${vcs}"
            val used_pi = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            fragment.startActivity(used_pi)
        }

        fun sendVclistForUsedByPush(fragment: Fragment, vcs: String){
            val ssi_url = "ssi://vclist?vc=${vcs}"
            val used_pi = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            fragment.startActivityForResult(used_pi, VC_USED_PUSH)
        }

        fun requestProductVpForUsed(fragment: Fragment, ssiAppUrl: String){
            val ssiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ssiAppUrl))
            fragment.startActivityForResult(ssiIntent, REQUEST_PRODUCT_VP_FOR_USED)
        }

        fun requestProductVpForUsedPush(fragment: Fragment, ssiAppUrl: String){
            val ssiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ssiAppUrl))
            fragment.startActivityForResult(ssiIntent, REQUEST_PRODUCT_VP_FOR_USED_PUSH)
        }
    }
}