package com.roeticvampire.fleet;

import android.content.Context;
import android.icu.text.RelativeDateTimeFormatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.util.List;

public class chatboxAdapter extends RecyclerView.Adapter<chatboxAdapter.ViewHolder> {
    private final List<Message> mData;
    private final LayoutInflater mInflater;

    private static final int USER_TEXT=1;
    private static final int OTHER_TEXT=2;

    // data is passed into the constructor
    chatboxAdapter(Context context, List<Message> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==USER_TEXT) {
            view = mInflater.inflate(R.layout.chatbox_user, parent, false);
        }
        else  {
            view = mInflater.inflate(R.layout.chatbox_other, parent, false);
        }
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message animal = mData.get(position);
        holder.lastMessage.setText(animal.getMessageContent());
        holder.lastMessageTime.setText(TimeLogic.CustomTimeFormat(animal.getMessageTime()));

    }



    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(mData.get(position).isUser) return USER_TEXT;
        else return OTHER_TEXT;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView lastMessage;
        TextView lastMessageTime;


        ViewHolder(View itemView) {
            super(itemView);

            lastMessageTime=itemView.findViewById(R.id.messageTime);
            lastMessage=itemView.findViewById(R.id.messageContent);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }

    }

    // convenience method for getting data at click position
    Message getItem(int id) {
        return mData.get(id);
    }
}
