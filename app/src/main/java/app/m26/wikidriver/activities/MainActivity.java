package app.m26.wikidriver.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.app.diffpridriver.fragments.ChatListFragment;
import app.m26.wikidriver.fragments.ChatListFragment;
import app.m26.wikidriver.fragments.SocialFragment;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.services.AdvertNotificationService;
import app.m26.wikidriver.services.ChatNotificationService;
import app.m26.wikidriver.services.ListenerService;
import app.m26.wikidriver.services.StartAppsService;
import app.m26.wikidriver.services.WidgetService;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.R;;
import app.m26.wikidriver.fragments.AdvertFragment;
import app.m26.wikidriver.fragments.CalendarFragment;
import app.m26.wikidriver.fragments.SettingsFragment;
import app.m26.wikidriver.helpers.BottomNavigationViewHelper;

import static app.m26.wikidriver.services.WidgetService.fab_in;
import static app.m26.wikidriver.services.WidgetService.fbWidget;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();

            if(!Config.isConnectedToInternet(getApplicationContext()))
                Config.NetworkAlert(getApplicationContext());
            else
            switch (item.getItemId()) {
                case R.id.navigation_chat:
                    t.replace(R.id.frameLayout, new ChatListFragment());
                    t.commit();
                    return true;
                case R.id.navigation_advert:
                    t.replace(R.id.frameLayout, new AdvertFragment());
                    t.commit();
                    return true;
                case R.id.navigation_calendar:
                    //if(!Config.getCurrentUser(getApplicationContext()).getCountry().equals("null")) {
                        //Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                        //startActivity(intent);
                        //finish();
                    //} else {
                    t.replace(R.id.frameLayout, new CalendarFragment());
                    t.commit();
                    //}
                    return true;
                case R.id.navigation_social:
                    t.replace(R.id.frameLayout, new SocialFragment());
                    t.commit();
                    return true;
                case R.id.navigation_settings:
                    t.replace(R.id.frameLayout, new SettingsFragment());
                    t.commit();
                    return true;
            }
            return false;
        }
    };

    private boolean clicked = false;
    private TextView mTxtState;
    private Switch mSwitchState;
    private MenuItem prevMenuItem;
    private BottomNavigationView navigation;

    private Bundle bundle;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bundle = getIntent().getExtras();

        if(bundle != null) {
            boolean annonce = bundle.getBoolean("annonce");
            if(annonce) {
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.replace(R.id.frameLayout, new AdvertFragment());
                t.commit();
            }

        }
        //Toolbar mToolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);

        startService(new Intent(MainActivity.this, ChatNotificationService.class));
        startService(new Intent(MainActivity.this, AdvertNotificationService.class));

        Config.updateLocalUser(getApplicationContext());
        User currentuser = Config.getCurrentUser(getApplicationContext());
        Config.setAirportList(getApplicationContext(), currentuser.getCity(), currentuser.getCountry());

        mTxtState = findViewById(R.id.txtState);
        mSwitchState = findViewById(R.id.switchState);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.frameLayout, new AdvertFragment());
        t.commit();

        if(!Config.isConnectedToInternet(getApplicationContext()))
            Config.NetworkAlert(getApplicationContext());

        if(!Config.isUserOnline(getApplicationContext())) {
            mTxtState.setText(getResources().getString(R.string.offline));
            mSwitchState.setChecked(false);
        } else {
            mTxtState.setText(getResources().getString(R.string.online));
            mSwitchState.setChecked(true);
        }

        if (!Settings.canDrawOverlays(getApplicationContext())) {
            showDrawOverAppsSettings();
        }
        if(!Config.isAccessibilitySettingsOn(getApplicationContext())) {
            showAccessibilitySettings();
        }

        mSwitchState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                User currentUser = Config.getCurrentUser(getApplicationContext());

                if(Settings.canDrawOverlays(getApplicationContext()) && Config.isAccessibilitySettingsOn(getApplicationContext())){
                    Config.setUserOnline(getApplicationContext(), mSwitchState.isChecked());
                    if (checked) {
                        mTxtState.setText(getResources().getString(R.string.online));
                        //startService(new Intent(MainActivity.this, StartAppsService.class));
                        startService(new Intent(MainActivity.this, ListenerService.class));
                        /*if(fbWidget != null) {
                            Animation fab_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.design_fab_in);
                            fbWidget.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                            fbWidget.setVisibility(View.VISIBLE);
                            fbWidget.startAnimation(fab_in);
                        }*/
                        //finish();
                    } else {
                        mTxtState.setText(getResources().getString(R.string.offline));
                        setToDefault();
                        stopService(new Intent(MainActivity.this, ListenerService.class));
                        //Config.exitAllAppsFromSwitch(MainActivity.this, Config.getActivatedAppList(getApplicationContext()), "main", "");
                    }
                    Config.setCurrentUser(getApplicationContext(), currentUser);
                    Config.updateOnlineUser(getApplicationContext());
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.enable_all_settings), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //TODO uncomment the following
        if(Config.needPermissionForBlocking(getApplicationContext())) {
            showSettingsDialog();
        }
        //startService(new Intent(MainActivity.this, CloseAppsService.class));
    }

    public static void setToDefault() {
        Config.appsStarted = false;
        Config.finishedCourse = false;
        Config.currentApp = "";
        Config.waitingState = false;
        Config.onCourseState = false;
    }

    private void showAccessibilitySettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.accessibility_title));
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.accessibility_content));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoAccessibilitySettings(getApplicationContext());

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDrawOverAppsSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.draw_title));
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.draw_content));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1);
                }

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void gotoAccessibilitySettings(Context context) {
        Intent settingsIntent = new Intent(
                Settings.ACTION_ACCESSIBILITY_SETTINGS);
        if (!(context instanceof Activity)) {
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        boolean isOk = true;
        try {
            context.startActivity(settingsIntent);
        } catch (ActivityNotFoundException e) {
            isOk = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1) {
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.usage_title));
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.usage_content));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        WidgetService.hideFab();

        if(Config.appsStarted) {
            Config.appsStarted = false;
            Config.waitingState = true;
        }

        if(Config.finishedCourse) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                String Switch = getIntent().getStringExtra("switch");
                if(Switch.equals("on")) {
                    mTxtState.setText(getResources().getString(R.string.online));
                    mSwitchState.setChecked(true);
                } else {
                    mTxtState.setText(getResources().getString(R.string.offline));
                    mSwitchState.setChecked(false);
                }
            }
            Config.finishedCourse = false;
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.click_back_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUserOffline();
    }


    @Override
    protected void onStop() {
        super.onStop();

        /*User user = Config.getCurrentUser(getApplicationContext());
        if(user != null) {
            Config.setCurrentUser(getApplicationContext(), user);
            Config.updateLocalUser(getApplicationContext());
            Config.updateOnlineUser(getApplicationContext());
        }*/

        if(Config.isUserOnline(getApplicationContext()) && fbWidget == null)
            startService(new Intent(MainActivity.this, WidgetService.class));
        if(fbWidget != null) {
            fbWidget.setVisibility(View.VISIBLE);
            fbWidget.startAnimation(fab_in);
            if(Config.isUserOnline(getApplicationContext()))
                fbWidget.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

        }

        if(!Config.isUserOnline(getApplicationContext()))
            stopService(new Intent(MainActivity.this, WidgetService.class));
    }

    private void setUserOffline() {
        User user = Config.getCurrentUser(getApplicationContext());
        if(user != null) {
            Config.setCurrentUser(getApplicationContext(), user);
            Config.updateLocalUser(getApplicationContext());
            Config.updateOnlineUser(getApplicationContext());
        }
    }

}
