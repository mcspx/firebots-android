package com.mobcomlab.firebots.Firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBChatroom {

    static public DatabaseReference getChatroomRef() {
        return FirebaseDatabase.getInstance().getReference().child(FBConstant.CHATROOM);
    }
}
