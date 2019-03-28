package app.m26.wikidriver.application;

import android.app.Application;

import app.m26.wikidriver.utils.Config;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.fabric.sdk.android.Fabric;

/**
 * Created by AkshayeJH on 01/07/17.
 */

public class ChatApp extends Application {

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /* Picasso */

        /*Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);*/

        mAuth = FirebaseAuth.getInstance();

        if(Config.getCurrentUser(getApplicationContext()) != null) {

            mUserDatabase = FirebaseDatabase.getInstance()
                    .getReference().child(Config.FIREBASE_USERS_REFERENCE).child(Config.getCurrentUser(getApplicationContext()).getUserId());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        long timeStamp = System.currentTimeMillis() / 1000;
                        mUserDatabase.child("status").onDisconnect().setValue(String.valueOf(timeStamp));
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }


}
