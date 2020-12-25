package com.bd.ssishop.market.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bd.ssishop.R
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.market.MarketActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_register_complete.*
import kotlinx.android.synthetic.main.fragment_register_complete.view.*
import java.text.NumberFormat

/**
 * 상품등록 완료 프래그먼트
 */
class RegisterCompleteFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_register_complete, container, false)
        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.fragment_register_complete)

        var productType:String = ""

        registerViewModel.selected.observe(viewLifecycleOwner) {
            productType = it.type
            text_product_id.text = "상품번호 : ${it.productId}"
            text_product_name.text = it.productName

            if(it.images?.size!! > 0)
                Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.images?.get(0)?.img}").into(img_picture);
            else
                img_picture.setImageResource(android.R.color.transparent)

            text_price.text = NumberFormat.getNumberInstance().format(it.price) + "원"
            text_description.text = it.description
        }


        root.btn_navigate_deal.setOnClickListener {
            val bundle:Bundle = Bundle()
            if(productType == "중고품") {
                bundle.putString("tabIndex", "1")
            }else{
                bundle.putString("tabIndex", "0")
            }
            findNavController().navigate(R.id.nav_deal_list, bundle)
        }

        return root
    }
}