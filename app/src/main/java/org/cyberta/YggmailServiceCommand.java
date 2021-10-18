package org.cyberta;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import static org.cyberta.YggmailService.ACTION_STOP;

public class YggmailServiceCommand {

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
