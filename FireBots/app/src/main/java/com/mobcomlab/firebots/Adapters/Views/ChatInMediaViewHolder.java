package com.mobcomlab.firebots.Adapters.Views;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewMedium;
import com.mobcomlab.firebots.Views.TextViewNormal;

public class ChatInMediaViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout dateLayout;
    public TextViewMedium date, unread;
    public CardView mediaMessage;
    public TextViewNormal senderName;
    public TextView timestamp;
    public ImageView mediaMessageImage;

    public ChatInMediaViewHolder(View itemView) {
        super(itemView);

        dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);
        date = (TextViewMedium) itemView.findViewById(R.id.date);
        unread = (TextViewMedium) itemView.findViewById(R.id.unread);
        senderName = (TextViewNormal) itemView.findViewById(R.id.sender_name);
        mediaMessage = (CardView) itemView.findViewById(R.id.media_message);
        mediaMessageImage = (ImageView) itemView.findViewById(R.id.media_message_image);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);

        dateLayout.setVisibility(View.GONE);
        date.setText(null);
        unread.setVisibility(View.GONE);
        senderName.setText(null);
        timestamp.setText(null);
    }
}