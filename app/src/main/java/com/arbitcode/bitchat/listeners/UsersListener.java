package com.arbitcode.bitchat.listeners;

import com.arbitcode.bitchat.models.User;

public interface UsersListener {

    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);

}
