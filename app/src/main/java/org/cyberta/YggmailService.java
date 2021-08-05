package org.cyberta;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class YggmailService extends Service {

    public static final String ACTION_STOP = "ACTION_STOP";

    public YggmailService() {
    }

    YggmailNotificationManager notificationManager ;

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
        notificationManager.buildForegroundServiceNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager.buildForegroundServiceNotification();

        String action = intent != null ? intent.getAction() : "";
        if (ACTION_STOP.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelNotifications();
    }
}