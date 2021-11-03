package org.cyberta;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.cyberta.logging.FileLogger;
import org.cyberta.settings.PreferenceHelper;

import java.io.Closeable;

import yggmail.Logger;
import yggmail.Yggmail_;

public class YggmailService extends Service {

    public static final String ACTION_STOP = "ACTION_STOP";
    private static final String TAG = YggmailService.class.getSimpleName();

    private YggmailNotificationManager notificationManager ;
    private Yggmail_ yggmail;
    private FileLogger fileLogger;
    private IPCServiceConnection ipcServiceConnection;

    public YggmailService() {
    }

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
                PreferenceHelper.getMulticast(getApplicationContext()),
                PreferenceHelper.getSelectedPublicPeers(getApplicationContext()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "account name " + yggmail.getAccountName() + "@yggmail copied to clipboard", Toast.LENGTH_LONG ).show();
        Util.writeTextToClipboard(getApplicationContext(), yggmail.getAccountName() + "@yggmail");

        boolean isInitial = PreferenceHelper.getAccountName(getApplicationContext()).isEmpty() || true;
        if (ipcServiceConnection == null && isInitial) {
            ipcServiceConnection = new IPCServiceConnection(this);
            ipcServiceConnection.initAndSendAccountData(yggmail.getAccountName()+"@yggmail");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fileLogger.quit();

        if (ipcServiceConnection != null) {
            ipcServiceConnection.close();
        }
    }

    private static class IPCServiceConnection implements Closeable {
        private final static int ADD_ACCOUNT = 1;
        private static final String CONFIG_ADDRESS = "addr";
        private static final String CONFIG_MAIL_PASSWORD = "mail_pw";
        private Context context;
        private ServiceConnection serviceConnection;

        public IPCServiceConnection(Context context) {
            this.context = context;
        }

        public void initAndSendAccountData(String emailAddress) {
            if (this.serviceConnection != null) {
                return;
            }
            this.serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        Messenger serviceMessenger = new Messenger(service);
                        Message message = new Message();
                        message.what = ADD_ACCOUNT;
                        Bundle bundle = new Bundle();
                        bundle.putString(CONFIG_ADDRESS, emailAddress);
                        bundle.putString(CONFIG_MAIL_PASSWORD, "delta");
                        message.setData(bundle);
                        serviceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent = new Intent();
            // application Ids: chat.delta, com.b44t.messenger, chat.delta.beta, com.b44t.messenger.beta
            intent.setComponent(new ComponentName("com.b44t.messenger.beta","org.thoughtcrime.securesms.service.IPCAddAccountsService"));
            intent.setAction("chat.delta.addaccount");
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void close() {
            Log.d(TAG, "close IPCServiceConnection");
            if (serviceConnection != null) {
                Log.d(TAG, "close IPCServiceConnection -> unbind service");
                context.unbindService(serviceConnection);
            }
            serviceConnection = null;
            context = null;
        }
    }
}