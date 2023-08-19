package com.roeticvampire.fleet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;

public class CustomApplication extends Application {
    ChatlistDBHelper chatlistDBHelper;
    UserListDBHelper userListDBHelper;
    String username="";
    PrivateKey user_PrivateKey;
    PublicKey user_PublicKey;
    byte[] emptyMsg;

    KeyFactory kf = null; // or "EC" or whatever
    public PrivateKey getUser_PrivateKey() {
        return user_PrivateKey;
    }

    public PublicKey getUser_PublicKey() {
        return user_PublicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        SharedPreferences keySharedPrefs=getSharedPreferences("Personal_keys", Context.MODE_PRIVATE);

        String encodedPrivateKey = keySharedPrefs.getString("privateKey","");
        String encodedPublicKey = keySharedPrefs.getString("publicKey","");
        if( !encodedPrivateKey.equalsIgnoreCase("") ){
            byte[] b = Base64.decode(encodedPrivateKey, Base64.DEFAULT);
            try {
                user_PrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(b));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        if( !encodedPublicKey.equalsIgnoreCase("") ){
            byte[] b = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            try {
                user_PublicKey =kf.generatePublic(new X509EncodedKeySpec(b));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        //okay so now we have both of the keys we made in place xD

        try {
            emptyMsg=RSAEncyption.encryptData("",user_PublicKey);
        } catch (IOException e) {
            e.printStackTrace();
        }






        chatlistDBHelper=new ChatlistDBHelper(getApplicationContext());
        userListDBHelper=new UserListDBHelper(getApplicationContext());


        // Required initialization logic here!
    }
    public void startIncomingMsgs(){
        //we gotta add a child listener, but uske liye pehle realtime ka database refer krna hoga
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("messages").child(username);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FirebaseMessage fb = snapshot.getValue(FirebaseMessage.class);

                //process it into a msg, then delete this shit
                if(userListDBHelper.getUser(fb.getUsername()).getCount()>0)
                {
                    try {
                       chatlistDBHelper.insertMessage("Fleet_" + fb.getUsername(), Base64.decode(fb.getMessage(),Base64.DEFAULT), false);
                        // chatlistDBHelper.insertMessage("Fleet_" + fb.getUsername(),fb.getMessage(), false);
                         userListDBHelper.updateLastText(fb.getUsername(), Base64.decode(fb.getMessage(), Base64.DEFAULT));
                            //userListDBHelper.updateLastText(fb.getUsername(),fb.getMessage());



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else
                    {
                    chatlistDBHelper.addUser("Fleet_" + fb.getUsername());
                        try {
                            chatlistDBHelper.insertMessage("Fleet_" + fb.getUsername(), Base64.decode(fb.getMessage(),Base64.DEFAULT), false);
                            //chatlistDBHelper.insertMessage("Fleet_" + fb.getUsername(),fb.getMessage(), false);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("Users").child(fb.getUsername());
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User tempUser=snapshot.getValue(User.class);
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference imagesRef = storageRef.child("images/"+tempUser.getUsername()+".jpg");
                                imagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes->{
                                    StorageReference publicKeyRef = storageRef.child("Public_Keys/"+tempUser.getUsername()+".key");
                                    publicKeyRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes1 -> {

                                        userListDBHelper.insertUser(tempUser.getName(),tempUser.getUsername(),bytes,Base64.decode(fb.getMessage(), Base64.DEFAULT),bytes1);
                                    });

                                });






                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });





                }

                myRef.child(snapshot.getKey()).removeValue();


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
