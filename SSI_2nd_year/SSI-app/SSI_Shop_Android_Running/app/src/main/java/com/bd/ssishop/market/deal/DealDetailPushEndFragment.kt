package com.bd.ssishop.market.deal

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.api.request.DealRequest
import com.bd.ssishop.api.request.LoginRequest
import com.bd.ssishop.api.response.LoginResponse
import com.bd.ssishop.login.LoginActivity
import com.bd.ssishop.market.BuyListVO
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.service.SsiHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_deal_detail.*
import kotlinx.android.synthetic.main.fragment_deal_detail_pushend.view.*
import kotlinx.android.synthetic.main.info_delivery_frame.view.*
import kotlinx.android.synthetic.main.info_order_frame.view.*
import kotlinx.android.synthetic.main.info_payment_frame.view.*
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_dealId
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_price
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_productName
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_state
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.NumberFormat

/**
 * 거래내역 상세정보 프래그먼트 
 */
class DealDetailPushEndFragment : Fragment() {
    private val dealViewModel: DealViewModel by activityViewModels()
    lateinit var dialog: WarrantyDialog
    var step = ""
    var dealId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val progDialog = ProgressDialog(context)
        progDialog.setCancelable(false)
        progDialog.setMessage("진행중...")
        progDialog.show()

        val root = inflater.inflate(R.layout.fragment_deal_detail_pushend, container, false)
        var productDid: String = ""
        var buyerDid: String = ""
        var productType: String = ""

        step = arguments?.getString("step").toString()
        dealId = arguments?.getString("dealId").toString()

        if(!SsiShopApplication.runMode.isBlank()){
            //set title
            (activity as MarketActivity).toolbar_title.setText(R.string.fragment_deal_detail)
            (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())

            val buyListVO: ApiResponse<BuyListVO>? = runBlocking {
                dealId?.let { SsiApi.instance.getBuyHistByDealId(SsiShopApplication.token, it) }
            }
            var buyHist = BuyListVO()
            if (buyListVO?.data != null) {
                buyHist = buyListVO.data!!
            }

            if (buyHist != null) {
                SsiShopApplication.productType = buyHist.type
                root.text_dealId.text = "주문 번호 : ${buyHist.dealId}"
                root.text_productName.text = buyHist.productName
                if (buyHist.count == 0) {
                    root.text_price.text =
                        NumberFormat.getNumberInstance().format(buyHist.price) + " 원"
                } else {
                    root.text_price.text = NumberFormat.getNumberInstance().format(buyHist.price * buyHist.count) + " 원"
                }
                root.text_totalPrice.text = NumberFormat.getNumberInstance().format(buyHist.totalPrice.toInt())
                root.text_totalPayment.text = NumberFormat.getNumberInstance().format(buyHist.totalPrice.toInt())
                root.text_buyer.text = buyHist.buyerName
                root.text_phone.text = buyHist.phone
                root.text_address.text = buyHist.addressOfDeal

                when (step) {
                    "paid" -> {
                        swapBottomButton(root.text_sending)
                        root.text_state.text = buyHist.state
                    }
                    "get" -> {
                        swapBottomButton(root.text_delivery_end)
                        root.text_state.text = buyHist.state
                    }
                    "req_cert" -> {
                        swapBottomButton(root.btn_issue)
                        root.text_state.text = buyHist.vcState
                    }
                    "pub_cert" -> {
                        swapBottomButton(root.btn_confirm)
                        root.text_state.text = buyHist.vcState
                    }
                    "buy_end" -> {
                        swapBottomButton(root.text_confirm)
                        root.text_state.text = buyHist.vcState
                    }
                }

                val imgUrlOfNew = ApiGenerator.IMAGE_URL + buyHist.img
                Picasso.get().load(imgUrlOfNew).into(root.imageView)
            }

            progDialog.dismiss()
        }else{
            val intent = Intent(context, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            getActivity()?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            progDialog.dismiss()
            onStop()
        }

        root.btn_confirm.setOnClickListener{
            if (dealId != null) {
                val response = runBlocking {
                    SsiApi.instance.updateVcStateToComplete(SsiShopApplication.token, Integer.parseInt(dealId))
                }

                if(response.data!!.toBoolean()){
                    swapBottomButton(text_confirm)
                }
            }
        }

        root.btn_issue.setOnClickListener{
            val response = runBlocking{
                val dealRequest = DealRequest(dealId.toInt())
                SsiApi.instance.issue(SsiShopApplication.token, dealRequest)
            }

            val deal = response.data!!
            val rcvSign = deal.sign
            val rcvPublickKey = deal.publicKey
            val price = NumberFormat.getNumberInstance().format(deal.count * deal.product.price)
            val date = deal.dealDate
            val buyer_id = deal.didBuyer
            val seller_id = deal.didSelected
            val user_id = deal.didSeller
            val ssiAppUrl = "ssi://requestproductvp?productDID=${productDid}&sign=${rcvSign}&publicKey=${rcvPublickKey}&price=${price}&date=${date}&buyer_id=${buyer_id}&seller_id=${seller_id}&user_id=${user_id}"
            SsiHelper.requestProductVpForUsedPush(this, ssiAppUrl)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                var bundle = Bundle()
                if( SsiShopApplication.productType == "중고품" ){
                    when(step){
                        "pub_cert" -> bundle.putString("tabIndex", "0")
                        else -> bundle.putString("tabIndex", "1")
                    }
                }else{
                    bundle.putString("tabIndex", "0")
                }
                val navController = Navigation.findNavController(root)
                navController.navigate(R.id.nav_deal_list, bundle)
            }
        })

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //ssi://requestproductvp?productDID={}&sign={}&buyDID={}&publicKey={}
        if (requestCode == SsiHelper.REQUEST_PRODUCT_VP_FOR_USED_PUSH) {
            var map: MutableMap<String, String> = mutableMapOf()

            map.put("vc", "{\"vc\":${data!!.getStringExtra("vc")}}")
            map.put("dealId", dealId)
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

    fun hideAllBottomButton() {
        view?.btn_cancel?.visibility = View.GONE
        view?.btn_receive?.visibility = View.GONE
        view?.layout_warranty?.visibility = View.GONE
        view?.btn_confirm?.visibility = View.GONE
        view?.text_request?.visibility = View.GONE
        view?.btn_check?.visibility = View.GONE
        view?.text_confirm?.visibility = View.GONE
        view?.layout_send?.visibility = View.GONE
        view?.text_sending?.visibility = View.GONE
        view?.btn_issue?.visibility = View.GONE
    }

    fun swapBottomButton(view: View){
        hideAllBottomButton()
        view.visibility = View.VISIBLE
    }
}