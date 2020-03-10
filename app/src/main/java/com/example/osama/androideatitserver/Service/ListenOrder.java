package com.example.osama.androideatitserver.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.osama.androideatitserver.Model.Request;
import com.example.osama.androideatitserver.OrderStatus;
import com.example.osama.androideatitserver.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class ListenOrder extends Service {

    FirebaseDatabase db;
    DatabaseReference orders;
    private NotificationManager mNotificationManager;

    public ListenOrder() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseDatabase.getInstance();
        orders = db.getReference("Requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orders.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Request request = dataSnapshot.getValue(Request.class);
                if(request.getStatus().equals("0")){
                    showNotification(dataSnapshot.getKey(),request);
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
        return super.onStartCommand(intent, flags, startId);

    }

    
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    private void showNotification(String key, Request request) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(),"notify_002");

        Intent intent = new Intent(getBaseContext(), OrderStatus.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,0);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker("OSAMA")
                .setContentTitle("New Order")
                .setContentText("You have new Order #"+key)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(contentIntent); // to click notifiSwi

        mNotificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_002",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }
        // for show the notification must give unique id for each notifi
        int randomId = new Random().nextInt(9999-1)+1;

        mNotificationManager.notify(randomId,builder.build());


    }
}
