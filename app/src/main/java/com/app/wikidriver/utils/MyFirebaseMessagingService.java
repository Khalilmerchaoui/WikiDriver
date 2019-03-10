package com.app.wikidriver.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i("TAGGED", remoteMessage.getFrom());
        if(remoteMessage.getData().size() > 0) {
            Log.i("TAGGED", remoteMessage.getData().toString());
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }
}
