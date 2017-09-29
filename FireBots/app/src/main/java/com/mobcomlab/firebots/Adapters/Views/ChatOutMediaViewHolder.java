package com.mobcomlab.firebots.Adapters.Views;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewMedium;

public class ChatOutMediaViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout dateLayout;
    public TextViewMedium date, unread;
    public CardView mediaMessage;
    public TextView read, timestamp;
    public ImageView mediaMessageImage;

    public ChatOutMediaViewHolder(View itemView) {
        super(itemView);
        dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);
        date = (TextViewMedium) itemView.findViewById(R.id.date);
        unread = (TextViewMedium) itemView.findViewById(R.id.unread);
        mediaMessage = (CardView) itemView.findViewById(R.id.media_message);
        mediaMessageImage = (ImageView) itemView.findViewById(R.id.media_message_image);
        read = (TextView) itemView.findViewById(R.id.read);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);

        dateLayout.setVisibility(View.GONE);
        date.setText(null);
        unread.setVisibility(View.GONE);
        read.setText(null);
        read.setVisibility(View.GONE);
        timestamp.setText(null);
    }
}