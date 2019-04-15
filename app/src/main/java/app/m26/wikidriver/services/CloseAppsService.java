package app.m26.wikidriver.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.utils.Config;

public class CloseAppsService extends Service {
    public CloseAppsService() {
    }

    ImageView imageView;
    RelativeLayout relativeLayout;
    @Override
    public void onCreate() {
        super.onCreate();
        showWaitScreen();
    }

    private void showWaitScreen() {

        final WindowManager.LayoutParams p;
        p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            p.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

// Define the position of the window within the screen
        p.gravity = Gravity.TOP | Gravity.RIGHT;
        p.x = 0;
        p.y = 100;

        LayoutInflater layoutInflater =
                (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.closeapps_layout, null);

        final WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        windowManager.addView(popupView, p);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                windowManager.removeView(popupView);
                stopSelf();
            }
        }, Config.getActivatedAppList(getApplicationContext()).size() *  5000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
