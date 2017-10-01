package com.mobcomlab.firebots.Models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Chatroom {

    private class Key {
        static final String lat = "lat";
        static final String lng = "long";
    }

    public String id = null;
    public double lat;
    public double lng;
    public DatabaseReference ref = null;

    public Chatroom(String id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.ref = null;
    }

    public Chatroom(DataSnapshot dataSnapshot) {
        id = dataSnapshot.getKey();
        ref = dataSnapshot.getRef();
        lat = parseDouble(dataSnapshot.child(Key.lat));
        lng = parseDouble(dataSnapshot.child(Key.lng));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Key.lat, lat);
        result.put(Key.lng, lng);
        return result;
    }

    private double parseDouble(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue() == null ? 0.0 : Double.parseDouble(dataSnapshot.getValue().toString());
    }
}
