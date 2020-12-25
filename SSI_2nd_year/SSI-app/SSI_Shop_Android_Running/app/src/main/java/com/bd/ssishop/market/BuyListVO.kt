package com.bd.ssishop.market

class BuyListVO {
    //Product
    var productId: String = ""
    var productName: String = ""
    var description: String = ""
    var price: Int = 0
    var type: String = ""
    var did: String = ""
    var didSelected: String = ""
    var address: String = ""
    var createDate: String = ""
    var createUser: String = ""
    var productDid: String = ""
    var manufacturerDid: String = ""
    var manufacturer: String = ""
    var madeDate: String = ""
    var serialNum: String = ""
    var images: String = ""
    var usedProductId: String = ""

    //Deal
    var dealId: Int = 0
    var productIdOnDeal: String = ""
    var buyer: String = ""
    var buyerName: String = ""
    var request: String = ""
    var paymentMethod: String = ""
    var state: String = ""
    var dealDate: String = ""
    var count: Int = 0
    var pricePerOne: String = ""
    var totalPrice: String = ""
    var addressOfDeal: String = ""
    var phone: String = ""
    var publicKey: String = ""
    var didOfDeal: String = ""
    var didSeller: String = ""
    var didSelectedOfDeal: String = ""
    var vcState: String = ""
    var buyNSell: String = ""
    var etc: String = ""
    var sign: String = ""

    //Product_img
    var id: String = ""
    var productIdOfImg: String = ""
    var img: String = ""

    //PushMngr
    val didPush: String = ""
    val token: String = ""
    val create_date: String = ""
    val update_date: String = ""

    override fun toString(): String {
        return "BuyListVO(productId='$productId', productName='$productName', description='$description', price=$price, type='$type', did='$did', didSelected='$didSelected', address='$address', createDate='$createDate', createUser='$createUser', productDid='$productDid', manufacturerDid='$manufacturerDid', manufacturer='$manufacturer', madeDate='$madeDate', serialNum='$serialNum', images='$images', usedProductId='$usedProductId', dealId=$dealId, productIdOnDeal='$productIdOnDeal', buyer='$buyer', buyerName='$buyerName', request='$request', paymentMethod='$paymentMethod', state='$state', dealDate='$dealDate', count=$count, pricePerOne='$pricePerOne', totalPrice='$totalPrice', addressOfDeal='$addressOfDeal', phone='$phone', publicKey='$publicKey', didOfDeal='$didOfDeal', didSeller='$didSeller', didSelectedOfDeal='$didSelectedOfDeal', vcState='$vcState', buyNSell='$buyNSell', etc='$etc', sign='$sign', id='$id', productIdOfImg='$productIdOfImg', img='$img', didPush='$didPush', token='$token', create_date='$create_date', update_date='$update_date')"
    }
}
