package com.iitp.iitp_demo;

import android.app.Application;

import com.google.gson.Gson;


/**
 * Keepin application
 */
public class IITPApplication extends Application{
    /**
     * for application lock. pin-code or fingerprint
     */

    public static final Gson gson = new Gson();

    private static IITPApplication instance;


    /**
     * Get application instance
     *
     * @return application instance
     */
    public static IITPApplication getInstance(){
        return instance;
    }
}
