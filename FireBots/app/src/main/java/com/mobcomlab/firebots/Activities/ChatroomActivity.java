package com.mobcomlab.firebots.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mobcomlab.firebots.Constants;
import com.mobcomlab.firebots.Firebase.FBChatroom;
import com.mobcomlab.firebots.Firebase.FBConstant;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Models.Chatroom;
import com.mobcomlab.firebots.Models.User;
import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.ButtonMedium;
import com.mobcomlab.firebots.Views.TextViewMedium;

import java.util.HashMap;
import java.util.Map;

public class ChatroomActivity extends MainActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Properties
    // Properties normal
    private TextViewMedium logoutButton;
    private ButtonMedium startChatButton;

    private GoogleApiClient googleApiClient;
    private Location location;
    private User user;

    public ChatroomActivity() {
        super(R.layout.activity_chatroom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.id.toolbar_title, getResources().getString(R.string.app_name));

        logoutButton = (TextViewMedium) findViewById(R.id.toolbar_menu_logout);
        startChatButton = (ButtonMedium) findViewById(R.id.start_chat_button);

        logoutButton.setOnClickListener(this);
        startChatButton.setOnClickListener(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            googleApiClient.connect();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_chat_button:
                String chatroomID = FBChatroom.getChatroomRef().push().getKey();
                Chatroom chatroom = new Chatroom(chatroomID, user.lat, user.lng);
                FBChatroom.getChatroomRef().child(chatroomID).setValue(chatroom.toMap());
                FBChatroom.getChatroomRef().child(chatroomID).child(FBConstant.USER).child(FBUser.uid).setValue(true);
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CHATROOM_ID, chatroomID);
                startActivity(intent);
                break;
            case R.id.toolbar_menu_logout:
                swapToLoginActivity();
                break;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (location != null) {
            FBUser.getUserRef().child(FBUser.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = new User(dataSnapshot);

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    double userLat = user.lat;
                    double userLng = user.lng;
                    if (userLat != 0.0 && userLng != 0.0) {
                        if (userLat != lat && userLng != lng) {
                            user.lat = lat;
                            user.lng = lng;
                        }
                    } else {
                        user.lat = lat;
                        user.lng = lng;
                    }

                    startChatButton.setEnabled(true);

                    final Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("lat", user.lat);
                    userUpdate.put("lng", user.lng);
                    user.ref.updateChildren(userUpdate);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("ChatroomActivity", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ChatroomActivity", "onConnectionFailed");

    }
}
