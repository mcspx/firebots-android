package com.mobcomlab.firebots.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.mobcomlab.firebots.Adapters.Views.ChatInMediaViewHolder;
import com.mobcomlab.firebots.Adapters.Views.ChatInTextViewHolder;
import com.mobcomlab.firebots.Adapters.Views.ChatOutMediaViewHolder;
import com.mobcomlab.firebots.Adapters.Views.ChatOutTextViewHolder;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Helpers.DateHelper;
import com.mobcomlab.firebots.Models.Message;
import com.mobcomlab.firebots.R;

import java.util.HashMap;
import java.util.Map;

public class MessageAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int OUTGOING_MEDIA_MESSAGE = 0;
    private static final int INCOMING_MEDIA_MESSAGE = 1;
    private static final int OUTGOING_TEXT_MESSAGE = 2;
    private static final int INCOMING_TEXT_MESSAGE = 3;

    public interface OnMessageClickListener {
        void onMessageClickListener(Message message);
    }

    public interface OnMessageLongClickListener {
        void onMessageLongClickListener(Message message);
    }

    private OnMessageClickListener messageClickListener;
    private OnMessageLongClickListener messageLongClickListener;
    private Map<String, String> userLastReadMessageIDs = new HashMap<>();
    private String thisUserLastReadMessageID = null;
    private int firstTimeLastMessageIndex = 0;

    /**
     * @param ref The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *            combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public MessageAdapter(Query ref) {
        super(Message.class, R.layout.chat_incoming_text_message, RecyclerView.ViewHolder.class, ref);
    }

    public void setOnMessageClickListener(OnMessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public void setUserLastReadMessageIDs(Map<String, String> userLastReadMessageIDs) {
        this.userLastReadMessageIDs = userLastReadMessageIDs;
        notifyDataSetChanged();
    }

    public void setUserLastReadMessageIDs(Map<String, String> userLastReadMessageIDs, String thisUserLastReadMessageID, int firstTimeLastMessageIndex) {
        this.userLastReadMessageIDs = userLastReadMessageIDs;
        this.thisUserLastReadMessageID = thisUserLastReadMessageID;
        this.firstTimeLastMessageIndex = firstTimeLastMessageIndex;
    }

    @Override
    protected Message parseSnapshot(DataSnapshot dataSnapshot) {
        return new Message(dataSnapshot);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.isMediaMessage) {
            if (message.senderId.equals(FBUser.uid)) {
                return OUTGOING_MEDIA_MESSAGE;
            } else {
                return INCOMING_MEDIA_MESSAGE;
            }
        } else {
            if (message.senderId.equals(FBUser.uid)) {
                return OUTGOING_TEXT_MESSAGE;
            } else {
                return INCOMING_TEXT_MESSAGE;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
            case OUTGOING_MEDIA_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_outgoing_media_message, parent, false);
                return new ChatOutMediaViewHolder(itemView);
            case INCOMING_MEDIA_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_incoming_media_message, parent, false);
                return new ChatInMediaViewHolder(itemView);
            case OUTGOING_TEXT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_outgoing_text_message, parent, false);
                return new ChatOutTextViewHolder(itemView);
            case INCOMING_TEXT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_incoming_text_message, parent, false);
                return new ChatInTextViewHolder(itemView);
            default:
                return null;
        }
    }

    @Override
    protected void populateViewHolder(final RecyclerView.ViewHolder viewHolder, final Message message, final int position) {
        final Context context = viewHolder.itemView.getContext();
        int width = message.width.equals("") ? 0 : Integer.parseInt(message.width);
        int height = message.height.equals("") ? 0 : Integer.parseInt(message.height);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int widthDevice = metrics.densityDpi;
        int fixWidth;
        int fixHeight;
        //fix image size
        switch (getScaleDevice(widthDevice)){
            //xxDpi
            case 12 : fixWidth = (width * getScaleDevice(widthDevice)) / 6;
                fixHeight = (height * getScaleDevice(widthDevice)) / 6;
                break;
            //xDpi
            case 8 : fixWidth = (width * getScaleDevice(widthDevice)) / 8;
                fixHeight = (height * getScaleDevice(widthDevice)) / 8;
                break;
            //hDpi
            case 6 : fixWidth = (width * getScaleDevice(widthDevice)) / 12;
                fixHeight = (height * getScaleDevice(widthDevice)) / 12;
                break;
            //mDpi
            case 4 : fixWidth = (width * getScaleDevice(widthDevice)) / 14;
                fixHeight = (height * getScaleDevice(widthDevice)) / 14;
                break;
            //lDpi
            case 3 : fixWidth = (width * getScaleDevice(widthDevice)) / 18;
                fixHeight = (height * getScaleDevice(widthDevice)) / 18;
                break;
            default: fixWidth = (width * getScaleDevice(widthDevice)) / 8;
                fixHeight = (height * getScaleDevice(widthDevice)) / 8;
                break;
        }
        //Change to dp for set image size
        int widthDp = pxToDp(context, fixWidth);
        int heightDp = pxToDp(context, fixHeight);
        switch (viewHolder.getItemViewType()) {
            case OUTGOING_MEDIA_MESSAGE:
                final ChatOutMediaViewHolder chatOutMediaViewHolder = (ChatOutMediaViewHolder) viewHolder;
                if (message.isFirstMessageOfDate) {
                    chatOutMediaViewHolder.date.setText(DateHelper.toDateString(message.sendingTime));
                    chatOutMediaViewHolder.dateLayout.setVisibility(View.VISIBLE);
                } else {
                    chatOutMediaViewHolder.dateLayout.setVisibility(View.GONE);
                    chatOutMediaViewHolder.date.setText(null);
                }
                if (message.photoRef != null) {
                    if (widthDp != 0 && heightDp != 0) {
                        chatOutMediaViewHolder.mediaMessageImage.getLayoutParams().width = widthDp;
                        chatOutMediaViewHolder.mediaMessageImage.getLayoutParams().height = heightDp;
                        Glide.with(context).using(new FirebaseImageLoader())
                                .load(message.photoRef)
                                .override(widthDp, heightDp)
                                .placeholder(ContextCompat.getDrawable(context,
                                        R.drawable.photo_message_placeholder))
                                .into(chatOutMediaViewHolder.mediaMessageImage);
                    } else {
                        Glide.with(context).using(new FirebaseImageLoader())
                                .load(message.photoRef)
                                .placeholder(ContextCompat.getDrawable(context,
                                        R.drawable.photo_message_placeholder))
                                .into(chatOutMediaViewHolder.mediaMessageImage);
                    }

                    chatOutMediaViewHolder.timestamp.setText(message.getSendingTimeString());
                    int read = getRead(message.id);
                    if (read > 0) {
                        chatOutMediaViewHolder.read.setText(context.getResources().getString(R.string.read) + " " + String.valueOf(read));
                        chatOutMediaViewHolder.read.setVisibility(View.VISIBLE);
                    } else {
                        chatOutMediaViewHolder.read.setVisibility(View.GONE);
                        chatOutMediaViewHolder.read.setText(null);
                    }

                    chatOutMediaViewHolder.mediaMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageClickListener != null) {
                                messageClickListener.onMessageClickListener(message);
                            }
                        }
                    });

                    chatOutMediaViewHolder.mediaMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (messageLongClickListener != null) {
                                messageLongClickListener.onMessageLongClickListener(message);
                            }
                            return true;
                        }
                    });
                } else {
                    Glide.clear(chatOutMediaViewHolder.mediaMessageImage);
                    chatOutMediaViewHolder.mediaMessageImage.setImageDrawable(null);
                }

                if (message.id.equals(thisUserLastReadMessageID)) {
                    if (message.id.equals(getItem(firstTimeLastMessageIndex).id)) {
                        chatOutMediaViewHolder.unread.setVisibility(View.GONE);
                    } else {
                        chatOutMediaViewHolder.unread.setVisibility(View.VISIBLE);
                    }
                } else {
                    chatOutMediaViewHolder.unread.setVisibility(View.GONE);
                }
                break;
            case INCOMING_MEDIA_MESSAGE:
                final ChatInMediaViewHolder chatInMediaViewHolder = (ChatInMediaViewHolder) viewHolder;

                //Check is first message of date (show date on top)
                if (message.isFirstMessageOfDate) {
                    chatInMediaViewHolder.date.setText(DateHelper.toDateString(message.sendingTime));
                    chatInMediaViewHolder.dateLayout.setVisibility(View.VISIBLE);
                } else {
                    chatInMediaViewHolder.dateLayout.setVisibility(View.GONE);
                    chatInMediaViewHolder.date.setText(null);
                }

                //Check id teacher to set color
                chatInMediaViewHolder.senderName.setTextColor(ContextCompat.getColor(context, R.color.textNormal));

                chatInMediaViewHolder.senderName.setText(message.senderName);

                if (message.photoRef != null) {
                    if (widthDp != 0 && heightDp != 0) {
                        chatInMediaViewHolder.mediaMessageImage.getLayoutParams().width = widthDp;
                        chatInMediaViewHolder.mediaMessageImage.getLayoutParams().height = heightDp;
                        Glide.with(context).using(new FirebaseImageLoader())
                                .load(message.photoRef)
                                .override(widthDp, heightDp)
                                .placeholder(ContextCompat.getDrawable(context,
                                        R.drawable.photo_message_placeholder))
                                .into(chatInMediaViewHolder.mediaMessageImage);
                    } else {
                        Glide.with(context).using(new FirebaseImageLoader())
                                .load(message.photoRef)
                                .placeholder(ContextCompat.getDrawable(context,
                                        R.drawable.photo_message_placeholder))
                                .into(chatInMediaViewHolder.mediaMessageImage);
                    }

                    chatInMediaViewHolder.timestamp.setText(message.getSendingTimeString());

                    chatInMediaViewHolder.mediaMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageClickListener != null) {
                                messageClickListener.onMessageClickListener(message);
                            }
                        }
                    });

                    chatInMediaViewHolder.mediaMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (messageLongClickListener != null) {
                                messageLongClickListener.onMessageLongClickListener(message);
                            }
                            return true;
                        }
                    });
                } else {
                    Glide.clear(chatInMediaViewHolder.mediaMessageImage);
                    chatInMediaViewHolder.mediaMessageImage.setImageDrawable(null);
                }

                if (message.id.equals(thisUserLastReadMessageID)) {
                    if (message.id.equals(getItem(firstTimeLastMessageIndex).id)) {
                        chatInMediaViewHolder.unread.setVisibility(View.GONE);
                    } else {
                        chatInMediaViewHolder.unread.setVisibility(View.VISIBLE);
                    }
                } else {
                    chatInMediaViewHolder.unread.setVisibility(View.GONE);
                }
                break;
            case OUTGOING_TEXT_MESSAGE:
                final ChatOutTextViewHolder chatOutTextViewHolder = (ChatOutTextViewHolder) viewHolder;

                if (message.isFirstMessageOfDate) {
                    chatOutTextViewHolder.date.setText(DateHelper.toDateString(message.sendingTime));
                    chatOutTextViewHolder.dateLayout.setVisibility(View.VISIBLE);
                } else {
                    chatOutTextViewHolder.dateLayout.setVisibility(View.GONE);
                    chatOutTextViewHolder.date.setText(null);
                }

                chatOutTextViewHolder.textMessage.setText(message.text);
                chatOutTextViewHolder.timestamp.setText(message.getSendingTimeString());
                int read = getRead(message.id);
                if (read > 0) {
                    chatOutTextViewHolder.read.setText(context.getResources().getString(R.string.read) + " " + String.valueOf(read));
                    chatOutTextViewHolder.read.setVisibility(View.VISIBLE);
                } else {
                    chatOutTextViewHolder.read.setVisibility(View.GONE);
                    chatOutTextViewHolder.read.setText(null);
                }

                chatOutTextViewHolder.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (messageLongClickListener != null) {
                            messageLongClickListener.onMessageLongClickListener(message);
                        }
                        return true;
                    }
                });

                if (message.id.equals(thisUserLastReadMessageID)) {
                    if (message.id.equals(getItem(firstTimeLastMessageIndex).id)) {
                        chatOutTextViewHolder.unread.setVisibility(View.GONE);
                    } else {
                        chatOutTextViewHolder.unread.setVisibility(View.VISIBLE);
                    }
                } else {
                    chatOutTextViewHolder.unread.setVisibility(View.GONE);
                }
                break;
            case INCOMING_TEXT_MESSAGE:
                final ChatInTextViewHolder chatInTextViewHolder = (ChatInTextViewHolder) viewHolder;

                if (message.isFirstMessageOfDate) {
                    chatInTextViewHolder.date.setText(DateHelper.toDateString(message.sendingTime));
                    chatInTextViewHolder.dateLayout.setVisibility(View.VISIBLE);
                } else {
                    chatInTextViewHolder.dateLayout.setVisibility(View.GONE);
                    chatInTextViewHolder.date.setText(null);
                }
                //Check id teacher to set color
                chatInTextViewHolder.senderName.setTextColor(ContextCompat.getColor(context, R.color.textNormal));
                chatInTextViewHolder.senderName.setText(message.senderName);

                chatInTextViewHolder.textMessage.setText(message.text);
                chatInTextViewHolder.timestamp.setText(message.getSendingTimeString());

                chatInTextViewHolder.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (messageLongClickListener != null) {
                            messageLongClickListener.onMessageLongClickListener(message);
                        }
                        return true;
                    }
                });

                if (message.id.equals(thisUserLastReadMessageID)) {
                    if (message.id.equals(getItem(firstTimeLastMessageIndex).id)) {
                        chatInTextViewHolder.unread.setVisibility(View.GONE);
                    } else {
                        chatInTextViewHolder.unread.setVisibility(View.VISIBLE);
                    }
                } else {
                    chatInTextViewHolder.unread.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    private int getRead(String messageID) {
        int read = 0;
        for (Map.Entry<String, String> userLastMessageID : userLastReadMessageIDs.entrySet()) {
            if (messageID.compareTo(userLastMessageID.getValue()) <= 0) {
                read += 1;
            }
        }
        return read;
    }

    private int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private int getScaleDevice(int densityDpi){
        int densityDevice;
        if(densityDpi < 140){
            densityDevice = 3; //lDpi
        }else if(densityDpi >= 140 && densityDpi < 186){
            densityDevice = 4; //mDpi and tvDpi
        }else if(densityDpi >= 186 && densityDpi < 280){
            densityDevice = 6; //hDpi
        }else if(densityDpi >= 280 && densityDpi < 400){
            densityDevice = 8; //xDpi
        }else{
            densityDevice = 12; //xxDpi and MORE
        }
        return densityDevice;
    }
}
