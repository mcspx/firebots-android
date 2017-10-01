package com.mobcomlab.firebots.Models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private class Key {
        static final String username = "username";
        static final String lat = "lat";
        static final String lng = "long";
    }

    public String uid = null;
    public String username = null;
    public double lat;
    public double lng;
    public DatabaseReference ref = null;

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
        this.lat = 0.0;
        this.lng = 0.0;
        this.ref = null;
    }

    public User(DataSnapshot dataSnapshot) {
        uid = dataSnapshot.getKey();
        ref = dataSnapshot.getRef();
        username = parseString(dataSnapshot.child(Key.username));
        lat = parseDouble(dataSnapshot.child(Key.lat));
        lng = parseDouble(dataSnapshot.child(Key.lng));

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Key.username, username);
        if (lat != 0.0 && lng != 0.0) {
            result.put(Key.lat, lat);
            result.put(Key.lng, lng);
        }
        return result;
    }

    private String parseString(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue() == null ? "" : dataSnapshot.getValue().toString();
    }

    private double parseDouble(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue() == null ? 0.0 : Double.parseDouble(dataSnapshot.getValue().toString());
    }
}
