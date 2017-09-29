package com.mobcomlab.firebots.Helpers;

import android.content.Context;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.TimeZone;

public class DateHelper {

    public static void setupJodaTime(Context context) {
        JodaTimeAndroid.init(context);
    }

    public static DateTime currentTime() {
        changeDefaultTimeZone(TimeZone.getDefault());
        return new DateTime();
    }

    private static void changeDefaultTimeZone(TimeZone timeZone) {
        if (timeZone != null) {
            DateTimeZone newDefault = DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone.getID()));
            DateTimeZone.setDefault(newDefault);
        } else {
            DateTimeZone.setDefault(DateTimeZone.UTC);
        }
    }

    private static DateTimeFormatter iso8601DateFormatter = ISODateTimeFormat.dateTime();

    public static String toISO8601String(DateTime date) {
        changeDefaultTimeZone(null);
        date = date.toDateTime(DateTimeZone.getDefault());
        return date.toString(iso8601DateFormatter);
    }

    public static DateTime parseISO8601String(String isoDateString) {
        changeDefaultTimeZone(TimeZone.getDefault());
        return new DateTime(isoDateString);
    }


    private static DateTimeFormatter dateFormatter = DateTimeFormat.mediumDate();
//    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.mediumDateTime();
//    private static DateTimeFormatter timeFormatter = DateTimeFormat.shortDate();

    public static String toDateString(DateTime date) {
        return date.toString(dateFormatter);
    }

//    public static String toDateTimeString(DateTime date) {
//        return date.toString(dateTimeFormatter);
//    }

//    public static String toTimeString(DateTime date) {
//        return date.toString(timeFormatter);
//    }
}
