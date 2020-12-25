package com.iitp.iitp_demo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iitp.util.secure.SecureSharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class CommonPreference{
    private static String PREF_NAME = "iitp";
    private static String SECURE_PREF_NAME = "iitp_s";
    private static CommonPreference instance;
    private SharedPreferences mPref;
    private SharedPreferences mSecurePref;
    private Context mContext;
    private SharedPreferences.Editor mEditor;

    private CommonPreference(Context context){
        mContext = context;
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
    }

    public SharedPreferences getSecureSharedPreferences(){
        if(mSecurePref == null){
            mSecurePref = new SecureSharedPreferences(mContext, mContext.getSharedPreferences(SECURE_PREF_NAME, Context.MODE_PRIVATE));
        }
        return mSecurePref;
    }


    public static CommonPreference getInstance(Context context){
        System.out.println("create instance");
        if(instance == null){
            instance = new CommonPreference(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Stores String value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, String value){
        mEditor.putString(key, value);
        mEditor.commit();
    }

    /**
     * Stores int value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, int value){
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    /**
     * Stores Double value in String format in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, double value){
        setValue(key, Double.toString(value));
    }

    /**
     * Stores long value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, long value){
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    /**
     * Stores boolean value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, boolean value){
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    /**
     * Stores Map value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, Map<String, String> value){
        Gson gson = new Gson();
        String data = gson.toJson(value);
        mEditor.putString(key, data);
        mEditor.commit();
    }

    /**
     * Retrieves String value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public String getStringValue(String key, String defaultValue){
        return mPref.getString(key, defaultValue);
    }

    /**
     * Retrieves int value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public int getIntValue(String key, int defaultValue){
        return mPref.getInt(key, defaultValue);
    }

    /**
     * Retrieves long value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public long getLongValue(String key, long defaultValue){
        return mPref.getLong(key, defaultValue);
    }

    /**
     * Retrieves boolean value from preference
     *
     * @param keyFlag      key of preference
     * @param defaultValue default value if no key found
     */
    public boolean getBooleanValue(String keyFlag, boolean defaultValue){
        return mPref.getBoolean(keyFlag, defaultValue);
    }

    /**
     * Retrieves boolean value from preference
     *
     * @param keyFlag      key of preference
     * @param defaultValue default value if no key found
     * @return
     */

    public Map<String, String> getMapValue(String keyFlag, String defaultValue){
        Gson gson = new Gson();
        String data = mPref.getString(keyFlag, defaultValue);
        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){
        }.getType();
        HashMap<String, String> map = gson.fromJson(data, type);
        return map;
    }

    /**
     * Removes key from preference
     *
     * @param key key of preference that is to be deleted
     */
    public void removeKey(String key){
        if(mEditor != null){
            mEditor.remove(key);
            mEditor.commit();
        }
    }

    /**
     * Clears all the preferences stored
     */
    public void clear(){
        mEditor.clear().commit();
    }

}

