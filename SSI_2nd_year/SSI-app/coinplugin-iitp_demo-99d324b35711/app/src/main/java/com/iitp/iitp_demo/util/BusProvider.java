package com.iitp.iitp_demo.util;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.ArrayList;

public final class BusProvider extends Bus{
    private ArrayList registeredObjects = new ArrayList();
    private static final BusProvider BUS = new BusProvider();
    public static Bus getInstance(){
        return BUS;
    }

    private BusProvider(){
        // No instances.
    }
    private final Handler mHandler = new Handler(Looper.getMainLooper());


    //object register
    @Override
    public void register(Object object) {
        if(!registeredObjects.contains(object)){
            registeredObjects.add(object);
            super.register(object);
        }
    }

    //object unregister
    @Override
    public void unregister(Object object) {
        if(registeredObjects.contains(object)){
            registeredObjects.remove(object);
            super.unregister(object);
        }
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    BusProvider.super.post(event);
                }
            });
        }
    }
}

