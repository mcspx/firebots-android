package com.mobcomlab.firebots.Models;

import android.annotation.SuppressLint;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.mobcomlab.firebots.Helpers.DateHelper;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

    private class Key {
        static final String senderId = "senderId";
        static final String senderName = "senderName";
        static final String sendingTime = "sendingTime";
        static final String text = "text";
    }

    public String id = null;
    public String senderId = null;
    public String senderName = null;
    public DateTime sendingTime = null;
    public String text = null;
    public DatabaseReference ref = null;

    public Message(String id, String senderId, String senderName, DateTime sendingTime, String text) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.sendingTime = sendingTime;
        this.text = text;
    }

    public Message(DataSnapshot dataSnapshot) {
        id = dataSnapshot.getKey();
        ref = dataSnapshot.getRef();
        senderId = parseString(dataSnapshot.child(Key.senderId));
        senderName = parseString(dataSnapshot.child(Key.senderName));
        final String sendingTimeRaw = parseString(dataSnapshot.child(Key.sendingTime));
        sendingTime = sendingTimeRaw.equals("") ? DateHelper.currentTime() : DateHelper.parseISO8601String(sendingTimeRaw);
        text = parseString(dataSnapshot.child(Key.text));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Key.senderId, senderId);
        result.put(Key.senderName, senderName);
        result.put(Key.sendingTime, DateHelper.toISO8601String(sendingTime));
        result.put(Key.text, text);
        return result;
    }

    private String parseString(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue() == null ? "" : dataSnapshot.getValue().toString();
    }

    public String getSendingTimeString() {
        Calendar sendingTimeCalendar = Calendar.getInstance();
        sendingTimeCalendar.setTime(new Date(sendingTime.getMillis()));
        int hour = sendingTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = sendingTimeCalendar.get(Calendar.MINUTE);
        @SuppressLint("DefaultLocale") String sendingTimeString = String.format("%02d:%02d", hour, minute);

        return sendingTimeString;
    }
}
