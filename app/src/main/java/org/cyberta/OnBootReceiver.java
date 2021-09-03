package org.cyberta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static org.cyberta.PreferenceHelper.PREF_ON_BOOT;


public class OnBootReceiver extends BroadcastReceiver {

    // Debug: am broadcast -a android.intent.action.BOOT_COMPLETED
    @Override
    public void onReceive(Context context, Intent intent) {
        //Lint complains if we're not checking the intent action
        if (intent == null || !ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        boolean startOnBoot = PreferenceHelper.getBoolean(context, PREF_ON_BOOT, false);
        if (startOnBoot) {
            Intent yggmailIntent = new Intent(context.getApplicationContext(), YggmailService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getApplicationContext().startForegroundService(yggmailIntent);
            } else {
                context.getApplicationContext().startService(yggmailIntent);
            }
        }
    }
}
