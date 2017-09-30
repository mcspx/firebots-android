package com.mobcomlab.firebots.Adapters.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewNormal;

public class ChatOutTextViewHolder extends RecyclerView.ViewHolder {

    public TextViewNormal textMessage;
    public TextView timestamp;

    public ChatOutTextViewHolder(View itemView) {
        super(itemView);

        textMessage = (TextViewNormal) itemView.findViewById(R.id.text_message);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);

        textMessage.setText(null);
        timestamp.setText(null);
    }
}