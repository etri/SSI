package com.iitp.iitp_demo.util;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iitp.iitp_demo.R;

public class BackPressCloseHandler{
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide(){
        toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        toastView.setBackgroundColor(activity.getResources().getColor(R.color.white));
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(activity.getResources().getColor(R.color.blackText));
        toastMessage.setBackgroundColor(activity.getResources().getColor(R.color.white));
        toast.show();
    }

}
