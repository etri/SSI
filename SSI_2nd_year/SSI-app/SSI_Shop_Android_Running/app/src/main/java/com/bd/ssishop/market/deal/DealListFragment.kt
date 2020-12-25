package com.bd.ssishop.market.deal

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bd.ssishop.R
import com.bd.ssishop.market.MarketActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_deal_list.view.*
import kotlinx.android.synthetic.main.listitem_deal_buy.*
import kotlinx.android.synthetic.main.tab_item_deal.view.*

/**
 * 거래내역 목록 프래그먼트
 */
class DealListFragment : Fragment() {
    private lateinit var dealListPagerAdapter: DealListPagerAdapter

    private val dealViewModel: DealViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_deal_list, container, false)

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.menu_deal)
        (activity as MarketActivity).toolbar_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.toFloat())

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val navController = Navigation.findNavController(root)
                navController.navigate(R.id.nav_product_list)
            }
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabIndex:String? = arguments?.getString("tabIndex")

        //페이저(탭) 설정
        dealListPagerAdapter = DealListPagerAdapter(childFragmentManager)
        view.pager.adapter = dealListPagerAdapter
        view.tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view.pager))
        view.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(view.tabs))
        if (tabIndex != null) {
            view.tabs.getTabAt(tabIndex.toInt())?.select()
        }
    }
}

/**
 * Tab 구성을 위한 페이저 어뎁터
 */
// Instances of this class are fragments representing a single
// object in our collection.
class DealListPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 2

    override fun getItem(i: Int): Fragment {
        return if( i == 0) {
            BuyListObjectFragment()
        } else {
            SellListObjectFragment()
        }
    }
}

/**
 * 구매내역목록 프래그먼트
 */
class BuyListObjectFragment : Fragment() {
    private val dealViewModel: DealViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.tab_item_deal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dealViewModel.buyList.observe(viewLifecycleOwner, Observer {
            view.recycler_deal.adapter = BuyListAdapter(it){
                dealViewModel.selectBL(it)
                dealViewModel.setTab("buy")
                findNavController().navigate(R.id.nav_deal_detail)
            }
        })

        dealViewModel.loadBuyList {
            Toast.makeText(activity, "구매내역 로딩 실패", Toast.LENGTH_SHORT).show()
        }

        //set LayoutManager to RecyclerView
        view.recycler_deal.apply {
            layoutManager = LinearLayoutManager(activity)
            val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            decoration.setDrawable(context.getDrawable(R.drawable.divider_product)!!)
            addItemDecoration(decoration)
        }
    }
}

/**
 * 판매내역 목록 프래그먼트
 */
class SellListObjectFragment : Fragment() {
    private val dealViewModel: DealViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.tab_item_deal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dealViewModel.sellList.observe(viewLifecycleOwner, Observer {
            view.recycler_deal.adapter = SellListAdapter(it){
                dealViewModel.selectPD(it)
                dealViewModel.setTab("sell")
                findNavController().navigate(R.id.nav_deal_detail)
            }
        })

        dealViewModel.loadSellList {
            Toast.makeText(activity, "판매내역 로딩 실패", Toast.LENGTH_SHORT).show()
        }

        view.recycler_deal.apply {
            layoutManager = LinearLayoutManager(activity)
            val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            decoration.setDrawable(context.getDrawable(R.drawable.divider_product)!!)
            addItemDecoration(decoration)
        }
    }
}
