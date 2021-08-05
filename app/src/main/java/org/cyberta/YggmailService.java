package org.cyberta;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import yggmail.Yggmail_;

public class YggmailService extends Service {

    public static final String ACTION_STOP = "ACTION_STOP";
    private static final String TAG = YggmailService.class.getSimpleName();

    public YggmailService() {
    }

    YggmailNotificationManager notificationManager ;
    Yggmail_ yggmail;


    public class LocalBinder extends Binder {
        public YggmailService getService() {
            return YggmailService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = new YggmailNotificationManager(getApplicationContext());

        yggmail = new Yggmail_();
        yggmail.setLogger(s -> Log.d("YGGMAIL error", s));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = notificationManager.buildForegroundServiceNotification();
        startForeground(YggmailNotificationManager.YGGMAIL_NOTIFICATION_ID, notification);

        String action = intent != null ? intent.getAction() : "";
        if (ACTION_STOP.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.d(TAG, getApplicationContext().getFilesDir().getPath()+"/yggmail.db");
        yggmail.setDatabaseName(getApplicationContext().getFilesDir().getPath()+"/yggmail.db");

        yggmail.createPasswordLogError("1324asdf");
        yggmail.startLogError("localhost:1025",
                "localhost:1143",
                false,
                "tcp://45.138.172.192:5001,tcp://94.130.203.208:5999,tcp://bunkertreff.ddns.net:5454,tcp://ygg.mkg20001.io:80,tcp://yugudorashiru.de:80");


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelNotifications();
        if (yggmail != null) {
            yggmail.stop();
        }
    }
}