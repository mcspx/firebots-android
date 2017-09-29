package com.mobcomlab.firebots.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobcomlab.firebots.Models.User;

import java.util.HashMap;
import java.util.Map;

public class FBUser {

    static public String uid = "";

    static public DatabaseReference getUserRef() {
        return FirebaseDatabase.getInstance().getReference().child(FBConstant.USER);
    }

    static public StorageReference getUserStorageRef() {
        return FirebaseStorage.getInstance().getReference().child(FBConstant.USER);
    }

    static public void setupUID(String userId) {
        uid = userId;
        addToken();
    }

    static public void setupUser(String userId, String username) {
        uid = userId;
        User user = new User(userId, username);
        getUserRef().child(uid).setValue(user.toMap());
        addToken();
    }

    static private void addToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (refreshedToken != null && !refreshedToken.equals("")) {
                final Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put(FBConstant.TOKEN, refreshedToken);
                getUserRef().child(uid).updateChildren(userUpdate);
            }
        }
    }

    static public void removeToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isAnonymous()) {
                uid = "";
                user.delete();
                getUserRef().child(user.getUid()).removeValue();
                FirebaseAuth.getInstance().signOut();
            }
            else {
                getUserRef().child(uid).child(FBConstant.TOKEN).removeValue();
                uid = "";
                FirebaseAuth.getInstance().signOut();
            }
        } else {
            uid = "";
            FirebaseAuth.getInstance().signOut();
        }
    }
}
