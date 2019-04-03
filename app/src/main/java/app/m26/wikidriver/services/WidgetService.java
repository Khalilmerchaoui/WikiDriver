package app.m26.wikidriver.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import app.m26.wikidriver.R;
import app.m26.wikidriver.activities.MainActivity;
import app.m26.wikidriver.utils.Config;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private RelativeLayout widgetLayout;
    private LayoutInflater layoutInflater;
    private FloatingActionButton fbWidget;
    private int CLICK_ACTION_THRESHOLD = 200;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        setTheme(R.style.AppTheme);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        widgetLayout = (RelativeLayout) layoutInflater.inflate(R.layout.widget_layout, null);

        fbWidget = widgetLayout.findViewById(R.id.fbWidget);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        windowManager.addView(widgetLayout, params);

        fbWidget.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private float finalTouchX;
            private float finalTouchY;
            private long startTime, duration;
            private int clickCount = 0;
            private boolean doubleTap = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        startTime = System.currentTimeMillis();
                        clickCount++;
                        return true;
                    case MotionEvent.ACTION_UP:
                        finalTouchX = event.getRawX();
                        finalTouchY= event.getRawY();

                        long time = System.currentTimeMillis() - startTime;
                        duration=  duration + time;
                        if(clickCount == 2) {
                            if (duration <= 200) {
                                doubleTap = true;
                                Log.i("tagged", "doubletap");
                                Intent mainActivity = new Intent(WidgetService.this, MainActivity.class);
                                mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainActivity);
                                stopSelf();
                            }
                            clickCount = 0;
                            duration = 0;
                        }
                        //TODO fix bug differentiating double click from single click.
                        /*if(!doubleTap && isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                            MainActivity.setToDefault();
                            Config.setUserOnline(getApplicationContext(), false);
                            stopService(new Intent(WidgetService.this, ListenerService.class));
                            Config.exitAllAppsFromWidget(WidgetService.this, Config.getActivatedAppList(getApplicationContext()), "main", "");
                        }*/
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX - (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(widgetLayout, params);
                        return true;
                }
                return false;
            }
        });

    }

    //TODO add bouncing effect
    //TODO add widget functionality
    //TODO add widget fading when clicked
    //TODO add icon


    private boolean isClicked(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(widgetLayout);
    }
}
