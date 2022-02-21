package chat.delta.androidyggmail;

import static chat.delta.androidyggmail.InstallHelper.DC_IPC_ACTION_ADD_ACCOUNT;
import static chat.delta.androidyggmail.InstallHelper.DC_IPC_SERVICE;
import static chat.delta.androidyggmail.InstallHelper.getInstalledDeltaChatPackageName;
import static chat.delta.androidyggmail.InstallHelper.isDeltaChatInstalled;

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

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import chat.delta.androidyggmail.logging.FileLogger;

import java.io.Closeable;

import chat.delta.androidyggmail.settings.PreferenceHelper;
import yggmail.Logger;
import yggmail.Yggmail;
import yggmail.Yggmail_;

public class YggmailService extends Service {
    private static final String TAG = YggmailService.class.getSimpleName();

    // Actions sent to the the service
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SEND_ACCOUNT_DATA = "ACTION_SEND_ACCOUNT_DATA";
    public static final String ACTION_CLEARLOG = "ACTION_CLEARLOG";
    // Actions broadcasted from the service
    public static final String SERVICE_ACTION_INSTALL_DC = "SERVICE_ACTION_INSTALL_DC";

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
        } else if (ACTION_CLEARLOG.equals(action)) {
            Log.d(TAG, "onStartCommand ACTION_CLEARLOG");
            fileLogger.reset();
            return START_NOT_STICKY;
        }

        boolean isInitial = PreferenceHelper.getAccountName(getApplicationContext()).isEmpty();
        if (Yggmail.Stopped == yggmail.getState()) {

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
            if (PreferenceHelper.useCustomMailClient(this)) {
                Util.writeTextToClipboard(getApplicationContext(), yggmail.getAccountName() + "@yggmail");
            }
            PreferenceHelper.setAccountName(this, yggmail.getAccountName());
        }

        if (ACTION_SEND_ACCOUNT_DATA.equals(action)) {
            ipcServiceConnection = new IPCServiceConnection(this);
            ipcServiceConnection.initAndSendAccountData(yggmail.getAccountName()+"@yggmail");
            return START_NOT_STICKY;
        }

        if (!isDeltaChatInstalled(getPackageManager()) && !PreferenceHelper.useCustomMailClient(this)) {
            broadcast(SERVICE_ACTION_INSTALL_DC);
            return START_NOT_STICKY;
        }

        if (isInitial) {
            ipcServiceConnection = new IPCServiceConnection(this);
            ipcServiceConnection.initAndSendAccountData(yggmail.getAccountName()+"@yggmail");
        }

        return START_NOT_STICKY;
    }

    private void broadcast(String action) {
        Intent broadcastIntent = new Intent(action);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
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
                context.unbindService(serviceConnection);
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
            intent.setComponent(new ComponentName(getInstalledDeltaChatPackageName(context), DC_IPC_SERVICE));
            intent.setAction(DC_IPC_ACTION_ADD_ACCOUNT);
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void close() {
            if (serviceConnection != null) {
                Log.d(TAG, "close IPCServiceConnection -> unbind service");
                context.unbindService(serviceConnection);
            }
            serviceConnection = null;
            context = null;
        }
    }
}
