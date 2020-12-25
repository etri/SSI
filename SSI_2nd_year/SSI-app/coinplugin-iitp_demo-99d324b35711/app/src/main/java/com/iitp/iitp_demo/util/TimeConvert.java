package com.iitp.iitp_demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeConvert{

    public static long convertTimeToLong(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        long timestamp = 0;
        try{
            Date changeDate = sdf.parse(date);
            timestamp = changeDate.getTime();
        }catch(ParseException e){
            PrintLog.e("convertTimeToLong error");
        }
        return timestamp;
    }

    public static String convertDateFormatTime(String time, SimpleDateFormat orgFormat, SimpleDateFormat convertFormat){
        orgFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        String changeFormatDate = null;
        try{
            date = orgFormat.parse(time);
            changeFormatDate = convertFormat.format(date);
        }catch(ParseException e){
            PrintLog.e("convertDateFormatTime error");
        }
        return changeFormatDate;
    }

    public static String convertDate(long time){
        String createDate;
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        createDate = dateFormat.format(date);
        PrintLog.e("convert date = " + createDate);
        return createDate;
    }
}
