package com.app.wikidriver.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.app.wikidriver.utils.Config;

public class StartAppsService extends Service {
    public StartAppsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Config.appsStarted = true;
        Config.launchApps(getApplicationContext(), Config.getActivatedAppList(getApplicationContext()));
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
