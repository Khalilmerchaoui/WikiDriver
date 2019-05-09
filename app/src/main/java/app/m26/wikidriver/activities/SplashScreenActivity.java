package app.m26.wikidriver.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;

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
