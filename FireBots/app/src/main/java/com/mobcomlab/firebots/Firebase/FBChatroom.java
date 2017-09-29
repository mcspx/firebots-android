package com.mobcomlab.firebots.Firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobcomlab.firebots.Models.Message;

import java.util.HashMap;
import java.util.Map;

public class FBChatroom {

    static public DatabaseReference getChatroomRef() {
        return FirebaseDatabase.getInstance().getReference().child(FBConstant.CHATROOM);
    }

    static public StorageReference getChatroomStorageRef() {
        return FirebaseStorage.getInstance().getReference().child(FBConstant.CHATROOM);
    }

    static public void removeMessage(final Message message) {
        if (message.isMediaMessage) {
            StorageReference mediaStorage = FirebaseStorage.getInstance().getReferenceFromUrl(message.photoURL);

            mediaStorage.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        getChatroomRef().child(FBConstant.MESSAGE).child(message.id).removeValue();
                    }
                }
            });
        } else {
            getChatroomRef().child(FBConstant.MESSAGE).child(message.id).removeValue();
        }
    }

    static public void removeMessageBadgeInChatroom() {
        getChatroomRef().child(FBConstant.USER).child(FBUser.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot chatroomBadgeDataSnapshot) {
                if (chatroomBadgeDataSnapshot.getValue() != null) {
                    final int badgeNumber = Integer.parseInt(chatroomBadgeDataSnapshot.getValue().toString());
                    if (badgeNumber > 0) {
                        FBUser.getUserRef().child(FBUser.uid).child(FBConstant.BADGE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userBadgeDataSnapshot) {
                                if (userBadgeDataSnapshot.child(FBConstant.MESSAGE).getValue() != null) {
                                    int userBadgeMessage = Integer.parseInt(userBadgeDataSnapshot.child(FBConstant.MESSAGE).getValue().toString());

                                    final Map<String, Object> chatroomBadgeUpdate = new HashMap<>();
                                    chatroomBadgeUpdate.put(FBUser.uid, 0);
                                    getChatroomRef().child(FBConstant.USER).updateChildren(chatroomBadgeUpdate);

                                    int userBadgeMessageUpdate = userBadgeMessage - badgeNumber;
                                    final Map<String, Object> userMessageBadgeUpdate = new HashMap<>();
                                    userMessageBadgeUpdate.put(FBConstant.MESSAGE, userBadgeMessageUpdate < 0 ? 0 : userBadgeMessageUpdate);
                                    userBadgeDataSnapshot.getRef().updateChildren(userMessageBadgeUpdate);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static public void updateChatroomUserRead(String messageID) {
        final Map<String, Object> chatroomUserReadUpdate = new HashMap<>();
        chatroomUserReadUpdate.put(FBUser.uid, messageID);
        getChatroomRef().child(FBConstant.READ).updateChildren(chatroomUserReadUpdate);
    }

    static public void removeChatroomUserRead() {
        FBChatroom.getChatroomRef().child(FBConstant.READ).removeValue();
    }
}
