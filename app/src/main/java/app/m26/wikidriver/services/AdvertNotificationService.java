package app.m26.wikidriver.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.AddCommentActivity;
import app.m26.wikidriver.models.Publication;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.utils.NotificationHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdvertNotificationService extends Service {
    public AdvertNotificationService() {
    }

    private FirebaseDatabase database;
    private DatabaseReference advertReference;

    @Override
    public void onCreate() {
        super.onCreate();

        database = FirebaseDatabase.getInstance();
        advertReference = database.getReference(Config.FIREBASE_PUBLICATION_REFERENCE);

        advertReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Publication publication = dataSnapshot.getValue(Publication.class);
                User user = Config.getCurrentUser(getApplicationContext());
                if(!publication.getUserId().equals(user.getUserId()) && publication.getCity().equals(user.getCity()) && publication.getCountry().equals(user.getCountry())) {
                    publicationNotification(publication);
                    Log.i("tagged", "publication added");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void publicationNotification(Publication publication) {

        String title = "";
        switch(publication.getType()) {
            case Config.PUBLICATION_TYPE_SALE:
                title = getResources().getString(R.string.sale_rent);
                break;
            case Config.PUBLICATION_TYPE_MECHANICAL:
                title = getResources().getString(R.string.mechanical_bodywork);
                break;
            case Config.PUBLICATION_TYPE_COURSE:
                title = getResources().getString(R.string.course_driver);
                break;
            case Config.PUBLICATION_TYPE_OTHER:
                title = getResources().getString(R.string.other);
                break;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            NotificationHelper notificationHelper = new NotificationHelper(AdvertNotificationService.this);

            Intent intent = new Intent(AdvertNotificationService.this, AddCommentActivity.class);
            intent.putExtra("publicationId", publication.getPublicationId());
            intent.putExtra("reference", Config.FIREBASE_PUBLICATION_REFERENCE);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            PendingIntent pendingIntent = PendingIntent.getActivity(AdvertNotificationService.this, Config.NOTIFICATION_RANK, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder notificationBuilder = null;
            notificationBuilder = notificationHelper.getNotification(pendingIntent, title, publication.getContent());
            if (notificationBuilder != null) {
                notificationHelper.notify(Config.NOTIFICATION_RANK, notificationBuilder);
            }
            Config.NOTIFICATION_RANK++;
        } else {
            Intent intent = new Intent(AdvertNotificationService.this, AddCommentActivity.class);
            intent.putExtra("publicationId", publication.getPublicationId());
            intent.putExtra("reference", Config.FIREBASE_PUBLICATION_REFERENCE);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Notification mBuilder = new Notification.Builder(AdvertNotificationService.this)
                    .setContentTitle(title)
                    .setContentText(publication.getContent())
                    .setSmallIcon(R.drawable.ic_baseline_announcement)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setLights(144, 93, 58)
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
