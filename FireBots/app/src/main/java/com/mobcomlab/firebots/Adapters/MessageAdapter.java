package com.mobcomlab.firebots.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.mobcomlab.firebots.Adapters.Views.ChatInTextViewHolder;
import com.mobcomlab.firebots.Adapters.Views.ChatOutTextViewHolder;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Models.Message;
import com.mobcomlab.firebots.R;

public class MessageAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int OUTGOING_TEXT_MESSAGE = 2;
    private static final int INCOMING_TEXT_MESSAGE = 3;

    public interface OnMessageLongClickListener {
        void onMessageLongClickListener(Message message);
    }

    private OnMessageLongClickListener messageLongClickListener;

    /**
     * @param ref The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *            combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public MessageAdapter(Query ref) {
        super(Message.class, R.layout.chat_incoming_text_message, RecyclerView.ViewHolder.class, ref);
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    @Override
    protected Message parseSnapshot(DataSnapshot dataSnapshot) {
        return new Message(dataSnapshot);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.senderId.equals(FBUser.uid)) {
            return OUTGOING_TEXT_MESSAGE;
        } else {
            return INCOMING_TEXT_MESSAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
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
        switch (viewHolder.getItemViewType()) {
            case OUTGOING_TEXT_MESSAGE:
                final ChatOutTextViewHolder chatOutTextViewHolder = (ChatOutTextViewHolder) viewHolder;

                chatOutTextViewHolder.textMessage.setText(message.text);
                chatOutTextViewHolder.timestamp.setText(message.getSendingTimeString());

                chatOutTextViewHolder.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (messageLongClickListener != null) {
                            messageLongClickListener.onMessageLongClickListener(message);
                        }
                        return true;
                    }
                });
                break;
            case INCOMING_TEXT_MESSAGE:
                final ChatInTextViewHolder chatInTextViewHolder = (ChatInTextViewHolder) viewHolder;

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
                break;
            default:
                break;
        }
    }
}
