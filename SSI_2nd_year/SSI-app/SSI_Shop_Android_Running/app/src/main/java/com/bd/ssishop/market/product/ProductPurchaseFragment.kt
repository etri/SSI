package com.bd.ssishop.market.product

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.androidquery.callback.BitmapAjaxCallback.async
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.market.deal.Deal
import com.bd.ssishop.service.SsiHelper
import com.bd.ssishop.service.SsiHelper.Companion.VP_PURCHASE
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_product_purchase.*
import kotlinx.android.synthetic.main.fragment_product_purchase.text_price
import kotlinx.android.synthetic.main.fragment_product_purchase.text_productName
import kotlinx.android.synthetic.main.fragment_product_purchase.view.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.info_order_frame.*
import kotlinx.android.synthetic.main.info_order_frame.view.*
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*

/**
 * 상품 구매 화면 프래그먼트
 */
class ProductPurchaseFragment : Fragment() {

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        super.onCreate(savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_product_purchase, container, false)
        val valueMap: MutableMap<String, String> = mutableMapOf()

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.fragment_product_purchase)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.toFloat())

        if(root.spinner_purchase.selectedItem != "신용카드"){
            root.spinner_purchase.visibility = View.GONE
        }

        root.spinner_purchase.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position).toString()

                if( item == "신용카드" ){
                    root.spinner_card.visibility = View.VISIBLE
                    root.img_card.visibility = View.VISIBLE
                }else{
                    root.spinner_card.visibility = View.GONE
                    root.img_card.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("### spn_pur_item", "1")
            }
        }

        //set data
        root.edit_name.setText(SsiShopApplication.user.name)
        root.edit_phone.setText(SsiShopApplication.user.phone)
//        root.text_postnum.text = "08390"
        root.edit_addr1.setText(SsiShopApplication.user.address)

        //수량 증가 버튼
        root.btn_plus.setOnClickListener {
            productViewModel.setCount(productViewModel.getCount() + 1)
        }

        //수량 감소 버튼
        root.btn_minus.setOnClickListener {
            if(productViewModel.getCount() > 1){
                productViewModel.setCount(productViewModel.getCount() - 1)
            }
        }

        //주소 검색 버튼
//        root.btn_addr.setOnClickListener {
//            text_postnum.text = "08390"
//            edit_addr1.setText(SsiShopApplication.user.address)
//        }

        //결제하기 버튼
        root.btn_purchase.setOnClickListener {
            //상품 구매용 VP 제출
            SsiShopApplication.typedName = edit_name.text.toString()
            SsiShopApplication.typedPhone = edit_phone.text.toString()
//            SsiShopApplication.typedZipcode = text_postnum.text.toString()
            SsiShopApplication.typedAddress = edit_addr1.text.toString()
            SsiShopApplication.paymentMethod = spinner_purchase.getSelectedItem().toString()
            SsiShopApplication.paymentCard = spinner_card.getSelectedItem().toString()
            valueMap.put("selectedCard", spinner_card.getSelectedItem().toString())
            SsiHelper.getPurchaseVP(this@ProductPurchaseFragment, valueMap)
        }

        //set viewmodel observer
        productViewModel.selected.observe(viewLifecycleOwner) {
            if(it.type == "새제품"){
                root.imageViewNew.visibility = View.VISIBLE
                root.imageViewUsed.visibility = View.GONE
            }else{
                root.imageViewNew.visibility = View.GONE
                root.imageViewUsed.visibility = View.VISIBLE
            }
            if(it.images?.size!! > 0){
                Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.images?.get(0)?.img}").into(img_picture);
            }else {
                img_picture.setImageResource(android.R.color.transparent)
            }

            text_productName.text = it.productName
            text_price.text = NumberFormat.getNumberInstance().format(it.price) + "원"

            valueMap.put("productId", it.productId.toString())
            valueMap.put("paymentCard", root.spinner_card.selectedItem.toString())
        }

        productViewModel.count.observe(viewLifecycleOwner) {
            text_count.text = it.toString()
            val total = NumberFormat.getNumberInstance().format(productViewModel.selected.value!!.price * it) + "원"
            text_totalPrice.text = total
            text_totalPayment.text = total

            valueMap.put("count", it.toString())
            valueMap.put("pricePerOne", productViewModel.selected.value!!.price.toString())
            valueMap.put("totalPrice", (productViewModel.selected.value!!.price * it).toString())
        }

        return root
    }

    //결제하기 버튼의 리턴 수신.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //"결제하기"버튼 클릭 결과
        if(requestCode == VP_PURCHASE){
            //SSI APP에서 정상적으로 VP를 받지 못함.
            if( resultCode != Activity.RESULT_OK ){
                Toast.makeText(getActivity(), "결재 실패", Toast.LENGTH_SHORT).show()

            }else if( resultCode == Activity.RESULT_OK ) {
                //SSI APP에서 정상적으로 VP 수령.
                val rcvVp: String = data!!.getStringExtra("vp")
                val sendVpMap: Map<*, *> = Gson().fromJson(rcvVp, MutableMap::class.java)
                val sendVp: String = sendVpMap.get("Presentations").toString()
                var result: String = ""

                //결제 진행중 prgress bar
//                paymentDialog = ProgressDialog.show(activity, null, "결제 진행중", true, true)

                //SSI APP으로부터 받은 VP 검증 & PG 승인 요청
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        val map: MutableMap<String, String> = mutableMapOf()
                        map.put("vp", sendVp)
//                        map.put("vp", rcvVp)
                        map.put("uuid", SsiShopApplication.uuid)
                        /**
                         * 현재는(2020.10.06) SSI APP에서 받은 VP를 검증하고 같은 VP를 PG에 verifyvp로 넘기고 있음.
                         * 향후 credential에 따라 vc들이 정해지면, 이들 vc를 metadium sdk에 넣어 vp 생성하여 map.put("vp", vp)로 활용.
                         */
                        val res: ApiResponse<String> = SsiApi.instance.doVerifyingVpForPaying(SsiShopApplication.token, map)

                        if(res.code == 9999) {
                            //VP 검증 실패
                            result = "false"
                        }else{
                            //VP 검증 성공, PG 요청 진행.
                            result = "true"
                        }
                    }

                    if( result == "true" ) {
                        //구매내역 저장
                        val savedDeal: Deal? = doInsertBuyHistory()
//                        paymentDialog.hide()
                        val bundle:Bundle = Bundle()
                        bundle.putString("dealId", savedDeal?.dealId.toString())
                        findNavController().navigate(R.id.action_nav_product_purchase_to_nav_product_complete, bundle)
                    }else{
                        Toast.makeText(activity, "결제 실패", Toast.LENGTH_SHORT).show()
//                        paymentDialog.hide()
                    }
                }

//                productViewModel.doVerifyingVpForPaying(data!!.getStringExtra("vp")){
//                    paymentDialog.hide()
//                    findNavController().navigate(R.id.action_nav_product_purchase_to_nav_product_complete)
//                }
            }
        }
    }

    fun doInsertBuyHistory():Deal?{
        val valueMap:MutableMap<String, String> = mutableMapOf()

        //set viewmodel observer
        productViewModel.selected.observe(viewLifecycleOwner) {
            if(it.images != null && it.images?.size!! > 0){
                Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.images?.get(0)?.img}").into(img_picture);
            }else {
                img_picture.setImageResource(android.R.color.transparent)
            }

            text_productName.text = it.productName
            text_price.text = NumberFormat.getNumberInstance().format(it.price) + "원"

            valueMap.put("buyer", SsiShopApplication.user.username)
            valueMap.put("buyerName", SsiShopApplication.user.name)
            valueMap.put("paymentMethod", SsiShopApplication.paymentMethod)
            valueMap.put("paymentCard", SsiShopApplication.paymentCard)
            valueMap.put("address", SsiShopApplication.typedAddress)
            valueMap.put("phone", SsiShopApplication.typedPhone)
            valueMap.put("did", SsiShopApplication.user.did)
            valueMap.put("didSelected", it.didSelected.toString())
            valueMap.put("productId", it.productId.toString())
            valueMap.put("productName", it.productName)
            valueMap.put("type", it.type)
//            Log.d("### doHisIns type", it.didSelected)
        }

        productViewModel.count.observe(viewLifecycleOwner) {
            text_count.text = it.toString()+"개"
            val total = NumberFormat.getNumberInstance().format(productViewModel.selected.value!!.price * it) + "원"
            text_totalPrice.text = total
            text_totalPayment.text = total

            valueMap.put("count", it.toString())
            valueMap.put("pricePerOne", productViewModel.selected.value!!.price.toString())
            valueMap.put("totalPrice", (productViewModel.selected.value!!.price * it).toString())
            valueMap.put("birth", SsiShopApplication.user.birthDate.toString())
        }

        val res: ApiResponse<Deal> = runBlocking {
            SsiApi.instance.addBuying(SsiShopApplication.token, valueMap)
        }

        return res.data
    }
}