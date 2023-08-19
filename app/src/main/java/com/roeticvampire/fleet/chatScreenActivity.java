package com.roeticvampire.fleet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class chatScreenActivity extends AppCompatActivity {
    chatboxAdapter adapter;
    ImageButton send_btn;
    Handler handler;
    EditText sendText;
    RecyclerView recyclerView;
    ImageButton back_btn;
    ImageView profileImage;
    TextView nameView,usernameView;
    String name;
    String username;
    String tableName;
    Bitmap profileImageData;
    DatabaseReference myRef;
    PublicKey publicKey;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        ArrayList<Message> messageArrayList = new ArrayList<>();


        Intent intent=getIntent();
        name= intent.getStringExtra("name");
        username= intent.getStringExtra("username");
        Cursor cesr=((CustomApplication)getApplication()).userListDBHelper.getUser(username);
        if(cesr.getCount()>0){
            while(cesr.moveToNext()){
                name=cesr.getString(1);
                byte[] b=cesr.getBlob(6);
                profileImageData = BitmapFactory.decodeByteArray(b, 0, b.length);

                try {
                    publicKey=((CustomApplication)getApplication()).kf.generatePublic(new X509EncodedKeySpec(cesr.getBlob(7)));
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
        }


       tableName="Fleet_"+username;
        myRef =  FirebaseDatabase.getInstance().getReference("messages").child(username);
        UserListDBHelper userListDbHelper = new UserListDBHelper(this);

        ChatlistDBHelper chatlistDBHelper=new ChatlistDBHelper(this);
        chatlistDBHelper.addUser(tableName);

        Cursor csr= chatlistDBHelper.getAllMessages(tableName);
        if(csr.getCount()>0){
            while(csr.moveToNext()){
                int msgId=csr.getInt(0);
                boolean isUser= csr.getInt(1) == 1;
                String msg = null;
                try {
                    msg = RSAEncyption.decryptData(csr.getBlob(2),((CustomApplication)getApplication()).user_PrivateKey);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String timing= csr.getString(3);
                messageArrayList.add(new Message(msgId,isUser,msg,timing));
            }
        }

        // set up the RecyclerView
        recyclerView = findViewById(R.id.ChatScreenRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new chatboxAdapter(this, messageArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messageArrayList.size() - 1);
        back_btn=findViewById(R.id.back_btn);
        profileImage=findViewById(R.id.profileImage);
        usernameView=findViewById(R.id.user_username);
        nameView=findViewById(R.id.user_name);
        sendText=findViewById(R.id.username_input);
        send_btn=findViewById(R.id.sendText_btn);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {Cursor csr;
                try{
                    csr = chatlistDBHelper.getNewMessages(tableName, messageArrayList.get(messageArrayList.size() - 1).msgId);
                }
                catch (Exception erer){
                    csr = chatlistDBHelper.getNewMessages(tableName, 0);
                }
                if(csr.getCount()>0){
                    while(csr.moveToNext()){
                        int msgId=csr.getInt(0);
                        boolean isUser= csr.getInt(1) == 1;
                        String msg= null;
                        try {
                            msg = RSAEncyption.decryptData(csr.getBlob(2),((CustomApplication)getApplication()).user_PrivateKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String timing= csr.getString(3);
                        messageArrayList.add(new Message(msgId,isUser,msg,timing));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                }

                handler.postDelayed(this,1000);
            }


        },1000);




        back_btn.setOnClickListener(v -> finish());
        profileImage.setImageBitmap(profileImageData);
        usernameView.setText("@"+username);
        nameView.setText(name);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(sendText.getText().toString().length()>0) {
                    //messageArrayList.add(new Message(true, sendText.getText().toString(), "now"));

                   //here'we're sending the msgs, to ourselves right now coz yay
                   try {
                       FirebaseMessage fb=new FirebaseMessage(((CustomApplication)getApplication()).getUsername(),Base64.encodeToString(RSAEncyption.encryptData(sendText.getText().toString(), publicKey),Base64.DEFAULT));
                       //FirebaseMessage fb=new FirebaseMessage(((CustomApplication)getApplication()).getUsername(),(sendText.getText().toString()));

                       myRef.push().setValue(fb);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }


                   //will change this one later I guess
                   try {
                       chatlistDBHelper.insertMessage(tableName,RSAEncyption.encryptData(sendText.getText().toString(), ((CustomApplication) getApplication()).getUser_PublicKey()),true);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   try {
                       userListDbHelper.updateLastText(username,RSAEncyption.encryptData(sendText.getText().toString(), ((CustomApplication) getApplication()).getUser_PublicKey()));
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   try {
                       updateChats();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    sendText.setText("");
                }


            }

            private void updateChats() throws IOException {int _id=0;
                try{
                    _id = messageArrayList.get(messageArrayList.size() - 1).msgId;
                }
catch (Exception e){}

                Cursor csr=chatlistDBHelper.getNewMessages(tableName,_id);
                if(csr.getCount()>0){
                    while(csr.moveToNext()){
                        int msgId=csr.getInt(0);
                        boolean isUser= csr.getInt(1) == 1;
                        String msg= RSAEncyption.decryptData(csr.getBlob(2),((CustomApplication) getApplication()).getUser_PrivateKey());
                        String timing= csr.getString(3);
                        messageArrayList.add(new Message(msgId,isUser,msg,timing));


                    }
                }

            }

            private void gettingAllUserDetails() {
                Cursor csr=userListDbHelper.getAllUsers();
                if(csr.getCount()>0){
                    while(csr.moveToNext()){
                        String str="";
                        str+="\nID: "+csr.getInt(0);
                        str+="\nNAME: "+csr.getString(1);
                        str+="\nUSERNAME: "+csr.getString(2);
                        str+="\nTABLE NAME: "+csr.getString(3);
                    }
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}