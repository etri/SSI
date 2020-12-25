package com.bd.ssishop.market.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.ssishop.R
import kotlinx.android.synthetic.main.item_banner_view.view.*

class ProductBannerAdapter(private val banners:ArrayList<ProductBanner>): RecyclerView.Adapter<Holder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.item_banner_view, parent, false)
//        val inflateView = inflater.inflate(R.layout.fragment_product_list, container, false)
        return Holder(inflateView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val banner = banners[position]
        holder.setImg(banner)
    }

    override fun getItemCount(): Int {
        return banners.size
    }
}

class Holder(itemView: View):RecyclerView.ViewHolder(itemView){
    fun setImg(banner:ProductBanner){
        itemView.imgvw_banner.setImageDrawable(banner.imgBanner)
    }
}