package com.roeticvampire.fleet;

import android.graphics.Bitmap;
import android.net.Uri;

public class User {
    String name;
    String username;
    String email_id;
    //Object public_key;

    public User() {
    }

    public User(String name, String username, String email_id) {
        this.name = name;
        this.username = username;
        this.email_id = email_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

}
