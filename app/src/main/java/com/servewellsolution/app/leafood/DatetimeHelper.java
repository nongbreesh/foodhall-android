package com.servewellsolution.app.leafood;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Breeshy on 9/4/2016 AD.
 */

public class DatetimeHelper {
    public static String convertDate(String date) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tzz = cal.getTimeZone();

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(tzz.getID());
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yyyy ' เวลา' HH:mm");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertDate2(String date) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tzz = cal.getTimeZone();

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(tzz.getID());
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yyyy");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertDateUTC(String date) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tzz = cal.getTimeZone();

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date1 = sdf.format(parsed.getTime());

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date2 = sdf.format(parsed.getTime());



            Date parsed1 = sourceFormat.parse(date1);
            Date parsed2 = sourceFormat.parse(date2);


            System.out.println(parsed1);
            System.out.println(parsed2);
            long mills =  parsed2.getTime() - parsed1.getTime();
            int diffHour = (int) TimeUnit.MILLISECONDS.toHours(mills);

            System.out.println(diffHour);


            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            calendar.add(Calendar.HOUR, diffHour);

            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
