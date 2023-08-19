package com.roeticvampire.fleet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class chatlistAdapter extends RecyclerView.Adapter<chatlistAdapter.ViewHolder> {
    private final List<chatlist_component> mData;
    private final LayoutInflater mInflater;
    private final Context context;
    // data is passed into the constructor
    chatlistAdapter(Context context, List<chatlist_component> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context=context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chatlist_recycler, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        chatlist_component animal = mData.get(position);
        holder.chatName.setText(animal.getChatName());
        holder.lastMessage.setText(animal.getLastMessage());
        holder.lastMassageTime.setText(TimeLogic.CustomTimeFormat(animal.getLastTextTime()));
        holder.userProfile.setImageBitmap(BitmapFactory.decodeByteArray(animal.getProfilePic(), 0, animal.getProfilePic().length));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(context, chatScreenActivity.class);
                intent.putExtra("username",mData.get(position).getChatUsername());
                context.startActivity(intent);
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView chatName;
        TextView lastMessage;
        TextView lastMassageTime;
        ImageView userProfile;
        LinearLayout linearLayout;
        ViewHolder(View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.recycler_UserTitle);
            lastMassageTime=itemView.findViewById(R.id.recycler_textTime);
            lastMessage=itemView.findViewById(R.id.recycler_lastText);
            userProfile=itemView.findViewById(R.id.profileImage);
            linearLayout=itemView.findViewById(R.id.linLayout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    // convenience method for getting data at click position
    chatlist_component getItem(int id) {
        return mData.get(id);
}
}
