package com.roeticvampire.fleet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class ChatlistActivity extends AppCompatActivity {
    chatlistAdapter adapter;
    ImageView User_profile_image;
    String name,username,email_id;
    TextView username_view,name_view,email_id_view;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    Handler handler;
    String newUserUsername;
    int btn_code=0; //0 for find user, 1 for found hogya chalo ab add krdo entry me
    ImageView logoIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);
        logoIcon=findViewById(R.id.logoicon2);



        logoIcon.setOnLongClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ChatlistActivity.this,"You've been signed out.",Toast.LENGTH_SHORT);
           return false;
        });
        SharedPreferences sharedpreferences = getSharedPreferences("personal_details", Context.MODE_PRIVATE);


        name=sharedpreferences.getString("name","");
        username=sharedpreferences.getString("username","");
        ((CustomApplication)ChatlistActivity.this.getApplication()).setUsername(username);
        //now we gotta start looking for the merceneries!!!
        ((CustomApplication)ChatlistActivity.this.getApplication()).startIncomingMsgs();



        email_id=sharedpreferences.getString("email_id","");
        String previouslyEncodedImage = sharedpreferences.getString("image_data", "");
        User_profile_image=findViewById(R.id.profileImage);
        name_view=findViewById(R.id.user_name);
        email_id_view=findViewById(R.id.user_email);
        username_view=findViewById(R.id.user_username);
        floatingActionButton=findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v->{
            floatingActionButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));


            //startActivity(new Intent(getApplicationContext(),AddNewUserActivity.class));
            Dialog dialog= new Dialog(this);
            dialog.setContentView(R.layout.adduser_fragment);
            dialog.setTitle("This is my custom dialog box");
            dialog.setCancelable(true);


            dialog.setOnCancelListener(dialog1 -> {
                floatingActionButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rev_rotate));
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha=1f;
                getWindow().setAttributes(lp);
            });


            WindowManager.LayoutParams lp = getWindow().getAttributes();
             lp.alpha=0.6f;
             getWindow().setAttributes(lp);

            EditText searchbar;
            TextView userPrompt;
            ProgressBar loading_spinner;
            btn_code=0; //0 for find user, 1 for found hogya chalo ab add krdo entry me

            Button search_btn;
            searchbar=dialog.findViewById(R.id.search_username);
            userPrompt=dialog.findViewById(R.id.userPrompt);
            search_btn=dialog.findViewById(R.id.addUserButton);
            loading_spinner=dialog.findViewById(R.id.loading_spinner);
            loading_spinner.setVisibility(View.INVISIBLE);





            search_btn.setOnClickListener(ve->{
                newUserUsername=searchbar.getText().toString();
                if(newUserUsername.length()>0) {

                    userPrompt.setVisibility(View.INVISIBLE);
                    loading_spinner.setVisibility(View.VISIBLE);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Users");
                    if (btn_code == 0) {
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                loading_spinner.setVisibility(View.INVISIBLE);
                                if (snapshot.hasChild(newUserUsername)) {


                                    userPrompt.setTextColor(getResources().getColor(R.color.green_success));
                                    userPrompt.setText("User found");
                                    userPrompt.setVisibility(View.VISIBLE);
                                    btn_code = 1;
                                    search_btn.setText("ADD");


                                } else {
                                    userPrompt.setTextColor(getResources().getColor(R.color.red_error));
                                    userPrompt.setText("User not found");
                                    userPrompt.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        //btn is now code 1, user is found
                        myRef.child(newUserUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User tempUser = snapshot.getValue(User.class);
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference imagesRef = storageRef.child("images/" + tempUser.getUsername() + ".jpg");
                                imagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                                    StorageReference publicKeyRef = storageRef.child("Public_Keys/"+tempUser.getUsername()+".key");
                                    publicKeyRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes2->{
                                        UserListDBHelper userListDBHelper = new UserListDBHelper(getApplicationContext());
                                        userListDBHelper.insertUser(tempUser.getName(), tempUser.getUsername(), bytes,((CustomApplication)getApplication()).emptyMsg,bytes2);
                                        dialog.dismiss();
                                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                                        lp.alpha=1f;
                                        getWindow().setAttributes(lp);
                                    });
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }
            });
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.show();
        });
        name_view.setText(name);
        username_view.setText(username);
        email_id_view.setText(email_id);




        if( !previouslyEncodedImage.equalsIgnoreCase("") ){
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            User_profile_image.setImageBitmap(bitmap);
        }


        ArrayList<chatlist_component> chatListMembers = new ArrayList<>();
        recyclerView = findViewById(R.id.chatlist_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new chatlistAdapter(this, chatListMembers);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //retrieiving all user details
        UserListDBHelper userListDBHelper=new UserListDBHelper(this);
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor csr=userListDBHelper.getAllUsers();
                if(csr.getCount()>0){
                    chatListMembers.clear();
                    while(csr.moveToNext()){
                        String name= csr.getString(1);
                        String username= csr.getString(2);
                        String last_message= null;
                        try {
                            last_message = RSAEncyption.decryptData(csr.getBlob(4),((CustomApplication)getApplication()).user_PrivateKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String last_message_time=csr.getString(5);
                        byte[] profile_image=csr.getBlob(6);
                        chatListMembers.add(new chatlist_component(name,username,last_message,profile_image,last_message_time));

                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                }

                handler.postDelayed(this,3000);
            }


        },0);
        // set up the RecyclerView



    }
}