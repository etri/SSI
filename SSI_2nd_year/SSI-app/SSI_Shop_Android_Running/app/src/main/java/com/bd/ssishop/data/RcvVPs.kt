package com.bd.ssishop.data

data class RcvVPs(val init: String?) {
    var phoneId: String = ""        //phone uuid
    var username: String = ""       //ID
    var name : String = ""          //사용자 이름
    var vc: String = ""             //검증된 vc
    var vp : String = ""            //검증된 vp

    val test: String = ""

}