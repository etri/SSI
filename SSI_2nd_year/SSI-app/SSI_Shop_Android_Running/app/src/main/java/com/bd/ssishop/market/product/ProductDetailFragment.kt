package com.bd.ssishop.market.product

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
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
import kotlinx.android.synthetic.main.fragment_product_detail.*
import kotlinx.android.synthetic.main.fragment_product_detail.view.*
import java.text.NumberFormat

/**
 * 상품 상세화면 프래그먼트
 */
class ProductDetailFragment : Fragment() {

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_product_detail, container, false)

        //set title
//        (activity as MarketActivity).img_icon_toolbar.visibility = View.GONE
        (activity as MarketActivity).toolbar_title.setText(R.string.fragment_product_detail)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.toFloat())

        //set viewmodel observer
        productViewModel.selected.observe(viewLifecycleOwner) {
            if(it.images?.size!! > 0)
                Glide.with(requireActivity()).load("${ApiGenerator.IMAGE_URL}${it.images?.get(0)?.img}").into(img_picture);
            else
                img_picture.setImageResource(android.R.color.transparent)

            when(it.type){
                "새제품" -> {
                    image_new.visibility = View.VISIBLE
                    image_used.visibility = View.GONE
                }
                "중고품" -> {
                    image_new.visibility = View.GONE
                    image_used.visibility = View.VISIBLE
                }
                else -> image_new.visibility = View.GONE
            }

            text_productName.text = it.productName
            text_price.text = NumberFormat.getNumberInstance().format(it.price) + " 원"
            text_description.text = it.description
            text_description.setMovementMethod(ScrollingMovementMethod.getInstance())

            var rnds = (1..100).random()
            tx_buyCnt.text = it.buyCnt.toString()
            tx_reviewCnt.text = it.reviewCnt.toString()
        }

        root.btn_buy.setOnClickListener {
//            productViewModel.setCount( text_count.text.toString().toInt() )
            productViewModel.setCount(1)
            findNavController().navigate(R.id.nav_product_purchase)
        }

        return root
    }
}