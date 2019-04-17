package app.m26.wikidriver.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import app.m26.wikidriver.activities.MainActivity;
import app.m26.wikidriver.models.App;
import app.m26.wikidriver.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class ListenerService extends Service {
    public ListenerService() {
    }

    private int mInterval = 500; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private List<String> packageNames = new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        for(App app : Config.getActivatedAppList(getApplicationContext()))
            packageNames.add(app.getPackageName());
        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(packageNames.contains(Config.getForegroundProcess(getApplicationContext())) && Config.waitingState) {
                    Config.waitingState = false;
                    Log.i("coursestate", "course started");
                    Config.currentApp = Config.getForegroundProcess(getApplicationContext());

                    List<App> backgroundApps = Config.getActivatedAppList(getApplicationContext());

                    List<App> backApps = new ArrayList<>();

                    for(App app : backgroundApps) {
                        if(!app.getPackageName().equals(Config.currentApp)) {
                            backApps.add(app);
                            Log.i("coursestate", app.getName() + "  killed.");
                        }
                    }
                }
                if(!Config.getForegroundProcess(getApplicationContext()).equals(Config.currentApp) && Config.onCourseState) {
                    Config.onCourseState = false;
                    Config.finishedCourse = true;
                    Intent startMain = new Intent(ListenerService.this, MainActivity.class);
                    startMain.putExtra("switch", "off");
                    startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startMain);

                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
