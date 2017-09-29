package com.mobcomlab.firebots.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobcomlab.firebots.Adapters.MessageAdapter;
import com.mobcomlab.firebots.Constants;
import com.mobcomlab.firebots.Firebase.FBChatroom;
import com.mobcomlab.firebots.Firebase.FBConstant;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Helpers.DateHelper;
import com.mobcomlab.firebots.Helpers.DialogHelper;
import com.mobcomlab.firebots.Helpers.ImageHelper;
import com.mobcomlab.firebots.Helpers.PermissionHelper;
import com.mobcomlab.firebots.Models.Message;
import com.mobcomlab.firebots.Models.User;
import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewNormal;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends MainActivity implements MessageAdapter.OnMessageClickListener, MessageAdapter.OnMessageLongClickListener {

    // Properties
    // Properties permission
    private int PERMISSION_ALL = 1;
    private Intent imageCaptureIntent;
    private Intent imageGalleryIntent;

    // Properties post data from intent

    // Properties Firebase
    private DatabaseReference chatroomRef;
    private DatabaseReference chatroomReadRef;
    private DatabaseReference chatroomUserRef;
    private DatabaseReference chatroomTeacherRef;
    private DatabaseReference chatroomMessageRef;
    private Query messageQuery;
    private StorageReference chatroomStorageRef;
    private DatabaseReference userRef;
    private ChildEventListener chatroomUserListener;
    private ChildEventListener chatroomReadListener;
    private ChildEventListener updateThisUserBadgeListener;

    // Properties normal
    private TextViewNormal emptyStateTextView;
    private RecyclerView messageRecycler;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;

    private String thisUserLastReadMessageID = null;
    private Map<String, String> userLastReadMessageIDs = new HashMap<>();
    private Map<String, StorageReference> avatars = new HashMap<>();
    private ArrayList<String> memberIDs = new ArrayList<>();
    private EditText inputMessage;
    private Button sendButton;
    private int messageCount = 0;
    private String width = null;
    private String height = null;
    private File mediaMessageFile = null;
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

        // Initialize Database
        chatroomRef = FBChatroom.getChatroomRef();
        chatroomReadRef = FBChatroom.getChatroomRef().child(FBConstant.READ);
        chatroomUserRef = FBChatroom.getChatroomRef().child(FBConstant.USER);
        chatroomMessageRef = FBChatroom.getChatroomRef().child(FBConstant.MESSAGE);
        messageQuery = FBChatroom.getChatroomRef().child(FBConstant.MESSAGE).
                orderByChild(FBConstant.SENDING_TIME).limitToLast(5000);
        chatroomStorageRef = FBChatroom.getChatroomStorageRef();
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


        // Setup group button
//        ImageView icon = (ImageView) findViewById(R.id.toolbar_menu_icon);
//        icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_group));
//        icon.setVisibility(View.VISIBLE);
//        icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ChatActivity.this, GroupMembersActivity.class);
//                intent.putExtra(Constants.EXTRA_ACTIVITY_ID, activityID);
//                intent.putExtra(Constants.EXTRA_SCHOOL_ID, activity.schoolID);
//                intent.putExtra(Constants.EXTRA_SCHOOL_NAME, activity.schoolName);
//                ArrayList<String> bookedChildren = new ArrayList<>();
//                for (Package activityPackage : activity.packages) {
//                    for (String bookedChildID : activityPackage.bookedChildIDs) {
//                        bookedChildren.add(bookedChildID);
//                    }
//                }
//                intent.putStringArrayListExtra(Constants.EXTRA_BOOKED_CHILDREN_IDS, bookedChildren);
//                intent.putStringArrayListExtra(Constants.EXTRA_CHATROOM_MEMBER_IDS, memberIDs);
//                startActivity(intent);
//            }
//        });

        // Setup input toolbar
        ImageView uploadImageButton = (ImageView) findViewById(R.id.chat_image_upload_button);
        uploadImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
        uploadImageButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
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

        // Upload image on click
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] items = new String[3];
                items[0] = getResources().getString(R.string.take_photo);
                items[1] = getResources().getString(R.string.gallery);
                items[2] = getResources().getString(R.string.cancel);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle(getResources().getString(R.string.pick_image_from));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                imageCapture();
                                break;
                            case 1:
                                imageGallery();
                                break;
                            default:
                                break;
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
                chatroomReadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot readDataSnapshot) {
                        messageCount = (int) messageDataSnapshot.getChildrenCount();
                        if (messageCount > 0) {
                            if (readDataSnapshot.getChildrenCount() > 0) {
                                for (DataSnapshot userReadDataSnapshot : readDataSnapshot.
                                        getChildren()) {
                                    if (userReadDataSnapshot.getKey().equals(FBUser.uid)) {
                                        thisUserLastReadMessageID = userReadDataSnapshot.
                                                getValue().toString();
                                    } else {
                                        userLastReadMessageIDs.put(userReadDataSnapshot.getKey(),
                                                userReadDataSnapshot.getValue().toString());
                                    }
                                }
                            }
                            messageRecycler.setVisibility(View.VISIBLE);
                        } else {
                            if (readDataSnapshot.getChildrenCount() > 0) {
                                FBChatroom.removeChatroomUserRead();
                            }
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            DialogHelper.dismissIndicator();
                        }
                        messageAdapter = new MessageAdapter(messageQuery);
                        messageAdapter.setOnMessageClickListener(ChatActivity.this);
                        messageAdapter.setOnMessageLongClickListener(ChatActivity.this);
                        messageAdapter.setUserLastReadMessageIDs(userLastReadMessageIDs,
                                thisUserLastReadMessageID, messageCount - 1);
                        messageRecycler.setAdapter(messageAdapter);
                        messageAdapter.registerAdapterDataObserver(new RecyclerView.
                                AdapterDataObserver() {
                            @Override
                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                super.onItemRangeInserted(positionStart, itemCount);
                                if (positionStart >= messageCount - 1) {
                                    FBChatroom.updateChatroomUserRead(messageAdapter.
                                            getItem(positionStart).id);
                                }
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
                                    FBChatroom.removeChatroomUserRead();
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

                        observeRead();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

    private void observeRead() {
        chatroomReadListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(FBUser.uid) && !userLastReadMessageIDs.
                        containsKey(dataSnapshot.getKey())) {
                    userLastReadMessageIDs.put(dataSnapshot.getKey(),
                            dataSnapshot.getValue().toString());
                    messageAdapter.setUserLastReadMessageIDs(userLastReadMessageIDs);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(FBUser.uid)) {
                    userLastReadMessageIDs.put(dataSnapshot.getKey(),
                            dataSnapshot.getValue().toString());
                    messageAdapter.setUserLastReadMessageIDs(userLastReadMessageIDs);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        chatroomReadRef.addChildEventListener(chatroomReadListener);
    }

    private void imageCapture() {
        imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageCaptureIntent.resolveActivity(getPackageManager()) != null) {
            mediaMessageFile = null;
            try {
                mediaMessageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mediaMessageFile != null) {
                Uri uploadImageFileURI = FileProvider.getUriForFile(this,
                        "com.mobcomlab.firebots", mediaMessageFile);
                imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uploadImageFileURI);
                String[] PERMISSIONS = {Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!PermissionHelper.hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    startActivityForResult(imageCaptureIntent, Constants.REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new
                SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void imageGallery() {
        imageGalleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!PermissionHelper.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            //one can be replaced with any action code
            startActivityForResult(imageGalleryIntent, Constants.REQUEST_IMAGE_GALLERY);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (mediaMessageFile.exists()) {
                        try {
                            // modify orientation picture
                            Bitmap mediaMessageBitmap = ImageHelper.
                                    modifyOrientation(mediaMessageFile.getPath(), 1024);
                            width = String.valueOf(mediaMessageBitmap.getWidth());
                            height = String.valueOf(mediaMessageBitmap.getHeight());
                            mediaMessageFile.deleteOnExit();
                            mediaMessageFile = ImageHelper.
                                    saveToInternalStorage(mediaMessageBitmap, this);
                            sendMediaMessage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case Constants.REQUEST_IMAGE_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    mediaMessageFile = new File(ImageHelper.getRealPathFromURI(this, uri));
//                    Intent previewImage = new Intent(this,ChatPreviewImageActivity.class);
//                    Log.d("TEST",uri+" is Chat");
//                    previewImage.putExtra("URI", uri.toString());
//                    startActivity(previewImage);
                    try {
                        Bitmap mediaMessageBitmap = ImageHelper.modifyOrientation(mediaMessageFile.
                                getPath(), 1024); // modify orientation picture
                        width = String.valueOf(mediaMessageBitmap.getWidth());
                        height = String.valueOf(mediaMessageBitmap.getHeight());
                        mediaMessageFile = ImageHelper.saveToInternalStorage(mediaMessageBitmap, this);
                        sendMediaMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateBadge() {
//        chatroomUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getChildrenCount() > 0) {
//                    for (DataSnapshot chatroomUser : dataSnapshot.getChildren()) {
//                        if (!chatroomUser.getKey().equals(user.uid)) {
//                            int chatroomUserBadge = Integer.parseInt(chatroomUser.
//                                    getValue().toString());
//                            final Map<String, Object> chatroomUserBadgeUpdate = new HashMap<>();
//                            chatroomUserBadgeUpdate.put(chatroomUser.getKey(), chatroomUserBadge + 1);
//                            dataSnapshot.getRef().updateChildren(chatroomUserBadgeUpdate);
//
//                            userRef.child(chatroomUser.getKey()).child(FBConstant.BADGE).
//                                    addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot badgeDataSnapshot) {
//                                            int messageBadge = Integer.parseInt(badgeDataSnapshot.
//                                                    child(FBConstant.MESSAGE).getValue().toString());
//                                            final Map<String, Object> userBadgeUpdate = new HashMap<>();
//                                            userBadgeUpdate.put(FBConstant.MESSAGE, messageBadge + 1);
//                                            badgeDataSnapshot.getRef().updateChildren(userBadgeUpdate);
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//
//                                        }
//                                    });
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void updateThisUserBadge() {
//        updateThisUserBadgeListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                if (dataSnapshot.getKey().equals(user.uid)) {
//                    final int chatroomUserBadge = Integer.parseInt(dataSnapshot.
//                            getValue().toString());
//                    if(chatroomUserBadge > 0){
//                        final Map<String, Object> chatroomUserBadgeUpdate = new HashMap<>();
//                        chatroomUserBadgeUpdate.put(dataSnapshot.getKey(), 0);
//                        userRef.child(dataSnapshot.getKey()).child(FBConstant.BADGE).
//                                addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot badgeDataSnapshot) {
//                                        int messageBadge = Integer.parseInt(badgeDataSnapshot.
//                                                child(FBConstant.MESSAGE).getValue().toString());
//                                        final Map<String, Object> userBadgeUpdate = new HashMap<>();
//                                        int newBadge = messageBadge - chatroomUserBadge;
//                                        userBadgeUpdate.put(FBConstant.MESSAGE, newBadge == 0 ? newBadge : 0);
//                                        chatroomUserRef.updateChildren(chatroomUserBadgeUpdate);
//                                        badgeDataSnapshot.getRef().updateChildren(userBadgeUpdate);
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });
//                    }
//                }
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//        chatroomUserRef.addChildEventListener(updateThisUserBadgeListener);
    }


    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     */

    private void sendTextMessage(String textMessage) {
        DateTime lastMessageDate = null;
        width = null;
        height = null;

        if (messageAdapter.getItemCount() > 0) {
            lastMessageDate = messageAdapter.getItem(messageAdapter.getItemCount() - 1).sendingTime;
        }

        final DatabaseReference messageNewRef = chatroomMessageRef.push().getRef();
        Message message = new Message(
                messageNewRef.getKey(),
                user.uid,
                user.username,
                width,
                height,
                DateHelper.currentTime(),
                textMessage,
                lastMessageDate
        );
        messageNewRef.setValue(message.toMap());
        updateBadge();
        chatroomRef.child(FBConstant.TYPING_INDICATOR).child(user.uid).removeValue();
    }

    private void sendMediaMessage() {

        final DatabaseReference messageNewRef = chatroomMessageRef.push().getRef();
        final String key = messageNewRef.getKey();

        if (key != null && !key.equals("")) {
            DialogHelper.showIndicator(this, getResources().getString(R.string.uploading));
            final String imageName = String.valueOf((int) (System.currentTimeMillis() * 1000)) + ".jpg";

            UploadTask uploadTask;
            Uri file = Uri.fromFile(mediaMessageFile);
            uploadTask = chatroomStorageRef.child(user.uid).child(imageName).putFile(file);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        @SuppressWarnings("VisibleForTests") StorageMetadata metadata = task.getResult().getMetadata();
                        assert metadata != null;
                        DateTime lastMessageDate = null;
                        if (messageAdapter.getItemCount() > 0) {
                            lastMessageDate = messageAdapter.
                                    getItem(messageAdapter.getItemCount() - 1).sendingTime;
                        }
                        Message message = new Message(
                                key,
                                user.uid,
                                user.username,
                                width,
                                height,
                                DateHelper.currentTime(),
                                FirebaseStorage.getInstance().
                                        getReference().
                                        child(metadata.getPath()).toString(),
                                lastMessageDate
                        );
                        messageNewRef.setValue(message.toMap());
                        updateBadge();

                        // Delete file upload image
                        if (mediaMessageFile.exists()) {
                            mediaMessageFile.deleteOnExit();
                        }

                        DialogHelper.dismissIndicator();
                    } else {
                        String title = getResources().getString(R.string.image_upload_failed);
                        try {
                            throw task.getException();
                        } catch (FirebaseNetworkException e) {
                            title = getResources().getString(R.string.network_error);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DialogHelper.dismissIndicator();
                        DialogHelper.showOkAlert(ChatActivity.this, title, title, null);
                    }
                }
            });
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (permissions.length) {
            case 1:
                if (permissions[0].equals(Manifest.permission.CAMERA) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //zero can be replaced with any action code
                    startActivityForResult(imageCaptureIntent, Constants.REQUEST_IMAGE_CAPTURE);
                }
                break;
            case 2:
                boolean permissionsGrantedAll = true;
                for (int granResult : grantResults) {
                    if (granResult == PackageManager.PERMISSION_DENIED) {
                        permissionsGrantedAll = false;
                        break;
                    }
                }
                if (permissionsGrantedAll) {
                    //one can be replaced with any action code
                    startActivityForResult(imageGalleryIntent, Constants.REQUEST_IMAGE_GALLERY);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMessageClickListener(Message message) {
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(Constants.EXTRA_MESSAGE_ID, message.id);
        startActivity(intent);
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
        if (chatroomUserListener != null) {
            chatroomUserRef.removeEventListener(chatroomUserListener);
        }
        if (chatroomReadListener != null) {
            chatroomReadRef.removeEventListener(chatroomReadListener);
        }
        if (updateThisUserBadgeListener != null) {
            chatroomUserRef.removeEventListener(updateThisUserBadgeListener);
        }
    }
}



