package com.mobcomlab.firebots.Models;

import android.annotation.SuppressLint;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
        static final String photoURL = "photoURL";
        static final String isFirstMessageOfDate = "isFirstMessageOfDate";
        static final String width = "width";
        static final String height = "height";
    }

    public String id = null;
    public String senderId = null;
    public String senderName = null;
    public String width = null;
    public String height = null;
    public DateTime sendingTime = null;
    public String text = null;
    public String photoURL = null;
    public StorageReference photoRef = null;
    public Boolean isFirstMessageOfDate = false;
    public Boolean isMediaMessage = false;
    public DatabaseReference ref = null;

    public Message(String id, String senderId, String senderName , String width, String height, DateTime sendingTime, String data, DateTime lastMessageDate) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.width = width;
        this.height = height;
        this.sendingTime = sendingTime;
        if (data.startsWith("gs://")) {
            this.photoURL = data;
            this.isMediaMessage = true;
        } else {
            this.text = data;
            this.isMediaMessage = false;
        }
        this.isFirstMessageOfDate = lastMessageDate == null || checkIsFirstMessageOfDate(lastMessageDate);
    }

    public Message(DataSnapshot dataSnapshot) {
        id = dataSnapshot.getKey();
        ref = dataSnapshot.getRef();
        senderId = parseString(dataSnapshot.child(Key.senderId));
        senderName = parseString(dataSnapshot.child(Key.senderName));
        width = parseString(dataSnapshot.child(Key.width));
        height = parseString(dataSnapshot.child(Key.height));
        final String sendingTimeRaw = parseString(dataSnapshot.child(Key.sendingTime));
        sendingTime = sendingTimeRaw.equals("") ? DateHelper.currentTime() : DateHelper.parseISO8601String(sendingTimeRaw);
        photoURL = parseString(dataSnapshot.child(Key.photoURL));
        if (photoURL.equals("")) {
            photoURL = null;
            isMediaMessage = false;
            text = parseString(dataSnapshot.child(Key.text));
        } else {
            if(!photoURL.equals("NOTSET")){
                photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoURL);
            }
            isMediaMessage = true;
        }
        if (dataSnapshot.child(Key.isFirstMessageOfDate).getValue() != null) {
            isFirstMessageOfDate = (boolean) dataSnapshot.child(Key.isFirstMessageOfDate).getValue();
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Key.senderId, senderId);
        result.put(Key.senderName, senderName);
        result.put(Key.width, width);
        result.put(Key.height, height);
        result.put(Key.sendingTime, DateHelper.toISO8601String(sendingTime));
        if (isMediaMessage) {
            result.put(Key.photoURL, photoURL);
        } else {
            result.put(Key.text, text);
        }
        result.put(Key.isFirstMessageOfDate, isFirstMessageOfDate);
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

    private boolean checkIsFirstMessageOfDate(DateTime lastMessageDate) {
        Calendar lastMessageCalendar = Calendar.getInstance();
        Calendar currentMessageCalendar = Calendar.getInstance();
        lastMessageCalendar.setTime(new Date(lastMessageDate.getMillis()));
        currentMessageCalendar.setTime(new Date(DateHelper.currentTime().getMillis()));

        return !(lastMessageCalendar.get(Calendar.YEAR) == currentMessageCalendar.get(Calendar.YEAR) && lastMessageCalendar.get(Calendar.DAY_OF_YEAR) == currentMessageCalendar.get(Calendar.DAY_OF_YEAR));
    }
}
