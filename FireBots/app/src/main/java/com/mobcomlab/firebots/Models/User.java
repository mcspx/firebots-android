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
    }

    public String uid = null;
    public String username = null;
    public DatabaseReference ref = null;

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
        this.ref = null;
    }

    public User(DataSnapshot dataSnapshot) {
        uid = dataSnapshot.getKey();
        ref = dataSnapshot.getRef();
        username = parseString(dataSnapshot.child(Key.username));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Key.username, username);
        return result;
    }

    private String parseString(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue() == null ? "" : dataSnapshot.getValue().toString();
    }
}
