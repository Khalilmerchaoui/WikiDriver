package com.app.wikidriver.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.app.wikidriver.R;
import com.app.wikidriver.adapters.AppsListAdapter;
import com.app.wikidriver.models.App;
import com.app.wikidriver.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class SelectAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<App> appList  = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.select_apps_title));

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(Config.needPermissionForBlocking(getApplicationContext())) {
            showSettingsDialog();
        }
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            showDrawOverAppsSettings();
        }
        if(!Config.isAccessibilitySettingsOn(getApplicationContext())) {
            showAccessibilitySettings();
        }
        appList = Config.getAllInstalledApps(getApplicationContext());

        AppsListAdapter appsListAdapter = new AppsListAdapter(getApplicationContext(), appList);
        recyclerView.setAdapter(appsListAdapter);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_selectapps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_confirm)
            confirmSelections();

        return super.onOptionsItemSelected(item);
    }

    private void confirmSelections() {
        Config.setAppList(getApplicationContext(), AppsListAdapter.getAppList());
        startActivity(new Intent(SelectAppsActivity.this, MainActivity.class));
        SelectAppsActivity.this.finish();
    }
}
