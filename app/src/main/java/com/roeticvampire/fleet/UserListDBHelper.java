package com.roeticvampire.fleet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserListDBHelper extends SQLiteOpenHelper {

    public static final String USERLIST_DATABASE_NAME="userlist.db";
    public static final String USERLIST_TABLE_NAME="userlist_table";
    public static final String USERLIST_USERNAME="username"; //this'll be our ID for userlist
    public static final String USERLIST_NAME="name";
    public static final String USERLIST_PUBLIC_KEY="public_key"; //we'll add you later brother don't worry homie!
    public static final String USERLIST_CHAT_TABLE_NAME="chat_table_name";
    public static final String USERLIST_CHAT_TABLE_PREFIX="Fleet_";
    public static final String USERLIST_LAST_MESSAGE="last_message";
    public static final String USERLIST_LAST_MSG_TIMING="last_msg_time";
    public static final String USERLIST_PROFILE_PIC="profile_image";
    public static final int VERSION=1;
   /*
   0: _id
   1: name
   2: username
   3: chat table name
   4: last message blob
   5: last message time
   6: profile image blob
   7: public key blob
We'll add 7th as the Public Key blob
    */


    public UserListDBHelper(@Nullable Context context) {
        super(context, USERLIST_DATABASE_NAME, null, VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

       // String sql="create table "+USERLIST_TABLE_NAME+" (_id integer primary key autoincrement, "+USERLIST_NAME+" text,"+USERLIST_USERNAME+" text,"+USERLIST_CHAT_TABLE_NAME+" text, "+USERLIST_PUBLIC_KEY+" blob)";
        String sql="create table "+USERLIST_TABLE_NAME+" (_id integer primary key autoincrement, "+USERLIST_NAME+" text,"+USERLIST_USERNAME+" text,"+USERLIST_CHAT_TABLE_NAME+" text,last_message blob,last_msg_time text,profile_image blob,public_key blob)";
         db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+USERLIST_TABLE_NAME);
        onCreate(db);
    }
    public boolean insertUser (String name, String username, byte[] profile_pic,byte[] public_key) {
       Cursor cs=getUser(username);
       if(cs.getCount()>0)  return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERLIST_NAME, name);
        contentValues.put(USERLIST_USERNAME, username);
        contentValues.put(USERLIST_LAST_MESSAGE,"");
        contentValues.put(USERLIST_LAST_MSG_TIMING,"");
        contentValues.put(USERLIST_PROFILE_PIC,profile_pic);
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX+username);
        contentValues.put(USERLIST_PUBLIC_KEY,public_key);
        long p=db.insert(USERLIST_TABLE_NAME, null, contentValues);
        return p != -1;
    }

    public boolean insertUser (String name, String username, byte[] profile_pic,byte[] last_msg,byte[] public_key) {
        Cursor cs=getUser(username);
        if(cs.getCount()>0)  return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERLIST_NAME, name);
        contentValues.put(USERLIST_USERNAME, username);
        contentValues.put(USERLIST_LAST_MESSAGE,last_msg);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        contentValues.put(USERLIST_LAST_MSG_TIMING,String.valueOf(timestamp));
        contentValues.put(USERLIST_PROFILE_PIC,profile_pic);
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX+username);
        contentValues.put(USERLIST_PUBLIC_KEY,public_key);
        long p=db.insert(USERLIST_TABLE_NAME, null, contentValues);
        return p != -1;
    }


    public boolean deleteUser (String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+USERLIST_TABLE_NAME+" where "+USERLIST_USERNAME+" =?", new String[]{username});
        long p=db.delete(USERLIST_TABLE_NAME,USERLIST_USERNAME+" =?",new String[]{username} );
        return p != -1;
    }

    public Cursor getAllUsers () {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+USERLIST_TABLE_NAME+" ORDER BY "+ USERLIST_LAST_MSG_TIMING+" DESC", null);
        return cursor;
    }
    public Cursor getUser (String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+USERLIST_TABLE_NAME+" where "+USERLIST_USERNAME+" =?", new String[]{username});

        return cursor;
    }
    public boolean updateLastText(String username, byte[] last_message){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+USERLIST_TABLE_NAME+" where "+USERLIST_USERNAME+" =?", new String[]{username});
        ContentValues contentValues = new ContentValues();
        if(cursor.getCount()==0)return false;
        cursor.moveToFirst();
        contentValues.put(USERLIST_NAME, cursor.getString(1));
        contentValues.put(USERLIST_USERNAME, username);
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX+username);

        contentValues.put(USERLIST_LAST_MESSAGE,last_message);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        contentValues.put(USERLIST_LAST_MSG_TIMING,String.valueOf(timestamp));
        contentValues.put(USERLIST_PROFILE_PIC,cursor.getBlob(6));
        contentValues.put(USERLIST_PUBLIC_KEY,cursor.getBlob(7));
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX+username);



        long p=db.update(USERLIST_TABLE_NAME,contentValues,USERLIST_USERNAME+" =?",new String[]{username} );
        return p != -1;
    }

}
