package com.bd.ssishop.market

class ProductNDeal {
    //Product
    val productId: Int = 0
    val productName: String = ""
    val description: String = ""
    val price: Long = 0
    val type: String = ""
    val did: String = ""
    val didSelected: String = ""
    val address: String = ""
    val createDate: String = ""
    val createUser: String = ""
    val productDid: String = ""
    val manufacturerDid: String = ""
    val manufacturer: String = ""
    val madeDate: String = ""
    val serialNum: String = ""
    val productWallet: String = ""
    val manufacturerWallet: String = ""
    val images: String = ""
    val usedProductId: Int= 0

    //Deal
    val dealId: Int = 0
    val productIdOnDeal: Int = 0
    val buyer: String = ""
    val buyerName: String = ""
    val request: String = ""
    val paymentMethod: String = ""
    val state: String = ""
    val dealDate: String = ""
    val count: Int = 0
    val pricePerOne: String = ""
    val totalPrice: String = ""
    val addressOfDeal: String = ""
    val phone: String = ""
    val publicKey: String = ""
    val didOfDeal: String = ""
    val didSeller: String = ""
    val didSelectedOfDeal: String = ""
    val didBuyerOfDeal: String = ""
    val vcState: String = ""
    val buyNSell: String = ""
    val etc:String = ""
    val sign: String = ""

    //Product_img
    val id: String = ""
    val productIdOfImg: String = ""
    val img: String = ""

    override fun toString(): String {
        return "ProductNDeal(productId=$productId, productName='$productName', description='$description', price=$price, type='$type', did='$did', didSelected='$didSelected', address='$address', createDate='$createDate', createUser='$createUser', productDid='$productDid', manufacturerDid='$manufacturerDid', manufacturer='$manufacturer', madeDate='$madeDate', serialNum='$serialNum', productWallet='$productWallet', manufacturerWallet='$manufacturerWallet', images='$images', usedProductId=$usedProductId, dealId=$dealId, productIdOnDeal=$productIdOnDeal, buyer='$buyer', buyerName='$buyerName', request='$request', paymentMethod='$paymentMethod', state='$state', dealDate='$dealDate', count=$count, pricePerOne='$pricePerOne', totalPrice='$totalPrice', addressOfDeal='$addressOfDeal', phone='$phone', publicKey='$publicKey', didOfDeal='$didOfDeal', didSeller='$didSeller', didSelectedOfDeal='$didSelectedOfDeal', didBuyerOfDeal='$didBuyerOfDeal', vcState='$vcState', buyNSell='$buyNSell', etc='$etc', sign='$sign', id='$id', productIdOfImg='$productIdOfImg', img='$img')"
    }
}