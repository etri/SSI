package com.bd.ssishop.market.deal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.ssishop.R
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.market.ProductNDeal
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.info_delivery_frame.view.*
import kotlinx.android.synthetic.main.listitem_deal_buy.view.*
import kotlinx.android.synthetic.main.listitem_deal_sell.view.*
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_count
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_date
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_dealId
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_price
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_productName
import kotlinx.android.synthetic.main.listitem_deal_sell.view.text_state
import java.text.NumberFormat

/**
 * 판매내역 리사이클러뷰 어댑터
 */
class SellListAdapter(private val deals: List<ProductNDeal>, val onItemClickListener: (ProductNDeal) -> Unit): RecyclerView.Adapter<SellListAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    inner class ViewHolder(val layout: ViewGroup): RecyclerView.ViewHolder(layout) {
        init {
            layout.setOnClickListener {
                onItemClickListener(deals[adapterPosition])
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_deal_sell, parent, false) as ViewGroup

        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(layout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(deals[position].img != null && deals[position].img.length > 0){
            Glide
                .with(holder.layout.context)
                .load("${ApiGenerator.IMAGE_URL}${deals[position].img}")
                .centerCrop()
                .into(holder.layout.imgvw_listitem_deal_sell)
        } else {
            holder.layout.imgvw_listitem_deal_sell.setImageResource(android.R.color.transparent)
        }

//        Log.d("### deal.str", deals[position].toString())
//        Log.d("### deal stat", deals[position].state)

        holder.layout.text_state.text = deals[position].state
        holder.layout.text_date.text = deals[position].dealDate
        holder.layout.text_productName.text = deals[position].productName
        holder.layout.text_dealId.text = deals[position].productId.toString()
        if(deals[position].count == 0) {
            holder.layout.text_price.text = NumberFormat.getNumberInstance().format(deals[position].price) + " 원"
        }else{
            holder.layout.text_price.text = NumberFormat.getNumberInstance().format(deals[position].price * deals[position].count) + " 원"
        }
        holder.layout.text_count.text = deals[position].count.toString()

        if(deals[position].type == "새제품"){
            holder.layout.imgvw_listitem_deal_sell_New.visibility = View.VISIBLE
            holder.layout.imgvw_listitem_deal_sell_Used.visibility = View.GONE
        }else{
            holder.layout.imgvw_listitem_deal_sell_New.visibility = View.GONE
            holder.layout.imgvw_listitem_deal_sell_Used.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return deals.size
    }
}
