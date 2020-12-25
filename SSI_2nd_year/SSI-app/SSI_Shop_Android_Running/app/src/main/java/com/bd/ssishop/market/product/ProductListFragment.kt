package com.bd.ssishop.market.product

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.market.MarketActivity
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import kotlinx.android.synthetic.main.fragment_product_list.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*


/**
 * 상품 목록 프래그먼트
 */
class ProductListFragment : Fragment() {

//    private lateinit var productViewModel: ProductViewModel
    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_product_list, container, false)

        //set viewmodel observer
        productViewModel.productList.observe(viewLifecycleOwner, Observer {
            //set new Adapter to RecyclerView
            recycler_product.adapter = ProductAdapter(it) {
                productViewModel.select(it)
                findNavController().navigate(R.id.nav_product_detail)
            }
        })

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.VISIBLE)
        (activity as MarketActivity).toolbar_title.setText(R.string.menu_product)
        val typeface = this.context?.let { ResourcesCompat.getFont(it, R.font.tangerine_bold) }
        (activity as MarketActivity).toolbar_title.setTypeface(typeface, Typeface.BOLD)
        (activity as MarketActivity).toolbar_title.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            40.toFloat()
        )

        //load products
        productViewModel.loadProducts("") {
            Toast.makeText(activity, "상품 로딩 실패", Toast.LENGTH_SHORT).show()
        }

        //set LayoutManager to RecyclerView
        root.recycler_product.apply {
            layoutManager = LinearLayoutManager(activity)
            val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            decoration.setDrawable(context.getDrawable(R.drawable.divider_product)!!)
            addItemDecoration(decoration)
        }

        //상품 목록 하단 배너
        val bannerImgs = ArrayList<ProductBanner>()
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr01, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr02, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr03, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr04, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr05, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr06, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr07, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr08, null)))
        bannerImgs.add(ProductBanner(resources.getDrawable(R.drawable.img_bnr09, null)))

        val rcrBanner = root.findViewById(R.id.rcrBanner) as RecyclerView
        val layout = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rcrBanner.layoutManager = layout
        val adapter = ProductBannerAdapter(bannerImgs)
        rcrBanner.adapter = adapter

        //상품 목록 하단 좌/우 버튼
        val imgbtn_left = root.findViewById(R.id.imgbtn_left) as ImageButton
        val imgbtn_right = root.findViewById(R.id.imgbtn_right) as ImageButton
        imgbtn_left.setOnClickListener{
            val lastImg = layout.findLastVisibleItemPosition()

            if(lastImg < adapter.itemCount){
                layout.scrollToPosition(lastImg + 1)
            }
        }
        imgbtn_right.setOnClickListener{
            val firstImg = layout.findFirstVisibleItemPosition()

            if(firstImg > 0){
                layout.scrollToPosition(firstImg - 1)
            }
        }


//        root.edit_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String): Boolean {
//                Log.d("### TypedText", query)
//
//                productViewModel.loadProducts(query) {
//                    Toast.makeText(activity, "상품 로딩 실패", Toast.LENGTH_SHORT).show()
//                }
//
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                Log.d("### TextChged", newText)
//                return false
//            }
//        })

        root.edit_search.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
//                Log.d("### PressKey", "ENTER")
                productViewModel.loadProducts(edit_search.text.toString()) {
                    Toast.makeText(activity, "상품 로딩 실패", Toast.LENGTH_SHORT).show()
                }

                val imm = (activity as MarketActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edit_search.getWindowToken(), 0)

                return@OnKeyListener true
            } else {
                false
            }
        })

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val navController = Navigation.findNavController(root)
                navController.navigate(R.id.nav_product_list)
            }
        })

        return root
    }
}