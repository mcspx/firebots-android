package com.mobcomlab.firebots.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mobcomlab.firebots.Adapters.MessageAdapter;
import com.mobcomlab.firebots.Constants;
import com.mobcomlab.firebots.Firebase.FBChatroom;
import com.mobcomlab.firebots.Firebase.FBConstant;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Helpers.DateHelper;
import com.mobcomlab.firebots.Helpers.DialogHelper;
import com.mobcomlab.firebots.Models.Message;
import com.mobcomlab.firebots.Models.User;
import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewNormal;

import java.util.ArrayList;

public class ChatActivity extends MainActivity implements MessageAdapter.OnMessageLongClickListener {

    // Properties

    // Properties Firebase
    private DatabaseReference chatroomRef;
    private DatabaseReference chatroomUserRef;
    private DatabaseReference chatroomMessageRef;
    private Query messageQuery;
    private DatabaseReference userRef;

    // Properties normal
    private TextViewNormal emptyStateTextView;
    private RecyclerView messageRecycler;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;

    private String chatroomID;
    private ArrayList<String> memberIDs = new ArrayList<>();
    private EditText inputMessage;
    private Button sendButton;
    private int messageCount = 0;
    private User user;

    int currentChild = 0;
//    private DatabaseReference userIsTypingRef;
//    private Query usersTypingQuery;
//    private Boolean localTyping = false;
//    Boolean isTyping = false;

    public ChatActivity() {
        super(R.layout.activity_chat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show loading indicator
        DialogHelper.showIndicator(this, getResources().getString(R.string.loading));

        chatroomID = getIntent().getStringExtra(Constants.EXTRA_CHATROOM_ID);

        // Initialize Database
        chatroomRef = FBChatroom.getChatroomRef().child(chatroomID);
        chatroomUserRef = FBChatroom.getChatroomRef().child(chatroomID).child(FBConstant.USER);
        chatroomMessageRef = FBChatroom.getChatroomRef().child(chatroomID).child(FBConstant.MESSAGE);
        messageQuery = FBChatroom.getChatroomRef().child(chatroomID).child(FBConstant.MESSAGE).
                orderByChild(FBConstant.SENDING_TIME).limitToLast(5000);
        userRef = FBUser.getUserRef();

        // Listen for single user
        userRef.child(FBUser.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = new User(dataSnapshot);

                // Setup title
                if(user.username.length() <= 28){
                    setTitle(R.id.toolbar_title, user.username);
                }else {
                    String str = user.username.substring(0,28) + "...";
                    setTitle(R.id.toolbar_title, str);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Setup input toolbar
        inputMessage = (EditText) findViewById(R.id.content_new_message);
        sendButton = (Button) findViewById(R.id.chat_send_button);

        // Initialize Views
        emptyStateTextView = (TextViewNormal) findViewById(R.id.chat_welcome_text);
        messageRecycler = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecycler.setLayoutManager(layoutManager);

        emptyStateTextView.setVisibility(View.GONE);
        messageRecycler.setVisibility(View.GONE);

//        usersTypingQuery = chatroomRef.child("typingIndicator").orderByValue().equalTo(true);
        // Listen on input toolbar text change
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (inputMessage.getText().toString().trim().equals("")) {
                    sendButton.setEnabled(false);
                    sendButton.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.textUnselected));
                    chatroomRef.child(FBConstant.TYPING_INDICATOR).child(FBUser.uid).removeValue();
                } else {
                    sendButton.setEnabled(true);
                    sendButton.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.white));
                    chatroomRef.child(FBConstant.TYPING_INDICATOR).child(FBUser.uid).setValue(true);
                }
            }
        });

        // Send button on click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textMessage = inputMessage.getText().toString();
                if (!textMessage.trim().equals("")) {
                    sendTextMessage(textMessage);
                    inputMessage.setText("");
                }
            }
        });

        // Listen message query for setup messageRecycler
        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot messageDataSnapshot) {
                messageCount = (int) messageDataSnapshot.getChildrenCount();
                if (messageCount > 0) {
                    messageRecycler.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    DialogHelper.dismissIndicator();
                }
                messageAdapter = new MessageAdapter(messageQuery);
                messageAdapter.setOnMessageLongClickListener(ChatActivity.this);
                messageRecycler.setAdapter(messageAdapter);
                messageAdapter.registerAdapterDataObserver(new RecyclerView.
                        AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int messageCount = messageAdapter.getItemCount();
                        if (messageCount > 0 && messageRecycler.getVisibility() == View.GONE) {
                            emptyStateTextView.setVisibility(View.GONE);
                            messageRecycler.setVisibility(View.VISIBLE);
                        }
                        int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                        // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                        // to the bottom of the list to show the newly added message.
                        if (lastVisiblePosition == -1 || (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                            messageRecycler.scrollToPosition(positionStart);
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        super.onItemRangeRemoved(positionStart, itemCount);
                        if (messageAdapter.getItemCount() == 0 && messageRecycler.
                                getVisibility() == View.VISIBLE) {
                            messageRecycler.setVisibility(View.GONE);
                            emptyStateTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                chatroomUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            final int lastChild = (int) dataSnapshot.getChildrenCount();
                            for (DataSnapshot chatroomUser : dataSnapshot.getChildren()) {
                                userRef.child(chatroomUser.getKey()).
                                        addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot userDataSnapshot) {
                                                final User member = new User(userDataSnapshot);
                                                currentChild +=1;
                                                memberIDs.add(member.uid);
                                                if (currentChild == lastChild) {
                                                    DialogHelper.dismissIndicator();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                if (currentChild == lastChild) {
                                                    DialogHelper.dismissIndicator();
                                                }
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        DialogHelper.dismissIndicator();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     */

    private void sendTextMessage(String textMessage) {
        final DatabaseReference messageNewRef = chatroomMessageRef.push().getRef();
        Message message = new Message(
                messageNewRef.getKey(),
                user.uid,
                user.username,
                DateHelper.currentTime(),
                textMessage
        );
        messageNewRef.setValue(message.toMap());
        chatroomRef.child(FBConstant.TYPING_INDICATOR).child(user.uid).removeValue();
    }

    private void showActionAlert(final CharSequence[] items, final Message message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        copyMessage(message);
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void copyMessage(Message message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(message.text, message.text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onMessageLongClickListener(Message message) {
        CharSequence[] items = new String[2];
        items[0] = getResources().getString(R.string.copy);
        items[1] = getResources().getString(R.string.cancel);
        showActionAlert(items, message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageAdapter != null) {
            messageAdapter.cleanup();
        }
    }
}



