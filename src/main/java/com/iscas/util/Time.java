package com.iscas.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {
    public static long getCurTimeStampMs() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getCurTimeStr() {
        Date curDate = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return df.format(curDate);
    }
}