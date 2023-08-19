package com.roeticvampire.fleet;

public class Message {
    int msgId;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    boolean isUser;
    String messageContent;
    String messageTime;

    public Message(int msgId,boolean isUser, String messageContent, String messageTime) {
        this.msgId=msgId;
        this.isUser = isUser;
        this.messageContent = messageContent;
        this.messageTime = messageTime;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
