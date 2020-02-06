package com.iitp.util;

/**
 * Byte array utility
 */
public class Bytes{
    /**
     * Concatenate several byte array
     *
     * @param arrays byte arrays
     * @return concatenated byte array
     */
    public static byte[] concat(byte[]... arrays){
        int length = 0;
        for(byte[] array : arrays){
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for(byte[] array : arrays){
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    /**
     * byte array 크기를 확장한다.<br/>확장된 영역은 0 으로 채워짐
     *
     * @param src    source byte array
     * @param length 확장할 크기
     * @return 확장된 byte array
     */
    public static byte[] expandPadded(byte[] src, int length) throws RuntimeException{
        if(length < src.length){
            throw new RuntimeException("Input is to large target length");
        }

        if(length == src.length){
            return src;
        }

        byte[] result = new byte[length];
        System.arraycopy(src, 0, result, length - src.length, src.length);
        return result;
    }
}
