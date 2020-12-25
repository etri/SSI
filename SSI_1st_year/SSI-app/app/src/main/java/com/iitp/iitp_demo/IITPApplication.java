package com.iitp.iitp_demo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


/**
 * Keepin application
 */
public class IITPApplication extends Application{
    /**
     * for application lock. pin-code or fingerprint
     */



    private static IITPApplication instance;

    public static final boolean DEBUGFLAG = BuildConfig.DEBUG;
    public boolean hasId = false;



    /**
     * Get application instance
     *
     * @return application instance
     */
    public static IITPApplication getInstance() {
        return instance;
    }
}
