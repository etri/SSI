package com.bd.ssishop.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.login.LoginActivity
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.market.deal.WarrantyDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SsiFcmService: FirebaseMessagingService() {
    val TAG = "SsiFcmService"

    lateinit var dialog: WarrantyDialog

    override fun onNewToken(token: String) {
        Log.i(TAG, "ref.... Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var msgMap:MutableMap<String, String> = mutableMapOf()

        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(remoteMessage.data.get("title").toString(), remoteMessage.data.get("body").toString(), remoteMessage.data.get("link").toString())
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification("고가품APP", it.body.toString(), it.link.toString())
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     * @param messageBody FCM message body received.
     */
    @SuppressLint("RestrictedApi")
    private fun sendNotification(msgTitle: String, msgBody: String, msgLink:String) {
//        val firebasePush = FirebasePush("AIzaSyAx9FclsYPspb_-FfT2pGqlNWU4GJ4kt80Y")
//        firebasePush.setNotification(RemoteMessage.Notification())
//        firebasePush.sendToTopic("news")

        val channelId = "SSIAPP"
        val title = msgTitle
        val content = msgBody

//        createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_DEFAULT, false, channelId, "App notification channel")

        val intent = Intent(baseContext, MarketActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val builder = NotificationCompat.Builder(this, channelId)
        val uri = Uri.parse(msgLink)
        val step = uri.getQueryParameter("step")
        val parsedDealId = uri.getQueryParameter("dealId")
        val bundle = Bundle()
        bundle.putString("step", step)
        bundle.putString("dealId", parsedDealId)
        if(parsedDealId != null) {
            SsiShopApplication.parsedDealId = parsedDealId
        }

        var pendingIntent: PendingIntent
        var target_id = -9
        if( SsiShopApplication.runMode == "NORMAL" ) {
            //화면 깜박임 때문에 4번 케이스가 아닌 경우, 혹은 SSI APP호출이 필요없는 경우 여기서 바로 pushEnd로
            when (step) {
                "pub_cert" -> {
                    bundle.putString("step", step)
                    target_id = R.id.nav_push_detail_deal
                }
                else -> {
                    target_id = R.id.nav_pushend_detail_deal
                }
            }

//        val pendingIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(MarketActivity::class.java)
                .setGraph(R.navigation.market_navigation)
                .setDestination(target_id)
                .setArguments(bundle)
                .createPendingIntent()
        }else{
            pendingIntent = getActivity(this, 0, Intent(baseContext, LoginActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.ic_label_hot)
        builder.setContentTitle(title)
        builder.setContentText(content)
        if( Build.VERSION.SDK_INT < 26) {
            builder.priority = NotificationCompat.PRIORITY_HIGH
        }else {
            builder.priority = NotificationManager.IMPORTANCE_HIGH
        }
//        Log.d("### prioty", ",   a=${builder.priority}   b=${Build.VERSION.SDK_INT},   c=${NotificationCompat.PRIORITY_HIGH},   d=${NotificationManager.IMPORTANCE_HIGH}")
        builder.setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    //현재 FCM Token.
    fun doCheckFcmToken():String{
        var token = ""
        var fcm = Tasks.await(
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val msg = task.result
                token = task.result.toString()
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
        )

        return fcm
    }

    private fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "SSIAPP"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}