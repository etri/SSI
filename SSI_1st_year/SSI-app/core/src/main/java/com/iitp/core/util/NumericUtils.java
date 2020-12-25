package com.iitp.core.util;

import android.util.Log;

import com.iitp.util.Bytes;

import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link Numeric} Utility
 */
public class NumericUtils{
    /**
     * concatenated array of Hex-string to bytes<br/>
     * ["010101","020202", "030303"], 4 => [00,01,01,01,00,02,02,02,00,03,03,03]
     *
     * @param input          array of Hex-string
     * @param outBytesLength fixed length of hex-string
     * @return concatenated bytes
     */
    public static byte[] hexStringArrayToByteArray(String[] input, int outBytesLength){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
        try{
            for(String i : input){
                buffer.write(Bytes.expandPadded(Numeric.hexStringToByteArray(i), outBytesLength));
            }
        }catch(IOException e){
            // not cause
            Log.e("NumericUtils", "exception error " );
        }finally{
            try{
                buffer.close();
            }catch(IOException e){
                Log.e("NumericUtils", "exception error" );
            }
        }
        return buffer.toByteArray();
    }
}
