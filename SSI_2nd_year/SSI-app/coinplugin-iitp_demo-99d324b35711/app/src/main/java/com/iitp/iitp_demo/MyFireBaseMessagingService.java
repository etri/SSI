package com.iitp.iitp_demo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.request.DidSelectWebActivity;
import com.iitp.iitp_demo.api.model.MessageVo;
import com.iitp.iitp_demo.api.model.pushTokenVo;
import com.iitp.iitp_demo.util.BusProvider;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import java.util.Map;

import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN;
import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN_SET;

public class MyFireBaseMessagingService extends FirebaseMessagingService{

    @Override
    public void onNewToken(String token){
        Log.d("FCM__", "onNewToken() token=" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
//        PrintLog.e("onMessageReceived()" + IITPApplication.gson.toJson(remoteMessage));
        String action = null;
        if(remoteMessage.getNotification() == null){
            Map<String, String> data = remoteMessage.getData();
            if(data != null){
                String message = data.get("jsonMsg");
                PrintLog.e("jsonMsg = " + message);
                pushTokenVo token = IITPApplication.gson.fromJson(message, pushTokenVo.class);
                PrintLog.e("action = " + token.getAction());
                action = token.getAction();
                MessageVo messageVo = token.getMessage();
                String sendData = IITPApplication.gson.toJson(messageVo);
                PrintLog.e("send data = "+sendData);
                if(action.equals("stock")){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("stockMessage", sendData);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    String channelId = "stock";
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("SSI-PUSH")
                            .setContentText("증권 보고서 수신")
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        String channelName = "stock";
                        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(0, notificationBuilder.build());
                }else if(action.equals("company")){
                    Intent intent = new Intent(this, DidSelectWebActivity.class);
                    intent.putExtra("requesturl", messageVo.getRequestURL());
                    intent.putExtra("msgid", messageVo.getMsgid());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                    String channelId = "company";
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("SSI-PUSH")
                            .setContentText("사원증 발급을 위한 DID 선택")
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        String channelName = "company";
                        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(1, notificationBuilder.build());
                }

            }
        }else{
            String msgTitle = remoteMessage.getNotification().getTitle();
            String msgBody = remoteMessage.getNotification().getBody();
            PrintLog.e("Msg:" + remoteMessage.getNotification().getBody());
            PrintLog.e("Msg:" + remoteMessage.getNotification().getTitle());
            pushTokenVo token = IITPApplication.gson.fromJson(msgBody, pushTokenVo.class);
            if(token.getAction().equals("delegate")){
                CommonPreference.getInstance(this).getSecureSharedPreferences().edit().putString(DELEGATOR_TOKEN, token.getToken()).commit();
                CommonPreference.getInstance(this).getSecureSharedPreferences().edit().putBoolean(DELEGATOR_TOKEN_SET, true).commit();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_ONE_SHOT);
                String channelId = "delegate";
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msgTitle)
                        .setContentText("신분증VC 위임장 도착")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri).setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    String channelName = "delegate";
                    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(2, notificationBuilder.build());
                BusProvider.getInstance().post("CredentialListActivity");
            }else if(token.getAction().equals("sendResult")){
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_ONE_SHOT);
                String channelId = "delegate";
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msgTitle)
                        .setContentText("신분증VC 위임장 수령확인")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    String channelName = "delegate";
                    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(2, notificationBuilder.build());
                BusProvider.getInstance().post("CredentialListActivity");
            }else if(token.getAction().equals("company")){
                PrintLog.e("action = " + token.getAction());
                MessageVo messageVo = token.getMessage();
                PrintLog.e("message = " + messageVo);
                Intent intent = new Intent(this, DidSelectWebActivity.class);
                intent.putExtra("requesturl", messageVo.getRequestURL());
                intent.putExtra("msgid", messageVo.getMsgid());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                String channelId = "company";
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msgTitle)
                        .setContentText("사원증 발급을 위한 DID 선택")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    String channelName = "company";
                    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(1, notificationBuilder.build());
            }
        }
    }
}
