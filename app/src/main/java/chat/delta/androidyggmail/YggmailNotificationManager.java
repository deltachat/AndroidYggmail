package chat.delta.androidyggmail;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static chat.delta.androidyggmail.YggmailService.ACTION_STOP;


public class YggmailNotificationManager {

    private final Context context;
    public final static int YGGMAIL_NOTIFICATION_ID = 1;
    private final static String NOTIFICATION_CHANNEL_NEWSTATUS_ID = "YGGMAIL_NOTIFICATION_CHANNEL_NEWSTATUS_ID";

    public YggmailNotificationManager(@NonNull Context context) {
        this.context = context;
    }

    public Notification buildForegroundServiceNotification() {
        NotificationManager notificationManager = initNotificationManager();
        if (notificationManager == null) {
            return null;
        }
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,
                "STOP", getStopIntent());
        NotificationCompat.Builder notificationBuilder = initNotificationBuilderDefaults();
        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification_light)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.notification_running))
                .setContentIntent(getContentPendingIntent())
                .addAction(actionBuilder.build());

        return notificationBuilder.build();
        //notificationManager.notify(YGGMAIL_NOTIFICATION_ID, notificationBuilder.build());
    }

    public void buildErrorNotification(String error) {
        NotificationManager notificationManager = initNotificationManager();
        if (notificationManager == null) {
            return;
        }
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.restart), getStartIntent());
        NotificationCompat.Builder notificationBuilder = initNotificationBuilderDefaults();
        notificationBuilder
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.notification_error))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(error))
                .setContentIntent(getContentPendingIntent())
                .addAction(actionBuilder.build());

        notificationManager.notify(YGGMAIL_NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent getContentPendingIntent() {
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
    private PendingIntent getStopIntent() {
        Intent stopYggmailIntent = new Intent (context, YggmailService.class);
        stopYggmailIntent.setAction(ACTION_STOP);
        return PendingIntent.getService(context, 0, stopYggmailIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getStartIntent() {
        Intent startYggmailIntent = new Intent (context, YggmailService.class);
        return PendingIntent.getService(context, 0, startYggmailIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationManager initNotificationManager() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }
        return notificationManager;
    }

    @TargetApi(26)
    private void createNotificationChannel(NotificationManager notificationManager) {
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name,
                NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(channel);
    }

    private NotificationCompat.Builder initNotificationBuilderDefaults() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context, NOTIFICATION_CHANNEL_NEWSTATUS_ID);
        notificationBuilder.
                setDefaults(Notification.DEFAULT_ALL).
                setAutoCancel(true);
        return notificationBuilder;
    }



    public void cancelNotifications() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancel(YGGMAIL_NOTIFICATION_ID);
    }

}
