package com.iitp.iitp_demo.util;

import android.util.Log;

import com.iitp.iitp_demo.BuildConfig;


public class PrintLog{
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String tag = new StringBuilder().append("[").append(Thread.currentThread().getStackTrace()[4].getMethodName())
            .append("()").append("-").append(Thread.currentThread().getStackTrace()[4].getLineNumber()).append("]").toString();
    public static void e(String message) {
        if(DEBUG){

            if (message.length() > 3000) {
                Log.e(tag, message.substring(0, 3000));
                e(message.substring(3000));
            } else {
                Log.e(tag, message);
            }
        }
    }

    public static void i(String message) {
        if(DEBUG){

            if (message.length() > 3000) {
                Log.i(tag, message.substring(0, 3000));
                e(message.substring(3000));
            } else {
                Log.i(tag, message);
            }
        }

    }
}
