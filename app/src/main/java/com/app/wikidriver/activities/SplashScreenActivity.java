package com.app.wikidriver.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.app.wikidriver.R;
import com.app.wikidriver.utils.Config;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Config.getCurrentUser(getApplicationContext()) != null) {
                    Intent mainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                } else {
                    Intent registerActivity = new Intent(SplashScreenActivity.this, LandingActivity.class);
                    startActivity(registerActivity);
                }
                finish();
            }
        }, 2300);
    }
}
