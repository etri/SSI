package com.bd.ssishop.market.product

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.market.deal.Deal
import com.bd.ssishop.market.deal.DealViewModel
import com.bd.ssishop.service.SsiHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_product_complete.*
import kotlinx.android.synthetic.main.fragment_product_complete.view.*
import kotlinx.android.synthetic.main.fragment_product_purchase.*
import kotlinx.android.synthetic.main.fragment_product_purchase.img_picture
import kotlinx.android.synthetic.main.fragment_product_purchase.text_count
import kotlinx.android.synthetic.main.fragment_product_purchase.text_price
import kotlinx.android.synthetic.main.fragment_product_purchase.text_productName
import kotlinx.android.synthetic.main.fragment_product_purchase.text_totalPayment
import kotlinx.android.synthetic.main.fragment_product_purchase.text_totalPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.text.NumberFormat

/**
 * 상품 구매완료 프래그먼트
 */
class ProductCompleteFragment : Fragment() {

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onActivityCreated(savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_product_complete, container, false)
        val valueMap:MutableMap<String, String> = mutableMapOf()
        val dealId:String? = arguments?.getString("dealId")

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.fragment_product_complete)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.toFloat())

        //set viewmodel observer
        productViewModel.selected.observe(viewLifecycleOwner) {
            if(it.images != null && it.images?.size!! > 0)
                Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.images?.get(0)?.img}").into(img_picture);
            else
                img_picture.setImageResource(android.R.color.transparent)

            text_productName.text = it.productName
            text_price.text = NumberFormat.getNumberInstance().format(it.price) + "원"

            valueMap.put("did", it.did.toString())
            valueMap.put("productId", it.productId.toString())
            valueMap.put("productName", it.productName.toString())
        }

        productViewModel.count.observe(viewLifecycleOwner) {
            text_count.text = it.toString()+"개"
            val total = NumberFormat.getNumberInstance().format(productViewModel.selected.value!!.price * it) + "원"
            text_totalPrice.text = total
            text_totalPayment.text = total

            valueMap.put("count", it.toString())
            valueMap.put("totalPrice", (productViewModel.selected.value!!.price * it).toString())
        }

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                valueMap.put("dealId", dealId!!)
                val res = SsiApi.instance.sendPush(SsiShopApplication.token, valueMap)
            }
        }

        root.btn_ok.setOnClickListener{
            val navController = Navigation.findNavController(root)
            navController.navigate(R.id.nav_product_list)
        }

//        Log.d("#### SAVE QQQ111", "")
//        GlobalScope.launch(Dispatchers.Main) {
//            Log.d("#### SAVE QQQ222", "")
//            withContext(Dispatchers.IO) {
//                Log.d("#### SAVE QQQ", "")
//                val res = SsiApi.instance.addBuying(SsiShopApplication.token, valueMap)
//            }
//        }

        return root
    }
}