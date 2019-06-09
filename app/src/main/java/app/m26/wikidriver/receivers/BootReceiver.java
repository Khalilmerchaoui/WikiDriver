package app.m26.wikidriver.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.m26.wikidriver.services.AdvertNotificationService;
import app.m26.wikidriver.services.ChatNotificationService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        context.startService(new Intent(context, ChatNotificationService.class));
        context.startService(new Intent(context, AdvertNotificationService.class));
    }
}
