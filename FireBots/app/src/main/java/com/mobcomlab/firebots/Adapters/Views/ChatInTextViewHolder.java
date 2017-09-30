package com.mobcomlab.firebots.Adapters.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewNormal;

public class ChatInTextViewHolder extends RecyclerView.ViewHolder {

    public TextViewNormal senderName, textMessage;
    public TextView timestamp;

    public ChatInTextViewHolder(View itemView) {
        super(itemView);

        senderName = (TextViewNormal) itemView.findViewById(R.id.sender_name);
        textMessage = (TextViewNormal) itemView.findViewById(R.id.text_message);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);

        senderName.setText(null);
        textMessage.setText(null);
        timestamp.setText(null);
    }
}