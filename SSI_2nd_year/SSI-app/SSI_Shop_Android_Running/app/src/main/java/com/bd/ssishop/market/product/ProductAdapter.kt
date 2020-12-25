package com.bd.ssishop.market.product

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bd.ssishop.R
import com.bd.ssishop.api.ApiGenerator
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.listitem_product.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 상품 리사이클러뷰 어댑터
 */
class ProductAdapter(private val products: List<Product>, val onItemClickListener: (Product) -> Unit): RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable {
    var countryFilterList = products

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    inner class ViewHolder(val layout: ViewGroup): RecyclerView.ViewHolder(layout) {
        init {
            layout.setOnClickListener {
                onItemClickListener(products[adapterPosition])
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_product, parent, false) as ViewGroup

        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(layout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

//        var product:Product = products[position]
        var product:Product = countryFilterList[position]

        if(product.images?.size!! > 0){
            Glide
                .with(holder.layout.context)
                .load("${ApiGenerator.IMAGE_URL}${product.images?.get(0)?.img}")
                .centerCrop()
                .into(holder.layout.imageView)
        } else {
            holder.layout.imageView.setImageResource(android.R.color.transparent)
        }

        when(product.type){
            "새제품" -> {
                holder.layout.image_new.visibility = View.VISIBLE
                holder.layout.image_used.visibility = View.GONE
            }
            "중고품" -> {
                holder.layout.image_new.visibility = View.GONE
                holder.layout.image_used.visibility = View.VISIBLE
            }
            else -> {
                holder.layout.image_new.visibility = View.GONE
                holder.layout.image_used.visibility = View.GONE
            }
        }

        var productTitle = product.productName
        val sb = StringBuilder()
        var loopCnt = 0
        for(one in productTitle){
            sb.append(one)
            if( loopCnt > 18 ){
                sb.append("...")
                break;
            }
            loopCnt++;
        }
        val titles = sb.toString().trim()
        holder.layout.text_productName.text = titles
        holder.layout.text_price.text = NumberFormat.getNumberInstance().format(products[position].price) + " 원"
        holder.layout.tx_buyCnt.text = products[position].buyCnt.toString()
        holder.layout.tx_reviewCnt.text = products[position].reviewCnt.toString()
    }

    override fun getItemCount(): Int {
//        return products.size
        return countryFilterList.size
    }

    override fun getFilter(): Filter {
        return object:Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    countryFilterList = products
                } else {
                    val resultList = ArrayList<Product>()
                    for (row in products) {
                        if (row.productName.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    countryFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = countryFilterList

                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                countryFilterList = results?.values as ArrayList<Product>
                notifyDataSetChanged()
            }
        }

    }
}
