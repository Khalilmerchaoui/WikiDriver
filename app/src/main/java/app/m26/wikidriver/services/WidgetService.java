package app.m26.wikidriver.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import app.m26.wikidriver.R;
import app.m26.wikidriver.activities.MainActivity;
import app.m26.wikidriver.utils.Config;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class WidgetService extends Service {

    private WindowManager windowManager;
    private RelativeLayout widgetLayout;
    public static PulsatorLayout pulsatorGreen, pulsatorGray;
    private LayoutInflater layoutInflater;
    public static FloatingActionButton fbWidget;
    public static Animation fab_in, fab_out;
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

        fab_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.design_fab_in);
        fab_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.design_fab_out);

        pulsatorGreen = widgetLayout.findViewById(R.id.pulsatorGreen);
        pulsatorGreen.start();

        pulsatorGray = widgetLayout.findViewById(R.id.pulsatorGray);
        pulsatorGray.start();

        fbWidget.setVisibility(View.VISIBLE);
        pulsatorGreen.setVisibility(View.VISIBLE);
        fbWidget.startAnimation(fab_in);


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

            private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
            private long lastPressTime;

            private boolean mHasDoubleClicked = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;
                    case MotionEvent.ACTION_UP:
                        finalTouchX = event.getRawX();
                        finalTouchY= event.getRawY();

                        long pressTime = System.currentTimeMillis();

                        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
                            Intent mainActivity = new Intent(WidgetService.this, MainActivity.class);
                            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainActivity);
                            //stopSelf();
                            mHasDoubleClicked = true;
                        }
                        else {     // If not double click....
                            mHasDoubleClicked = false;
                            Handler myHandler = new Handler() {
                                public void handleMessage(Message m) {
                                    if (!mHasDoubleClicked && isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                                        if(Config.isUserOnline(getApplicationContext())) {
                                            Config.exitAllAppsFromWidget(WidgetService.this, Config.getActivatedAppList(getApplicationContext()), "main", "");
                                            MainActivity.setToDefault();
                                            Config.setUserOnline(getApplicationContext(), false);
                                            stopService(new Intent(WidgetService.this, ListenerService.class));
                                            fbWidget.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                            pulsatorGreen.setVisibility(View.INVISIBLE);
                                            pulsatorGray.setVisibility(View.VISIBLE);
                                        } else {
                                            Intent mainActivity = new Intent(WidgetService.this, MainActivity.class);
                                            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(mainActivity);
                                        }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void hideFab() {
        if(fbWidget != null && pulsatorGreen != null && pulsatorGray != null) {
            fbWidget.setAnimation(fab_out);
            fbWidget.setVisibility(View.INVISIBLE);
            pulsatorGreen.setVisibility(View.INVISIBLE);
            pulsatorGray.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        fbWidget = null;
        windowManager.removeView(widgetLayout);
    }
}
