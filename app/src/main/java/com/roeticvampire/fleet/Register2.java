package com.roeticvampire.fleet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Target;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Register2 extends AppCompatActivity {

    LinearLayout primaryScreen,secondaryOverlay;
    TextView tap_to_change;
    ImageView imageChooser;
    private static final int IMAGE_PICK_CODE=1000;
    private static final int PERMISSION_CODE=1001;
    String name,username,email_id;
    ImageButton register_btn;
    PrivateKey user_privateKey;
    PublicKey user_publicKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent= getIntent();
        name=intent.getStringExtra("name");
        username=intent.getStringExtra("username");
        email_id=intent.getStringExtra("email_id");


        setContentView(R.layout.activity_register2);
        primaryScreen=findViewById(R.id.default_layout);
        secondaryOverlay=findViewById(R.id.waitingOverlay);

        secondaryOverlay.setVisibility(View.INVISIBLE);
        tap_to_change=findViewById(R.id.rand);
        imageChooser=findViewById(R.id.profileImage);
        imageChooser.setMinimumHeight(imageChooser.getWidth());
        tap_to_change.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                imageChooser.setImageResource(R.drawable.default_profile_image);
                return false;
            }
        });

        imageChooser.setImageResource(R.drawable.default_profile_image);


        imageChooser.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT>+Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions ={Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                }
            }//maga iff
        pickImageFromGallery();

        });





register_btn=findViewById(R.id.Continue_btn);
        register_btn.setOnClickListener(v->{

            primaryScreen.setAlpha(0.2f);
            secondaryOverlay.setVisibility(View.VISIBLE);


            //_________________________________________________________________________________________________
            try {
                KeyPair kp= RSAEncyption.generateKeyPair();
                user_privateKey=kp.getPrivate();
                user_publicKey=kp.getPublic();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            SharedPreferences keySharedPrefs=getSharedPreferences("Personal_keys", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor1 = keySharedPrefs.edit();
            String encodedPrivateKey = Base64.encodeToString(user_privateKey.getEncoded(), Base64.DEFAULT);
            String encodedPublicKey = Base64.encodeToString(user_publicKey.getEncoded(), Base64.DEFAULT);

            editor1.putString("privateKey",encodedPrivateKey);
            editor1.putString("publicKey",encodedPublicKey);
            editor1.commit();
            //_________________________________________________________________________________________________




            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = ((BitmapDrawable) imageChooser.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images/"+username+".jpg");


            StorageReference publicKeyRef = storageRef.child("Public_Keys/"+username+".key");

            UploadTask uploadPublicKey = publicKeyRef.putBytes(user_publicKey.getEncoded());
            uploadPublicKey.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    UploadTask uploadTask = imagesRef.putBytes(data);
                    //the immediate bottom one will have to be triggered after we cooking it raw uploading PublicKey in firebase Storage
                    {uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(Register2.this, "Oops, something's fishy!\nWanna try again?", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Users");
                            myRef.child(username).setValue(new User(name,username,email_id));

                            SharedPreferences sharedpreferences = getSharedPreferences("personal_details", Context.MODE_PRIVATE);
                            Intent intent= new Intent (Register2.this,ChatlistActivity.class);
                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            editor.putString("name", name);
                            editor.putString("username", username);
                            editor.putString("email_id", email_id);

                            String encodedImage = Base64.encodeToString(data, Base64.DEFAULT);
                            editor.putString("image_data",encodedImage);
                            editor.commit();

                            //for now we'll just add the user here anyway
                            //___________________________________________________________________________________________________________
                            //UserListDBHelper userListDBHelper=new UserListDBHelper(Register2.this);
                            //userListDBHelper.insertUser(name,username,data);
                            //ChatlistDBHelper chatlistDBHelper=new ChatlistDBHelper(Register2.this);
                            //chatlistDBHelper.addUser("Fleet_"+username);
                            //chatlistDBHelper.insertMessage("Fleet_"+username,"Well, technically it's all one person",false);




                            //___________________________________________________________________________________________________________
                            //fine the territory for mayhem is marked
                            startActivity(intent);
                            finish();
                        }
                    });}
                }
            });





        });

    }

    private void pickImageFromGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    startActivityForResult(intent,IMAGE_PICK_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            CropImage.activity(data.getData()).setAspectRatio(1,1)
                    .start(this);


        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageChooser.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}