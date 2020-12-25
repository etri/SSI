package com.bd.ssishop.login

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import androidx.core.content.res.ComplexColorCompat.inflate
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.api.request.LoginRequest
import com.bd.ssishop.api.response.LoginResponse
import com.bd.ssishop.data.*
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.service.SsiFcmService
import com.bd.ssishop.service.SsiHelper.Companion.VC_LOGIN
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_progress.*
import kotlinx.coroutines.*
import java.util.*

/**
 * 로그인 액티비티
 */
class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_vcUser.setOnClickListener{
            loginVcUserRequest("user1", "user1")
        }

        createNotificationChannel()
    }


    /**
     * ##NEW## VC 유저 자동 로그인
     */
    @SuppressLint("HardwareIds")
    fun loginVcUserRequest(username: String, password: String){
        GlobalScope.launch(Dispatchers.Main) {
            val progDialog = ProgressDialog(this@LoginActivity)
            progDialog.setCancelable(false)
            progDialog.setMessage("진행중...")
            progDialog.show()

            layout_loginActivity.isClickable = false;

            /*
             * 기존 수동 로그인 로직. jwt 유지
             */
            val request = LoginRequest(username, password)
            val response: ApiResponse<LoginResponse>
            withContext(Dispatchers.IO) {
                response = SsiApi.instance.login(request)
            }
            if (response.code == 1000) {
                SsiShopApplication.token = response.data!!.token
                SsiShopApplication.user = response.data!!.user
            }

            /*
             * VC로그인을 위해 SSI APP에 보낼 VC string 생성.
             */
            val uuid = SsiShopApplication.uuid
//            val preUserVcs: PreData_UserVC = PreData_UserVC("did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1")
            val valueMap: MutableMap<String, String> = mutableMapOf()
            valueMap.put("USER_DID", SsiShopApplication.user.did)
            valueMap.put("ISSUER_DID", Issuer.ISSUER_DID)
            valueMap.put("ISSUE_TIME", "")
            valueMap.put("UUID", "")
            valueMap.put("URL", "")
            val vcsTemp = VCsTemplate(valueMap)
            val sendVCs: String = vcsTemp.getSendVcsForLogin()

            /*
             * VC string을 SSI APP에 전송
             */
            val ssi_url = "ssi://requestVp?requestVP=${sendVCs}"
            val pi = Intent(Intent.ACTION_VIEW, Uri.parse(ssi_url))
            startActivityForResult(pi, VC_LOGIN)
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            progDialog.dismiss()
        }
    }

    /**
     * ##NEW## VC 유저 자동 로그인. SSI APP에서 리턴하는 VP 수신 후 서버로 검증 요청.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val progDialog = ProgressDialog(this)
        progDialog.setCancelable(false)
        progDialog.setMessage("진행중...")
        progDialog.show()

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        layout_loginActivity.isClickable = false;

        if(requestCode == VC_LOGIN && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch(Dispatchers.Main) {
                //SSI App에서 RESULT_OK와 함께 intent에 "vp"로 보내주는 값 수신.
                val rcvVp: String = data!!.getStringExtra("vp")
                val sendVpMap: Map<*, *> = Gson().fromJson(rcvVp, Map::class.java)
                val sendVp: String = sendVpMap.get("Presentations").toString()

                val sendVpSplit = sendVp.split(".")
                val presentationDec = String(Base64.getDecoder().decode(sendVpSplit[1]))
                val gson: Gson = Gson()
                var ltVO: LoginTypeVO  = gson.fromJson(presentationDec, LoginTypeVO::class.java)
                val credentialSplit = ltVO.credential[0].split(".")
                val credentialDec = String(Base64.getDecoder().decode(credentialSplit[1]))
                val ltcVO: LoginTypeCredentialVO = gson.fromJson(credentialDec, LoginTypeCredentialVO::class.java)
                val user: User = User(SsiShopApplication.uuid, ltcVO.sub, SsiShopApplication.user.username, SsiShopApplication.user.password, ltcVO.claim.name, ltcVO.claim.phone_num, ltcVO.claim.address, "USER", ltcVO.claim.birthDate)
                SsiShopApplication.user = user;

                /*
                 * SSI App에서 수신한 vp를 서버로 보내 metadium SDK로 vp 검증. 정상 검증인 경우 MarketActivity 실행.
                 */
                withContext(Dispatchers.IO) {
                    val paramMap:MutableMap<String, String> = mutableMapOf()
                    paramMap.put("sendVp", sendVp)
                    paramMap.put("did", ltcVO.sub)
                    val ssiFcmService:SsiFcmService = SsiFcmService()
                    paramMap.put("token", ssiFcmService.doCheckFcmToken())
                    var res: ApiResponse<String> = SsiApi.instance.doVerifyingVpForVcLogin(SsiShopApplication.token, paramMap)

                    if(res.code != 9999) {
                        SsiShopApplication.runMode = "NORMAL"
                        val intent = Intent(applicationContext, MarketActivity::class.java)
                        startActivity(intent)
                    }

                    Thread.sleep(2000)
                    progDialog.dismiss()
                }
            }
        }else{
            progDialog.dismiss()
            Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
        }
    }



    /**
     * 기존 ID자동 입력 로그인  API 호출
     */
    fun loginRequest(username: String, password: String){
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val request = LoginRequest(username, password)
                val response: ApiResponse<LoginResponse>
                withContext(Dispatchers.IO){
                    response = SsiApi.instance.login(request)
                }
                if(response.code == 1000){
                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                    SsiShopApplication.token = response.data!!.token
                    SsiShopApplication.user = response.data!!.user
                    val intent = Intent(this@LoginActivity, MarketActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "타임 아웃", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Notification을 받기 위한 채널 설정
     */
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SSIAPP"
            val descriptionText = name
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(name, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}