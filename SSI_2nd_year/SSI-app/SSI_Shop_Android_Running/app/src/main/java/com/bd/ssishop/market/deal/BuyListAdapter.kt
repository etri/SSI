package com.bd.ssishop.market.deal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.ssishop.R
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.market.BuyListVO
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.listitem_deal_buy.view.*
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_count
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_date
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_dealId
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_price
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_productName
import kotlinx.android.synthetic.main.listitem_deal_buy.view.text_state
import kotlinx.android.synthetic.main.listitem_deal_sell.view.*
import kotlinx.android.synthetic.main.listitem_product.view.*
import java.text.NumberFormat

/**
 * 구매내역 리사이클러뷰 어댑터
 */
class BuyListAdapter(private val buys: List<BuyListVO>, val onItemClickListener: (BuyListVO) -> Unit): RecyclerView.Adapter<BuyListAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    inner class ViewHolder(val layout: ViewGroup): RecyclerView.ViewHolder(layout) {
        init {
            layout.setOnClickListener {
                onItemClickListener(buys[adapterPosition])
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_deal_buy, parent, false) as ViewGroup

        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(layout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        Log.d("### 구매상세 deals.str", deals[position].toString())
        if(buys[position].img != null && buys[position].img.length > 0){
            Glide
                .with(holder.layout.context)
                .load("${ApiGenerator.IMAGE_URL}${buys[position].img}")
                .centerCrop()
                .into(holder.layout.imgvw_listitem_deal_buy)
        } else {
            holder.layout.imgvw_listitem_deal_buy.setImageResource(android.R.color.transparent)
        }
        holder.layout.text_state.text = buys[position].state
        holder.layout.text_date.text = buys[position].dealDate
        holder.layout.text_productName.text = buys[position].productName
        holder.layout.text_dealId.text = buys[position].dealId.toString()
        if(buys[position].count == 0) {
            holder.layout.text_price.text = NumberFormat.getNumberInstance().format(buys[position].price) + " 원"
        }else{
            holder.layout.text_price.text = NumberFormat.getNumberInstance().format(buys[position].price * buys[position].count) + " 원"
        }
        holder.layout.text_count.text = buys[position].count.toString()

        if(buys[position].type == "새제품"){
            holder.layout.imgvw_listitem_deal_buy_New.visibility = View.VISIBLE
            holder.layout.imgvw_listitem_deal_buy_Used.visibility = View.GONE
        }else{
            holder.layout.imgvw_listitem_deal_buy_New.visibility = View.GONE
            holder.layout.imgvw_listitem_deal_buy_Used.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return buys.size
    }
}
