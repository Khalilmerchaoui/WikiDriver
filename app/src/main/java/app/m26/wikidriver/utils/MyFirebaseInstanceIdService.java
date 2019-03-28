package app.m26.wikidriver.utils;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Config.FCM_ID = FirebaseInstanceId.getInstance().getToken();
        Log.i("TAGGED", "FCM : " + Config.FCM_ID);
    }
}