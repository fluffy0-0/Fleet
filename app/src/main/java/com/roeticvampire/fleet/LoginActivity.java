package com.roeticvampire.fleet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText user_email_input, user_password_input;
    ImageButton login_btn;
    LinearLayout primaryScreen,secondaryOverlay;
    TextView forgot_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        primaryScreen=findViewById(R.id.primaryView);
secondaryOverlay=findViewById(R.id.waitingOverlay);
        SharedPreferences keycheck=getSharedPreferences("Personal_keys", Context.MODE_PRIVATE);
        if(keycheck.getString("privateKey","").equalsIgnoreCase("")){
            //we fooked up
            //user_email_input.setError("Can't Proceed with the login.\nPlease create a new account.");
            Toast.makeText(this, "Can't Proceed with the login.\nPlease create a new account.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                //invoke the SecondActivity.
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                //the current activity will get finished.
            }, 3000);
        }

        SharedPreferences sharedpreferences = getSharedPreferences("personal_details", Context.MODE_PRIVATE);
        String prev_email=sharedpreferences.getString("email_id","");









        mAuth = FirebaseAuth.getInstance();
        forgot_password=findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(v->{
            FirebaseAuth.getInstance().sendPasswordResetEmail(user_email_input.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(LoginActivity.this,"Password reset email sent.",Toast.LENGTH_SHORT);
                }
            });
        });
        user_email_input =findViewById(R.id.input_email);
        user_password_input =findViewById(R.id.login_password);
        login_btn=findViewById(R.id.Continue_btn);
        login_btn.setOnClickListener(v -> {
            //
            String email_id= user_email_input.getText().toString();
            String password= user_password_input.getText().toString();
            if(!prev_email.equals(email_id)){
                user_email_input.setError("Please Log in with previously used email.");
                Toast.makeText(LoginActivity.this,"Please Log in with previously used email.",Toast.LENGTH_SHORT);

            }
            //now we gotta check the regex for both our Strings
            else if(verifyCredentials(email_id,password)){
                primaryScreen.setAlpha(0.2f);
                secondaryOverlay.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email_id,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            //Successful login, so we need to import the user details, that we saved back in register2 activity
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Users");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot user: snapshot.getChildren()){
                                        User tempUser=user.getValue(User.class);
                                        if(tempUser.getEmail_id().equals(email_id)){
                                            SharedPreferences sharedpreferences = getSharedPreferences("personal_details", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putString("name", tempUser.getName());
                                            editor.putString("username", tempUser.getUsername());
                                            editor.putString("email_id", email_id);
                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                            StorageReference storageRef = storage.getReference();
                                            StorageReference imagesRef = storageRef.child("images/"+tempUser.getUsername()+".jpg");
                                            imagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {

                                                StorageReference publicKeyRef = storageRef.child("Public_Keys/"+tempUser.getUsername()+".key");
                                                publicKeyRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes1 -> {
                                                    SharedPreferences.Editor editor1=keycheck.edit();
                                                    String encodedPublicKey = Base64.encodeToString(bytes1, Base64.DEFAULT);
                                                    editor1.putString("publicKey",encodedPublicKey);
                                                    editor1.commit();
                                                    String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                                                    editor.putString("image_data", encodedImage);
                                                    editor.commit();
                                                    Intent intent = new Intent(LoginActivity.this, ChatlistActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                });

                                            });

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            }
                        else
                            Toast.makeText(LoginActivity.this, "Wrong email or password!", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


    }

    private boolean verifyCredentials(String email_id, String password) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if( !pat.matcher(email_id).matches())
            return false;
        return password.length() <= 15 && password.length() >= 6;
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}