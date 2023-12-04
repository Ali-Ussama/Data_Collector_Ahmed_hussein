package com.ekc.ekccollector.collector.view.utils;

import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // common Format...
    public final static String FORMAT_DATE = "yyyyMMdd";
    //public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_DATE_DASHED = "yyyy-MM-dd";
    public final static String FORMAT_DATE_DASHED_INVERSE = "dd-MM-yyyy";
    public final static String FORMAT_DATE_SLASH = "yyyy/MM/dd";
    public final static String FORMAT_DATE_SLASH_ = "dd/MM/yyyy";
    public final static String FORMAT_DATE_SLASH_UPLOAD = "dd/MM/yyyy";
    public final static String FORMAT_DATE_VISIT = "yyyy/MM/dd hh:mm";
    public final static String FORMAT_SERVER_FORMAT = "MM/dd/yyyy hh:mm:ss a"; // FORMAT_SERVER_FORMAT that come from WebService.
    public final static String FORMAT_PRINTER = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_POSITION_TRACE = "yyyy/MM/dd HH:mm:ss";
    public final static String FORMAT_TIME = "HH:mm:ss";
    public final static String FORMAT_BILL_DATE = "dd/MM/yyyy HH:mm";
    public final static String FORMAT_DATE_TIME = "dd/MM/yyyy hh:mm:ss a";
    public final static String SERVER_FORMAT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    //public final static String FORMAT_TIME = "HH:mm:ss"; // 24-FORMAT_SERVER_FORMAT.
    public final static String FORMAT_CAMERA_IMG_NAME = "yyyyMMdd_HHmmss";


    /* return calendar of @givenTimeStr with the @format*/
    public static Calendar formatDate(String givenTimeStr, String format) {
        Calendar calendar = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
            Date date = dateFormat.parse(givenTimeStr);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (Exception ee) {
            //LogUtils.log(ee.getMessage());
        }
        return calendar;
    }

    /* return calendar of @givenTimeStr with the @format*/
    public static String formatCalendar(Calendar cal, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            return sdf.format(cal.getTime());
        } catch (Exception ee) {
            return "";
        }
    }

    public static String formatDateIntoDateSplash(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date mDate = inputFormat.parse(date);
            return sdf.format(mDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentFormattedDate(String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            return sdf.format(Calendar.getInstance().getTime());
        } catch (Exception ee) {
            //LogUtils.log(ee.getMessage());
        }
        return null;
    }

    public static String getCurrentDateTime(String format) {
        try {
            //return Calendar.getInstance().getTime().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
            return dateFormat.format(Calendar.getInstance().getTime());
        } catch (Exception ee) {
            //LogUtils.log(ee.getMessage());
        }
        return "";
    }

    public static String getDateTime() {
        try {
            return Calendar.getInstance().getTime().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatDateTime(Calendar mCalendar, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            return sdf.format(mCalendar.getTime());
        } catch (Exception ee) {
            //LogUtils.log(ee.getMessage());
        }
        return null;
    }

    public static String formatDateTime(String givenDateTime, String format) {
        String formattedDateTime = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
            Date dateTime = dateFormat.parse(givenDateTime);
            formattedDateTime = dateFormat.format(dateTime);
        } catch (Exception ee) {
            Log.i("", ee.getMessage());
        }
        return formattedDateTime;
    }

    public static String formatPositionTraceDate(long timeInMilliSecond) {
        try {
            return new SimpleDateFormat(FORMAT_POSITION_TRACE, Locale.ENGLISH)
                    .format(new Date(timeInMilliSecond));
        } catch (Exception ex) {
            return "";
        }
    }

    /* SATURDAY: 0 , To FRIDAY:7 */

    public static String getWeekDayIndex(String day) {
        day = TextUtils.isEmpty(day) ? "" : day.trim();
        if (day.toUpperCase().equals("SUN") || day.toUpperCase().equals("SUNDAY") || day.toUpperCase().equals("SU")) {
            return 1 + "";
        } else if (day.toUpperCase().equals("MON") || day.toUpperCase().equals("MONDAY") || day.toUpperCase().equals("MO")) {
            return 2 + "";
        } else if (day.toUpperCase().equals("TUE") || day.toUpperCase().equals("TUESDAY") || day.toUpperCase().equals("TU")) {
            return 3 + "";
        } else if (day.toUpperCase().equals("WED") || day.toUpperCase().equals("WEDNESDAY") || day.toUpperCase().equals("WE")) {
            return 4 + "";
        } else if (day.toUpperCase().equals("THU") || day.toUpperCase().equals("THURSDAY") || day.toUpperCase().equals("TH")) {
            return 5 + "";
        } else if (day.toUpperCase().equals("FRI") || day.toUpperCase().equals("FRIDAY") || day.toUpperCase().equals("FR")) {
            return 6 + "";
        } else {
            return 0 + ""; // default SATURDAY...
        }
    }
}
