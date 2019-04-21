package app.m26.wikidriver.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import app.m26.wikidriver.R;
import app.m26.wikidriver.activities.ChatActivity;
import app.m26.wikidriver.models.Messages;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.utils.Config;
import app.m26.wikidriver.utils.NotificationHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;


public class ChatNotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TAGGED", "Service Started");
        String mCurrentUserId = String.valueOf(Config.getCurrentUser(getApplicationContext()).getUserId());
        final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        Query lastElement = mRootRef.child("messages").child(mCurrentUserId);
        lastElement.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ((DatabaseReference) lastElement).child(postSnapshot.getKey()).limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.child("seen").getValue().equals(false) && dataSnapshot.child("notified").getValue().equals(false) && !dataSnapshot.child("from").getValue().equals(Config.getCurrentUser(getApplicationContext()).getUserId())) {
                                Messages messages = dataSnapshot.getValue(Messages.class);
                                messages.setNotified(true);
                                ((DatabaseReference) lastElement).child(postSnapshot.getKey()).child(dataSnapshot.getKey()).setValue(messages);
                                mRootRef.child(Config.FIREBASE_USERS_REFERENCE).child(dataSnapshot.child("from").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        String message = messages.getMessage();
                                        String thumb_image = user.getThumbnail();
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                notificationHelper = new NotificationHelper(ChatNotificationService.this);
                                                Intent intent = new Intent(ChatNotificationService.this, ChatActivity.class);
                                                intent.putExtra("user_id", user.getUserId());
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


                                                PendingIntent pendingIntent = PendingIntent.getActivity(ChatNotificationService.this, Config.NOTIFICATION_RANK, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                postNotification(Config.NOTIFICATION_RANK, user.getFirstName() + " " + user.getLastName(), thumb_image, message, pendingIntent);
                                                Config.NOTIFICATION_RANK++;
                                            } else {
                                                MessageReceivedNotification(user.getFirstName() + " " + user.getLastName(), message, thumb_image, postSnapshot.child("from").getValue().toString());
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
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
                    /* */
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    Bitmap LargeIcon;
    private void MessageReceivedNotification(final String chatter, final String message, String thumb_image, final String user_id) throws IOException {
        LargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.profile_icon);
        Picasso.with(this).load(thumb_image).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                LargeIcon = bitmap;

                Intent intent = new Intent(ChatNotificationService.this, ChatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Notification mBuilder = new Notification.Builder(ChatNotificationService.this)
                        .setContentTitle(chatter)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_message_received)
                        .setLargeIcon(getCircularBitmap(LargeIcon))
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setLights(144, 93, 58)
                        .setContentIntent(pendingIntent)
                        .build();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, mBuilder);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

    }

    public Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;
        if(bitmap != null) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } else {
                output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            float r = 0;

            if (bitmap.getWidth() > bitmap.getHeight()) {
                r = bitmap.getHeight() / 2;
            } else {
                r = bitmap.getWidth() / 2;
            }

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(r, r, r, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        } else
            return  BitmapFactory.decodeResource(getResources(), R.drawable.profile_icon);
    }

    NotificationHelper notificationHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void postNotification(int id, String title, String image, String url, PendingIntent pendingIntent) throws ExecutionException, InterruptedException {
        Notification.Builder notificationBuilder = null;
        notificationBuilder = notificationHelper.getNotification(pendingIntent, title, url);
        notificationBuilder.setLargeIcon(getCircularBitmap(new DownloadImageTask().execute(image).get()));
        if (notificationBuilder != null) {
            notificationHelper.notify(id, notificationBuilder);
        }
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }
}
