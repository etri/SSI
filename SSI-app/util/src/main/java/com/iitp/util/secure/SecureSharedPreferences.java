package com.iitp.util.secure;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SharedPreference with AndroidKeyStore.<br>
 * Use RSA with AndroidKeyStore and data encryption algorithm is AES<br>
 */
public class SecureSharedPreferences implements SharedPreferences{
    /**
     * Android Keystore alias
     */
    private static final String ALIAS = "secure_sp";

    /**
     * SharedPreferences to delegating
     */
    private SharedPreferences delegate;

    /**
     * Constructor
     *
     * @param context android context
     * @param sp      to delegating
     */
    public SecureSharedPreferences(Context context, SharedPreferences sp){
        delegate = sp;

        try{
            // create key in keystore
            if(KeyStoreUtils.getKeyStoreEntry(ALIAS) == null){
                KeyStoreUtils.createKeyStoreEntryRsaEcbPkcs1Padding(context, ALIAS);

                // Previously stored data is not needed and should be deleted
                sp.edit().clear().apply();
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt value
     *
     * @param value to encrypt
     * @return encrypted text
     */
    private String encrypt(String value){
        try{
            return Base64.encodeToString(KeyStoreUtils.encryptAesWithRsa(ALIAS, value.getBytes()), Base64.NO_WRAP);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypt value
     *
     * @param value to decrypt
     * @return decrypted text
     */
    private String decrypt(String value){
        if(value == null){
            return null;
        }
        try{
            return new String(KeyStoreUtils.decryptAesWithRSA(ALIAS, Base64.decode(value, Base64.NO_WRAP)));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt string set
     *
     * @param values to encrypt
     * @return encrypted set
     */
    private Set<String> encryptSet(Set<String> values){
        Set<String> encryptedValues = new HashSet<>();
        for(String value : values){
            encryptedValues.add(encrypt(value));
        }
        return encryptedValues;
    }

    /**
     * Decrypt string set
     *
     * @param values to decrypt
     * @return decrypted set
     */
    private Set<String> decryptSet(Set<String> values){
        Set<String> decryptedValues = new HashSet<>();
        for(String value : values){
            decryptedValues.add(decrypt(value));
        }
        return decryptedValues;
    }

    /**
     * @return values is null
     */
    @Override
    public Map<String, ?> getAll(){
        Map<String, ?> all = delegate.getAll();
        Set<String> keys = all.keySet();
        HashMap<String, String> ret = new HashMap<>(keys.size());
        for(String key : keys){
            ret.put(key, null);
        }
        return ret;
    }

    @Override
    public String getString(String key, String defValue){
        final String v = delegate.getString(key, null);
        return v == null ? defValue : decrypt(v);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues){
        final Set<String> v = delegate.getStringSet(key, null);
        return v == null ? defValues : decryptSet(v);
    }

    @Override
    public int getInt(String key, int defValue){
        final String v = delegate.getString(key, null);
        return v == null ? defValue : Integer.parseInt(decrypt(v));
    }

    @Override
    public long getLong(String key, long defValue){
        final String v = delegate.getString(key, null);
        return v == null ? defValue : Long.parseLong(decrypt(v));
    }

    @Override
    public float getFloat(String key, float defValue){
        final String v = delegate.getString(key, null);
        return v == null ? defValue : Float.parseFloat(decrypt(v));
    }

    @Override
    public boolean getBoolean(String key, boolean defValue){
        final String v = delegate.getString(key, null);
        return v == null ? defValue : Boolean.parseBoolean(decrypt(v));
    }

    @Override
    public boolean contains(String key){
        return delegate.contains(key);
    }

    @Override
    public Editor edit(){
        return new Editor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){
        delegate.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){
        delegate.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private class Editor implements SharedPreferences.Editor{
        SharedPreferences.Editor delegate;

        private Editor(){
            delegate = SecureSharedPreferences.this.delegate.edit();
        }

        @Override
        public SharedPreferences.Editor putString(String key, String value){
            delegate.putString(key, encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, Set<String> values){
            delegate.putStringSet(key, values == null ? null : encryptSet(values));
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value){
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value){
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value){
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value){
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key){
            delegate.remove(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear(){
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit(){
            return delegate.commit();
        }

        @Override
        public void apply(){
            delegate.apply();
        }
    }

}
