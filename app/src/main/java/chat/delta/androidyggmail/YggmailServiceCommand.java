package chat.delta.androidyggmail;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import static chat.delta.androidyggmail.YggmailService.ACTION_SEND_ACCOUNT_DATA;
import static chat.delta.androidyggmail.YggmailService.ACTION_STOP;
import static chat.delta.androidyggmail.YggmailService.ACTION_CLEARLOG;

public class YggmailServiceCommand {

    public static void sendYggmailAccountData(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context.getApplicationContext(), YggmailService.class);
        intent.setAction(ACTION_SEND_ACCOUNT_DATA);
        startServiceIntent(context, intent);
    }

    public static void startYggmail(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context.getApplicationContext(), YggmailService.class);
        startServiceIntent(context, intent);
    }

    public static void stopYggmail(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context.getApplicationContext(), YggmailService.class);
        intent.setAction(ACTION_STOP);
        startServiceIntent(context, intent);
    }

    public static void clearLog(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context.getApplicationContext(), YggmailService.class);
        intent.setAction(ACTION_CLEARLOG);
        startServiceIntent(context, intent);
    }

    private static void startServiceIntent(Context context, Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }
}
