package com.bd.ssishop.market.deal

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.service.SsiHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_deal_detail.*
import kotlinx.android.synthetic.main.fragment_deal_detail.btn_cancel
import kotlinx.android.synthetic.main.fragment_deal_detail.btn_check
import kotlinx.android.synthetic.main.fragment_deal_detail.btn_confirm
import kotlinx.android.synthetic.main.fragment_deal_detail.btn_issue
import kotlinx.android.synthetic.main.fragment_deal_detail.btn_receive
import kotlinx.android.synthetic.main.fragment_deal_detail.deal_proof_complete
import kotlinx.android.synthetic.main.fragment_deal_detail.text_delivery_end
import kotlinx.android.synthetic.main.fragment_deal_detail.layout_send
import kotlinx.android.synthetic.main.fragment_deal_detail.layout_warranty
import kotlinx.android.synthetic.main.fragment_deal_detail.text_confirm
import kotlinx.android.synthetic.main.fragment_deal_detail.text_request
import kotlinx.android.synthetic.main.fragment_deal_detail.text_sending
import kotlinx.android.synthetic.main.fragment_deal_detail.view.*
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_check
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_confirm
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_issue
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_receive
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_send
import kotlinx.android.synthetic.main.fragment_deal_detail.view.btn_warranty
import kotlinx.android.synthetic.main.fragment_deal_detail_pushend.*
import kotlinx.android.synthetic.main.fragment_deal_detail_pushend.view.*
import kotlinx.android.synthetic.main.info_delivery_frame.*
import kotlinx.android.synthetic.main.info_order_frame.*
import kotlinx.android.synthetic.main.info_payment_frame.*
import java.text.NumberFormat

/**
 * 거래내역 상세정보 프래그먼트 
 */
class DealDetailFragment : Fragment() {

    private val dealViewModel: DealViewModel by activityViewModels()
    lateinit var dialog: WarrantyDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_deal_detail, container, false)
        var productDid:String = ""
        var didSelected:String = ""         //"DID버튼"클릭해서 받은 DID, 구매자DID
        var didSeller:String = ""           //판매자DID
        var didLogin:String = ""            //로그인 시에 받은 DID, 사용자DID
        var buyerDid:String = ""
        var didBuyer:String = ""
        var dealId:String = ""
        var productType: String = ""

        val rcvDealId = savedInstanceState?.getString("dealId")
//        if(rcvDealId != null){
//            Log.d("### DETAIL rcvd_id", rcvDealId)
//        }else{
//            Log.d("### DETAIL rcvd_id", "null_null")
//        }

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.fragment_deal_detail)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.toFloat())

        //set viewmodel observer. 판매내역
        dealViewModel.selectedPD.observe(viewLifecycleOwner) {
            if( dealViewModel.tab.value == "sell") {
                if (it.img != null && it.img.length > 0) {
                    //            if(it.images != null && it.product.images?.size!! > 0) {
                    Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.img}").into(imageView);
                } else {
                    imageView.setImageResource(android.R.color.transparent)
                }

                productType = it.type

                text_dealId.text = "상품 번호 : ${it.productId}"
                text_productName.text = it.productName
                if (it.state == null) {
                    text_state.text = ""
                } else {
                    text_state.text = it.state
                }
                if (it.count == 0) {
                    text_price.text = "1개 / ${NumberFormat.getNumberInstance().format(it.price)} 원"
                    text_totalPrice.text = "${NumberFormat.getNumberInstance().format(it.price)} 원"
                } else {
                    text_price.text =
                        "${it.count}개 / ${NumberFormat.getNumberInstance().format(it.price)} 원"
                    text_totalPrice.text =
                        "${NumberFormat.getNumberInstance().format(it.price * it.count)} 원"
                }
                text_totalPayment.text =
                    "${NumberFormat.getNumberInstance().format(it.price * it.count)} 원"

                text_address.text = it.addressOfDeal
                text_buyer.text = it.buyerName
                text_phone.text = it.phone

                productDid = it.productDid
                buyerDid = it.did
//                didBuyer = it.didBuyerOfDeal
//                didSeller = it.didSelectedOfDeal
                dealId = it.dealId.toString()

                val tab = dealViewModel.tab.value
                if (it.dealId != 0) {
                    if (tab == "buy") {
                        if (productType == "새제품") {
                            when (it.state) {
                                "결제대기", "결제완료" -> swapBottomButton(btn_cancel)
                                "배송중" -> swapBottomButton(btn_receive)
                                "배송완료" -> when (it.vcState) {
                                    "미발급" -> swapBottomButton(layout_warranty)
                                    "발급요청" -> swapBottomButton(text_request)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> swapBottomButton(btn_confirm)
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        } else {
                            when (it.state) {
                                "결제대기", "결제완료" -> swapBottomButton(btn_cancel)
                                "배송중" -> swapBottomButton(btn_receive)
                                "배송완료" -> when (it.vcState) {
                                    "미발급" -> swapBottomButton(layout_warranty)
                                    "발급요청" -> swapBottomButton(text_request)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> swapBottomButton(btn_confirm)
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        }
                    } else {
                        if (productType == "새제품") {
                            when (it.state) {
                                "결제완료" -> swapBottomButton(layout_send)
                                "배송중" -> swapBottomButton(text_sending)
                                "배송완료" -> when (it.vcState) {
                                    "발급요청" -> swapBottomButton(btn_issue)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> hideAllBottomButton()
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        } else {
                            when (it.state) {
                                "결제완료" -> swapBottomButton(layout_send)
                                "배송중" -> swapBottomButton(text_sending)
                                "배송완료" -> when (it.vcState) {
                                    "미발급" -> swapBottomButton(text_delivery_end)
                                    "발급요청" -> swapBottomButton(btn_issue)
                                    "발급완료" -> swapBottomButton(deal_proof_complete)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> hideAllBottomButton()
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        }
                    }
                }
            }
        }


        //set viewmodel observer. 구매내역
        dealViewModel.selectedBL.observe(viewLifecycleOwner) {
            if( dealViewModel.tab.value == "buy") {
                if (it.img != null && it.img.length > 0) {
//            if(it.images != null && it.product.images?.size!! > 0) {
                    Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.img}").into(imageView);
                } else {
                    imageView.setImageResource(android.R.color.transparent)
                }

                productType = it.type

                text_dealId.text = "주문 번호 : ${it.dealId}"
                text_productName.text = it.productName
                if (it.state == null) {
                    text_state.text = ""
                } else {
                    text_state.text = it.state
                }
                if (it.count == 0) {
                    text_price.text = "1개 / ${NumberFormat.getNumberInstance().format(it.price)} 원"
                    text_totalPrice.text = "${NumberFormat.getNumberInstance().format(it.price)} 원"
                } else {
                    text_price.text = "${it.count}개 / ${NumberFormat.getNumberInstance().format(it.price)} 원"
                    text_totalPrice.text = "${NumberFormat.getNumberInstance().format(it.price * it.count)} 원"
                }
                text_totalPayment.text = "${NumberFormat.getNumberInstance().format(it.price * it.count)} 원"

                text_address.text = it.addressOfDeal
                text_buyer.text = it.buyerName
                text_phone.text = it.phone

                productDid = it.productDid
                buyerDid = it.did
                didSeller = it.didSeller
                didSelected = it.didSelected
                didLogin = it.did
                dealId = it.dealId.toString()

                val tab = dealViewModel.tab.value
                if (it.dealId != 0) {
                    if (tab == "buy") {
                        if (productType == "새제품") {
                            when (it.state) {
                                "결제대기", "결제완료" -> swapBottomButton(btn_cancel)
                                "배송중" -> swapBottomButton(btn_receive)
                                "배송완료" -> when (it.vcState) {
                                    "미발급" -> swapBottomButton(layout_warranty)
                                    "발급요청" -> swapBottomButton(text_request)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> swapBottomButton(btn_confirm)
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        } else {
                            when (it.state) {
                                "결제대기", "결제완료" -> swapBottomButton(btn_cancel)
                                "배송중" -> swapBottomButton(btn_receive)
                                "배송완료" -> when (it.vcState) {
                                    "미발급" -> swapBottomButton(layout_warranty)
                                    "발급요청" -> swapBottomButton(text_request)
                                    "발급완료" -> swapBottomButton(btn_check)      //"물품 보증서 발급 확인"
                                    "보증서확인" -> swapBottomButton(btn_confirm)  //"물품 보증서 발급 확인" 버튼 클릭 이후.
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> swapBottomButton(btn_confirm)
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        }
                    } else {
                        if (productType == "새제품") {
                            when (it.state) {
                                "결제완료" -> swapBottomButton(layout_send)
                                "배송중" -> swapBottomButton(text_sending)
                                "배송완료" -> when (it.vcState) {
                                    "발급요청" -> swapBottomButton(btn_issue)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> hideAllBottomButton()
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        } else {
                            when (it.state) {
                                "결제완료" -> swapBottomButton(layout_send)
                                "배송중" -> swapBottomButton(text_sending)
                                "배송완료" -> when (it.vcState) {
                                    "발급요청" -> swapBottomButton(btn_issue)
                                    "발급완료" -> swapBottomButton(deal_proof_complete)
                                    "구매확정" -> swapBottomButton(text_confirm)
                                    else -> hideAllBottomButton()
                                }
                                "구매확정" -> swapBottomButton(text_confirm)
                                else -> hideAllBottomButton()
                            }
                        }
                    }
                }
            }
        }


        // set buyer event
        root.btn_receive.setOnClickListener {
            dealViewModel.receive {
                Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        }

        root.btn_check.setOnClickListener{
//            root.btn_confirm.callOnClick()
            dealViewModel.confirm {
                if( it != null ){
                    if( productType == "새제품" ) {
                        swapBottomButton(text_confirm)
                        dealViewModel.updateVcStateToComplete(it.dealId.toString()) {
                            if( it == "SUCCESS" ){
//                                Toast.makeText(requireActivity(), "구매 완료", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        val vcs_etc = it.etc
                        SsiHelper.sendVclistForUsed(this@DealDetailFragment, vcs_etc!!)
                        Thread.sleep(1000)
                        dealViewModel.updateVcStateToCheckCertification(it.dealId.toString()) {
                            if( it == "SUCCESS" ){
                                swapBottomButton(btn_confirm)
                            }else{
                                Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else {
                    Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        root.btn_confirm.setOnClickListener {
            dealViewModel.confirm {
                if( it != null ){
                    if( productType == "새제품" ) {
                        swapBottomButton(text_confirm)
                        dealViewModel.updateVcStateToComplete(it.dealId.toString()) {
                            if( it == "SUCCESS" ){
//                                Toast.makeText(requireActivity(), "구매 완료", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        val vcs_etc = it.etc
                        dealViewModel.updateVcStateToComplete(it.dealId.toString()) {
                            if( it == "SUCCESS" ){
                                swapBottomButton(text_confirm)
                            }else{
                                Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
//                        SsiHelper.sendVclistForUsed(this@DealDetailFragment, vcs_etc!!)
                    }
                }else {
                    Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //물품 보증서 요청
        root.btn_warranty.setOnClickListener {
            val progDialog = ProgressDialog(activity)
            progDialog.setCancelable(false)
            progDialog.setMessage("진행중...")
            progDialog.show()

            if( productType == "새제품"){
                SsiShopApplication.productType = "새제품"
            }else{
                SsiShopApplication.productType = "중고품"
            }
            val ssiAppUrl = "ssi://requestdid?productDID=${productDid}&user_id=${didLogin}&buyer_id=${didSelected}&seller_id=${didSeller}"
            SsiShopApplication.dealId = dealId
            SsiShopApplication.didSeller = didSeller
            SsiShopApplication.didSelected = didSelected
            SsiShopApplication.didLogin = didLogin
            val paramMap:MutableMap<String, String> = mutableMapOf()
            SsiHelper.requestDID(this@DealDetailFragment, ssiAppUrl)
            progDialog.dismiss()
        }

        //set seller event
        root.btn_send.setOnClickListener {
            dealViewModel.send {
                Toast.makeText(requireActivity(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        }

        //중고품.판매자.물품보증서 발급 이벤트
        root.btn_issue.setOnClickListener {
            dealViewModel.issue {
                SsiShopApplication.usedDealId = dealId

                if(it.get("result") == "SUCCESS"){
                    val rcvSign = it.get("sign")
                    val rcvPublickKey = it.get("publicKey")
                    val price = it.get("price")
                    val date = it.get("date")
                    val buyer_id = it.get("buyer_id")
                    val seller_id = it.get("seller_id")
                    val user_id = it.get("user_id")
                    val ssiAppUrl = "ssi://requestproductvp?productDID=${productDid}&sign=${rcvSign}&publicKey=${rcvPublickKey}&price=${price}&date=${date}&buyer_id=${buyer_id}&seller_id=${seller_id}&user_id=${user_id}"
                    SsiHelper.requestProductVpForUsed(this@DealDetailFragment, ssiAppUrl)
                }else {
                    Toast.makeText(requireActivity(), "서버 연결 실패. 데이터 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }

//        dialog = WarrantyDialog(View.OnClickListener { dialog.dismiss() }, View.OnClickListener { dialog.dismiss() })
//        dialog.show(parentFragmentManager, "warranty")

        return root
    }


    fun hideAllBottomButton() {
        btn_cancel.visibility = View.GONE
        btn_receive.visibility = View.GONE
        layout_warranty.visibility = View.GONE
        btn_confirm.visibility = View.GONE
        text_request.visibility = View.GONE
        btn_check.visibility = View.GONE
        text_confirm.visibility = View.GONE
        layout_send.visibility = View.GONE
        text_sending.visibility = View.GONE
        btn_issue.visibility = View.GONE
    }

    fun swapBottomButton(view: View){
        hideAllBottomButton()
        view.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK ) {
            //새상품 ssi_url://vclist
            if (requestCode == SsiHelper.VC_SSI_VCLIST) {
                dealViewModel.updateVcStateToEnd(SsiShopApplication.dealId) {}

                if (data != null) {
                    val bundle = data!!.extras
                    val keys: Set<String> = bundle!!.keySet()
                    val itr: Iterator<String> = keys.iterator()
                    while (itr.hasNext()) {
                        val key = itr.next()
                    }
                } else {
                    Log.d("### bundle data", "is NULL")
                }
            }

            //중고품 ssi_url://vclist
            if (requestCode == SsiHelper.VC_SSI_VCLIST_FOR_USED) {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(requireActivity(), "구매확정 성공", Toast.LENGTH_SHORT).show()
                    //                findNavController().navigate(R.id.nav_deal_list)
                } else {
                    Toast.makeText(requireActivity(), "구매확정 실패", Toast.LENGTH_SHORT).show()
                    //                findNavController().navigate(R.id.nav_deal_list)
                }
            }

            //ssi_url://requestdid
            if (requestCode == SsiHelper.REQUEST_DID) {
                val progDialog = ProgressDialog(activity)
                progDialog.setCancelable(false)
                progDialog.setMessage("진행중...")
                progDialog.show()

                var map: MutableMap<String, String> = mutableMapOf()
                map.put("dealId", SsiShopApplication.dealId)
                map.put("did", SsiShopApplication.user.did)
                map.put("didLogin", SsiShopApplication.didLogin)
                map.put("didBuyer", data!!.getStringExtra("did"))           //"DID선택"버튼 클릭 후 SSI APP에서 넘겨준 DID
                map.put("publicKey", data!!.getStringExtra("publicKey"))
                map.put("sign", data!!.getStringExtra("sign"))
                map.put("curProductType", SsiShopApplication.productType)

                dealViewModel.warranty(map) {
                    if (it == "false") {
                        Toast.makeText(requireActivity(), "물품 보증서 발급 실패", Toast.LENGTH_SHORT).show()
                    } else {
                        if (SsiShopApplication.productType == "새제품") {
                            swapBottomButton(btn_confirm)
                            SsiHelper.sendVclist(this@DealDetailFragment, it)
                            progDialog.dismiss()
                        } else {
                            swapBottomButton(text_request)
                            progDialog.dismiss()
                            //                        SsiHelper.sendVclistForUsed(this@DealDetailFragment, it)
                        }
                        //                    Toast.makeText(requireActivity(), "물품 보증서 발급 완료", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //ssi://requestproductvp?productDID={}&sign={}&buyDID={}&publicKey={}
            if (requestCode == SsiHelper.REQUEST_PRODUCT_VP_FOR_USED) {
                var map: MutableMap<String, String> = mutableMapOf()

                map.put("vc", "{\"vc\":${data!!.getStringExtra("vc")}}")
                map.put("dealId", SsiShopApplication.usedDealId)
                map.put("vcState", "발급완료")
                val rcvVc = data!!.getStringExtra("vc")

                dealViewModel.saveUsdedVcs(map) {
                    if (it == "true") {
                        swapBottomButton(deal_proof_complete)
                    } else {
                        Toast.makeText(requireActivity(), "서버 연결 실패. 데이터 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}