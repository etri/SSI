package com.bd.ssishop.data

import com.bd.ssishop.market.product.Product

data class SpnrUsedListData(
    val idx:Int,
    val productId:Int,
    val productName:String,
    val price:Int,
    val description:String,
    val img:String?,
    val product: Product
)