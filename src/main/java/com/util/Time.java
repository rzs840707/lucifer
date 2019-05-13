package com.iscas.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {

    public final static String pattern = "yyyy-MM-dd HH:mm:ss:SSS";
    public final static String pattern1 = "dd HH:mm:ss";

    public static long getCurTimeStampMs() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getCurTimeStr() {
        Date curDate = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(curDate);
    }

    /**
     * @param timestamp 毫秒级别时间戳
     * @return yyyy-MM-dd HH:mm:ss:SSS格式的字符串
     */
    public static String timestamp2Str(long timestamp) {
        Date d = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(d);

    }

    public static String getCurTimeStr(String pattern){
        Date curDate = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(curDate);
    }
}