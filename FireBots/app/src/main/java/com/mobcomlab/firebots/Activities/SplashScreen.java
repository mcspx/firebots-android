package com.mobcomlab.firebots.Activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mobcomlab.firebots.Constants;
import com.mobcomlab.firebots.Helpers.DateHelper;
import com.mobcomlab.firebots.Helpers.PermissionHelper;
import com.mobcomlab.firebots.R;

import me.leolin.shortcutbadger.ShortcutBadger;

public class SplashScreen extends MainActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Properties
    // Properties permission
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private String notificationType;
    private String chatroomID;

    public SplashScreen() {
        super(R.layout.activity_splash);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup JodaTime for default timezone convert
        DateHelper.setupJodaTime(this);

        // Get post key from intent
        if (getIntent().hasExtra(Constants.EXTRA_NOTIFICATION_TYPE)) {
            notificationType = getIntent().getStringExtra(Constants.EXTRA_NOTIFICATION_TYPE);
        }
        if (getIntent().hasExtra(Constants.EXTRA_CHATROOM_ID)) {
            chatroomID = getIntent().getStringExtra(Constants.EXTRA_CHATROOM_ID);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    // MARK: Check permissions
    private void checkPermissions() {
        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        if (!PermissionHelper.hasPermissions(this, PERMISSIONS)) {
            int PERMISSION_ALL = 1;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            checkNotification();
        }
    }

    private void checkNotification() {
        if (isAuth()) {
            final Bundle extras = new Bundle();
            if (notificationType != null) {
                // Set up extra parameter
                switch (notificationType) {
                    case Constants.EXTRA_NOTIFICATION_TYPE_CHATROOM_INVITATION:
                        extras.putBoolean(Constants.EXTRA_IS_NOTIFICATION, true);
                        extras.putString(Constants.EXTRA_CHATROOM_ID, chatroomID);
                        break;
//                    case Constants.EXTRA_NOTIFICATION_TYPE_NEW_MESSAGE:
//                        extras.putInt(Constants.TABBAR_START_PAGE, 2);
//                        extras.putBoolean(Constants.EXTRA_IS_NOTIFICATION, true);
//                        extras.putString(Constants.EXTRA_ACTIVITY_ID, activityID);
//                        break;
                    default:
                        break;
                }
                startCountdown(extras);
            } else {
                startCountdown(extras);
            }
        } else {
            startCountdown(null);
        }
    }

    private void startCountdown(final Bundle extras) {
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                if (extras != null) {
                    // Start your app Tabbar activity
                    swapToChatroomActivity(extras);
                } else {
                    // Start your app Login activity
                    swapToLoginActivity();
                }
                finish();
            }
        }, 3000);
        // 3000 is SPLASH_TIME_OUT
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkNotification();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        startCountdown(new Bundle());
    }
}
