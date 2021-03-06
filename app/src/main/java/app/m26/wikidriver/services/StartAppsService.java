package app.m26.wikidriver.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import app.m26.wikidriver.utils.Config;

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
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
