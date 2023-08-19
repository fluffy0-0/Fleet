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

public class ChatlistDBHelper extends SQLiteOpenHelper {

    public static final String CHATS_DATABASE_NAME="chatlist.db";
    public static final String CHATS_IS_USER="isUser";
    public static final String CHATS_MESSAGE= "message";
    public static final String CHATS_MESSAGE_TIME="messageTime";

    public static final int IS_USER=1;
    public static final int IS_OTHER=0;
    public static final int VERSION=1;
   /*
<each_chat>.sql should have: sent_or_recieved(boolean) /the message/ time
    */


    public ChatlistDBHelper(@Nullable Context context) {
        super(context, CHATS_DATABASE_NAME, null, VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // String sql="create table "+USERLIST_TABLE_NAME+" (_id integer primary key autoincrement, "+USERLIST_NAME+" text,"+USERLIST_USERNAME+" text,"+USERLIST_CHAT_TABLE_NAME+" text, "+USERLIST_PUBLIC_KEY+" blob)";

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void addUser(String userTableName){
        String sql="create table if not exists "+userTableName+" (_id integer primary key autoincrement, "+CHATS_IS_USER+" integer,"+CHATS_MESSAGE+" blob,"+CHATS_MESSAGE_TIME+" timestamp)";
        this.getWritableDatabase().execSQL(sql);

    }


    public boolean insertMessage (String tableName, byte[] message,boolean isUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if(isUser)
        contentValues.put(CHATS_IS_USER, IS_USER);
        else
        contentValues.put(CHATS_IS_USER, IS_OTHER);

        contentValues.put(CHATS_MESSAGE, message);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        contentValues.put(CHATS_MESSAGE_TIME, String.valueOf(timestamp));
        long p=db.insert(tableName, null, contentValues);
        return p != -1;
    }

    public boolean deleteMsg (String tableName, int msg_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+tableName+" where _id =?", new String[]{String.valueOf(msg_id)});
        long p=db.delete(tableName,"_id =?", new String[]{String.valueOf(msg_id)} );
        return p != -1;
    }

    public Cursor getAllMessages (String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+tableName, null);
        return cursor;
    }


    public Cursor getNewMessages(String tableName, int last_msg_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+tableName+" where _id >?", new String[]{String.valueOf(last_msg_id)});
        return cursor;
    }
}
