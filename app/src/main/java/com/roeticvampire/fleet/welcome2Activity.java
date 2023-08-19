package com.roeticvampire.fleet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class welcome2Activity extends AppCompatActivity {

    ImageButton login_btn,register_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        login_btn=findViewById(R.id.login_btn);
        register_btn=findViewById(R.id.register_btn);


        login_btn.setOnClickListener(v -> {
            Intent intent=new Intent(welcome2Activity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        });
        register_btn.setOnClickListener(v -> {
            Intent intent=new Intent(welcome2Activity.this, RegisterActivity.class);
            startActivity(intent);
            finish();

        });

    }
}