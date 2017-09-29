package com.mobcomlab.firebots.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mobcomlab.firebots.Constants;
import com.mobcomlab.firebots.Firebase.FBChatroom;
import com.mobcomlab.firebots.Firebase.FBConstant;
import com.mobcomlab.firebots.Helpers.DialogHelper;
import com.mobcomlab.firebots.Helpers.PermissionHelper;
import com.mobcomlab.firebots.Models.Message;
import com.mobcomlab.firebots.R;

import java.io.File;
import java.io.FileNotFoundException;

public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private LinearLayout imagePreviewToolbar;
    private String messageId;
    private Uri imageUri;
    private String downloadOrShare;
    private final String download = "download";
    private final String share = "share";
    private Message message;

    String Download_ID = "DOWNLOAD_ID";

    SharedPreferences preferenceManager;
    DownloadManager downloadManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        DialogHelper.showIndicator(this, getResources().getString(R.string.loading));

        final ImageView imageView = (ImageView) findViewById(R.id.image_preview);
        imagePreviewToolbar = (LinearLayout) findViewById(R.id.image_preview_toolbar);
        final ImageView saveButton = (ImageView) findViewById(R.id.image_preview_save);
        final ImageView shareButton = (ImageView) findViewById(R.id.image_preview_share);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        saveButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_save));
        saveButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        shareButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_share));
        shareButton.setColorFilter(ContextCompat.getColor(this, R.color.white));

        messageId = getIntent().getStringExtra(Constants.EXTRA_MESSAGE_ID);

        FBChatroom.getChatroomRef().child(FBConstant.MESSAGE).child(messageId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        message = new Message(dataSnapshot);
                        if (message.photoRef != null) {
                            Glide.with(ImagePreviewActivity.this).using(new FirebaseImageLoader()).
                                    load(message.photoRef).into(imageView);
                            imageView.setOnClickListener(ImagePreviewActivity.this);
                            saveButton.setOnClickListener(ImagePreviewActivity.this);
                            shareButton.setOnClickListener(ImagePreviewActivity.this);
                            message.photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUri = uri;
                                }
                            });
                            DialogHelper.dismissIndicator();
                        } else {
                            Glide.clear(imageView);
                            imageView.setImageDrawable(null);
                            DialogHelper.dismissIndicator();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        DialogHelper.dismissIndicator();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_preview:
                if (imagePreviewToolbar.getVisibility() == View.GONE) {
                    imagePreviewToolbar.setVisibility(View.VISIBLE);
                } else {
                    imagePreviewToolbar.setVisibility(View.GONE);
                }
                break;
            case R.id.image_preview_save:
                downloadOrShare = download;
                if (!PermissionHelper.hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    DownloadImage(imageUri);
                }
                break;
            case R.id.image_preview_share:
                downloadOrShare = share;
                if (!PermissionHelper.hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    ShareImage(imageUri);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        unregisterReceiver(downloadReceiver);
    }

    private void DownloadImage(Uri uri) {
        String imageName = "image_" + messageId + ".jpg";
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("FireBots");
        request.setDescription("FireBots image");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator +
                "FireBots" + File.separator + imageName);
        long download_id = downloadManager.enqueue(request);

        //Save the download id
        SharedPreferences.Editor PrefEdit = preferenceManager.edit();
        PrefEdit.putLong(Download_ID, download_id);
        PrefEdit.apply();
    }

    private void ShareImage(Uri uri) {
        DialogHelper.showIndicator(this, getResources().getString(R.string.loading));
        Glide.with(getApplicationContext())
                .load(uri)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        // Do something with bitmap here.
                        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                                bitmap, "Image Description", null);
                        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                        startActivity(Intent.createChooser(shareIntent, "Share image using"));
                        DialogHelper.dismissIndicator();
                    }
                });
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(preferenceManager.getLong(Download_ID, 0));
            Cursor cursor = downloadManager.query(query);

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    //Retrieve the saved download id
                    long downloadID = preferenceManager.getLong(Download_ID, 0);

                    ParcelFileDescriptor file;
                    try {
                        file = downloadManager.openDownloadedFile(downloadID);
                        Toast.makeText(ImagePreviewActivity.this,
                                getResources().getString(R.string.downloaded),
                                Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Toast.makeText(ImagePreviewActivity.this,
                                e.toString(),
                                Toast.LENGTH_LONG).show();
                    }

                } else if (status == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(ImagePreviewActivity.this,
                            getResources().getString(R.string.failed) + reason,
                            Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    Toast.makeText(ImagePreviewActivity.this,
                            getResources().getString(R.string.paused) + reason,
                            Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    Toast.makeText(ImagePreviewActivity.this,
                            getResources().getString(R.string.running),
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionsGrantedAll = true;
        for (int granResult : grantResults) {
            if (granResult == PackageManager.PERMISSION_DENIED) {
                permissionsGrantedAll = false;
                break;
            }
        }
        if (permissionsGrantedAll) {
            switch (downloadOrShare) {
                case download:
                    DownloadImage(imageUri);
                    break;
                case share:
                    ShareImage(imageUri);
                    break;
                default:
                    break;
            }
        }
    }
}
