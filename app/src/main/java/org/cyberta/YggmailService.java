package org.cyberta;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import yggmail.Logger;
import yggmail.Yggmail_;

import static org.cyberta.PreferenceHelper.PREF_ON_BOOT;

public class YggmailService extends Service {

    public static final String ACTION_STOP = "ACTION_STOP";
    private static final String TAG = YggmailService.class.getSimpleName();

    public YggmailService() {
    }

    YggmailNotificationManager notificationManager ;
    Yggmail_ yggmail;
    FileLogger fileLogger;



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
        fileLogger = new FileLogger(this);

        yggmail = new Yggmail_();
        yggmail.setLogger(new Logger() {
            @Override
            public void logError(long l, String s) {
                YggmailOberservable.getInstance().setStatus(YggmailOberservable.Status.Error);
                Log.e("YGGMAIL error", s);
                fileLogger.send(s);

                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Toast.makeText(getApplicationContext(), "error:" + l + " - " + s, Toast.LENGTH_LONG ).show();
                notificationManager.buildErrorNotification(s);
                stopSelf();
            }

            @Override
            public void logMessage(String s) {
                Log.d(TAG, s);
                //TODO: show in UI
                if (s != null) {
                    fileLogger.send(s);
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = notificationManager.buildForegroundServiceNotification();
        startForeground(YggmailNotificationManager.YGGMAIL_NOTIFICATION_ID, notification);

        String action = intent != null ? intent.getAction() : "";
        if (ACTION_STOP.equals(action)) {
            Log.d(TAG, "onStartCommand ACTION_STOP");

            if (yggmail != null) {
                YggmailOberservable.getInstance().setStatus(YggmailOberservable.Status.ShuttingDown);
                yggmail.stop();
                YggmailOberservable.getInstance().setStatus(YggmailOberservable.Status.Stopped);
            }

            notificationManager.cancelNotifications();
            PreferenceHelper.putBoolean(getApplicationContext(), PREF_ON_BOOT, false);
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.d(TAG, "onStartCommand start yggmail");

        YggmailOberservable.getInstance().setStatus(YggmailOberservable.Status.Running);
        Log.d(TAG, getApplicationContext().getFilesDir().getPath()+"/yggmail.db");
        yggmail.setDatabaseName(getApplicationContext().getFilesDir().getPath()+"/yggmail.db");

        yggmail.createPassword("delta");
        yggmail.start("localhost:1025",
                "localhost:1143",
                false,
                "tcp://45.138.172.192:5001,tcp://94.130.203.208:5999,tcp://bunkertreff.ddns.net:5454,tcp://ygg.mkg20001.io:80,tcp://yugudorashiru.de:80");
        PreferenceHelper.putBoolean(getApplicationContext(), PREF_ON_BOOT, true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Toast.makeText(getApplicationContext(), "account name " + yggmail.getAccountName() + "@yggmail copied to clipboard", Toast.LENGTH_LONG ).show();
            Util.writeTextToClipboard(getApplicationContext(), yggmail.getAccountName() + "@yggmail");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fileLogger.quit();
    }
}