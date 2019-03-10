package com.app.wikidriver.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.wikidriver.services.AdvertNotificationService;
import com.app.wikidriver.services.ChatNotificationService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        context.startService(new Intent(context, ChatNotificationService.class));
        context.startService(new Intent(context, AdvertNotificationService.class));
    }
}
