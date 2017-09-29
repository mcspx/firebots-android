package com.mobcomlab.firebots.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mobcomlab.firebots.Firebase.FBChatroom;
import com.mobcomlab.firebots.Firebase.FBConstant;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.ButtonMedium;
import com.mobcomlab.firebots.Views.TextViewMedium;

public class ChatroomActivity extends MainActivity implements View.OnClickListener {

    // Properties
    // Properties normal
    private TextViewMedium logoutButton;
    private ButtonMedium startChatButton;

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_chat_button:
//                FBChatroom.getChatroomRef().child(FBConstant.USER).child(FBUser.uid).setValue(true);
//                Intent intent = new Intent(this, ChatActivity.class);
//                startActivity(intent);
                break;
            case R.id.toolbar_menu_logout:
                swapToLoginActivity();
                break;
        }
    }
}
