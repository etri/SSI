package com.bd.ssishop.market.product

/**
 * 상품 데이터 클래스
 */
data class Product(val productId: Int) {
    lateinit var productName: String
    lateinit var description: String
    var price: Long = 0
    lateinit var type: String
    var did: String? = null
    var didSelected: String? = null
    var address: String? = null
    lateinit var createDate: String
    lateinit var createUser: String
    var productDid: String? = null
    var images: List<ProductImage>? = null
    var usedProductId: Int= -1
    var buyCnt:Int = 0
    var reviewCnt:Int = 0
    var img: String? = null
    var imgCopy:String? = null
}