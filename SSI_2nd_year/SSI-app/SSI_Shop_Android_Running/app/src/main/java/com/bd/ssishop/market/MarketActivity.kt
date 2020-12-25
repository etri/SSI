package com.bd.ssishop.market

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.api.request.TokenRequest
import com.bd.ssishop.data.LoginTypeCredentialVO
import com.bd.ssishop.data.LoginTypeVO
import com.bd.ssishop.login.User
import com.bd.ssishop.service.SsiHelper
import com.bd.ssishop.service.SsiHelper.Companion.VC_LOGIN
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_market.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


/**
 * 고가품 거래마켓 메인 액티비티
 */
class MarketActivity: AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        nav_view.getHeaderView(0).text_name.text = SsiShopApplication.user.name

        nav_view.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_init ->{
                    true
                }else -> false
            }
        }

        //floating action button
        fab.setOnClickListener {
        }

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_product_list,
                R.id.nav_deal_list,
                R.id.nav_register,
                R.id.nav_init
            ), drawer_layout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("firebase", "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }

            //get new token
            val token = task.result?.token
            Log.i("firebase", "token: $token")

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val request = TokenRequest(token)
                    val response: ApiResponse<String>
                    withContext(Dispatchers.IO) {
                        response = SsiApi.instance.token(SsiShopApplication.token, request)
                    }
                    if (response.code == 1000) {
                        Log.i("firebase", "token update success")
                    }
                } catch (e: Exception) {
                }
            }

//            viewModelScope.launch{
//                val request = PurchaseRequest(selected.value!!.productId, paymentMethod, getCount(), phone, address)
//                val response = SsiApi.instance.purchase(SsiShopApplication.token, request)
//                withContext(Dispatchers.Main){
//                    if(response.code == 1000){
//                        response.data?.let { onSuccess(it) }
//                    }
//                }
//            }

        })

        //ToolBar 오른쪽 Icon 2개 세팅. 1번째 icon은 "My". 구매내역/판매로 이동. 2번째 "Sell"은 상품등록으로 이동
        val appBarIcon1: ImageView = this.app_bar_icon01
        val appBarIcon2: ImageView = this.app_bar_icon02
        appBarIcon1.setOnClickListener{
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.nav_deal_list)
        }
        appBarIcon2.setOnClickListener{
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.nav_register)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}