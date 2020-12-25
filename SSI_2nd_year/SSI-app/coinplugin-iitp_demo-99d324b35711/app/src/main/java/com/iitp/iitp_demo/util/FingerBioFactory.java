package com.iitp.iitp_demo.util;


import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FingerBioFactory{
    private AppCompatActivity activity;
    private BiometricPrompt.AuthenticationCallback callback;
    public BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo biometricPromptInfo;



    public FingerBioFactory(AppCompatActivity activity, BiometricPrompt.AuthenticationCallback callback){
        this.activity = activity;
        this.callback = callback;
//        setting();
    }

    public void setting(String title, String des, String negative){
        Executor newExecutor = Executors.newSingleThreadExecutor();
        biometricPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle("")
                .setDescription(des)
                .setNegativeButtonText(negative)
                .build();

        biometricPrompt = new androidx.biometric.BiometricPrompt(activity, newExecutor, callback);

    }

    public void authenticate(){
        biometricPrompt.authenticate(biometricPromptInfo);
    }






}
