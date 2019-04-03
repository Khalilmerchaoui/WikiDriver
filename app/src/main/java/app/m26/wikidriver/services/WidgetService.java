package app.m26.wikidriver.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

            private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
            private long lastPressTime;

            private boolean mHasDoubleClicked = false;

            private GestureDetector gestureDetector = new GestureDetector(WidgetService.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    Log.i("tagged", "onSingleTap");
                    return super.onSingleTapConfirmed(e);
                }
            });

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

                        long pressTime = System.currentTimeMillis();


                        // If double click...
                        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
                            Intent mainActivity = new Intent(WidgetService.this, MainActivity.class);
                            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainActivity);
                            //stopSelf();
                            mHasDoubleClicked = true;
                        }
                        else {     // If not double click....
                            mHasDoubleClicked = false;
                            Handler myHandler = new Handler() {
                                public void handleMessage(Message m) {
                                    if (!mHasDoubleClicked && isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                                        MainActivity.setToDefault();
                                        Config.setUserOnline(getApplicationContext(), false);
                                        stopService(new Intent(WidgetService.this, ListenerService.class));
                                        Config.exitAllAppsFromWidget(WidgetService.this, Config.getActivatedAppList(getApplicationContext()), "main", "");
                                        fbWidget.setBackgroundColor(Color.GRAY);
                                    }
                                }
                            };
                            myHandler.sendMessageDelayed(new Message(),DOUBLE_PRESS_INTERVAL);
                        }
                        // record the last time the menu button was pressed.
                        lastPressTime = pressTime;

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

    private boolean isClicked(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }

    //TODO add bouncing effect
    //TODO add widget functionality
    //TODO add widget fading when clicked
    //TODO add icon

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
