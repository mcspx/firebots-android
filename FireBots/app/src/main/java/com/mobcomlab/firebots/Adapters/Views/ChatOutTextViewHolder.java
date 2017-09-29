package com.mobcomlab.firebots.Adapters.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.TextViewMedium;
import com.mobcomlab.firebots.Views.TextViewNormal;

public class ChatOutTextViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout dateLayout;
    public TextViewMedium date, unread;
    public TextViewNormal textMessage;
    public TextView read, timestamp;

    public ChatOutTextViewHolder(View itemView) {
        super(itemView);

        dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);
        date = (TextViewMedium) itemView.findViewById(R.id.date);
        unread = (TextViewMedium) itemView.findViewById(R.id.unread);
        textMessage = (TextViewNormal) itemView.findViewById(R.id.text_message);
        read = (TextView) itemView.findViewById(R.id.read);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);

        dateLayout.setVisibility(View.GONE);
        date.setText(null);
        unread.setVisibility(View.GONE);
        textMessage.setText(null);
        read.setText(null);
        read.setVisibility(View.GONE);
        timestamp.setText(null);
    }
}