package com.mobcomlab.firebots.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobcomlab.firebots.Firebase.FBUser;

import net.danlew.android.joda.JodaTimeAndroid;

import me.leolin.shortcutbadger.ShortcutBadger;

public abstract class MainActivity extends AppCompatActivity {
    private final int layoutResId;

    public MainActivity(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResId);
        JodaTimeAndroid.init(this);
    }

    public void setTitle(int toolbarId, String title) {
        TextView titleTextView = (TextView) findViewById(toolbarId);
        titleTextView.setText(title);
    }

    protected boolean isAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (FBUser.uid.equals("") || !FBUser.uid.equals(user.getUid())) {
                FBUser.setupUID(user.getUid());
            }
            return true;
        } else {
            return false;
        }
    }

    protected void swapToLoginActivity() {
        // remove all badge
        ShortcutBadger.applyCount(this, 0);

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        FBUser.removeToken();
    }

    protected void swapToChatroomActivity(Bundle extras) {
        Intent intent = new Intent(this, ChatroomActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
    }
}
