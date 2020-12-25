package com.iitp.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File utility
 */
public class FileUtils{

    /**
     * read private file
     *
     * @param context  current context
     * @param filePath file path to read
     * @return read bytes
     */
    public static byte[] readFile(Context context, String filePath){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        int readBytes;
        try{
            fis = context.openFileInput(filePath);
            while((readBytes = fis.read(buffer)) != -1){
                out.write(buffer, 0, readBytes);
            }

            return out.toByteArray();
        }catch(FileNotFoundException e){
            return null;
        }catch(IOException e){
            return null;
        }finally{
            if(fis != null) try{
                fis.close();
            }catch(IOException e){
                return null;
            }
        }
    }

    /**
     * 주어진 Uri 이 대한 파일 이름을 얻는다.
     *
     * @param context android context
     * @param uri     이름을 확인할 uri
     * @return 파일 이름
     */
    public static String getFileName(Context context, Uri uri){
        String ret = null;
        String scheme = uri.getScheme();

        if(scheme.equals("file")){
            ret = uri.getLastPathSegment();
        }else if(scheme.equals("content")){
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try{
                if(cursor != null && cursor.moveToFirst()){
                    ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally{
                if(cursor != null){
                    cursor.close();
                }
            }
        }
        return ret;
    }

    /**
     * white private file
     *
     * @param context  current context
     * @param filePath file path to write
     * @param data     bytes to write
     * @return File object
     */
    public static File writeFile(Context context, String filePath, byte[] data){
        FileOutputStream fos = null;

        try{
            fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
            fos.write(data);
            return context.getFileStreamPath(filePath);
        }catch(IOException e){
            return null;
        }finally{
            if(fos != null) try{
                fos.close();
            }catch(IOException e){
                Log.e("writeFile", "exception error ");
            }
        }
    }

    /**
     * Checks if external storage is available for read and write
     *
     * @return true if available read and write
     */
    public static boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     *
     * @return true if available to read
     */
    public static boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            return true;
        }
        return false;
    }

    /**
     * Get public storage directory path
     *
     * @param dir  sub directory
     * @param name file name
     * @return File object
     */
    public static File getPublicStorageDir(String dir, String name){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name);
        file.getParentFile().mkdirs();
        return file;
    }

    /**
     * 주어진 uri 를 read 한다.
     *
     * @param context android context
     * @param uri     read 할 uri
     * @return 읽은 byte array
     */
    public static byte[] readUri(Context context, Uri uri){
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream is = null;
        try{
            // read uri file
            is = context.getContentResolver().openInputStream(uri);
            if(is != null){
                int read;
                while((read = is.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, read);
                }
            }
        }catch(IOException e){
            return null;
        }finally{
            if(is != null){
                try{
                    is.close();
                }catch(IOException e){
                    Log.e("readUri", "exception error ");
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
}
