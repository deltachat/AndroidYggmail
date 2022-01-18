package chat.delta.androidyggmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import chat.delta.androidyggmail.settings.PreferenceHelper;


public class OnBootReceiver extends BroadcastReceiver {

    // Debug: am broadcast -a android.intent.action.BOOT_COMPLETED
    @Override
    public void onReceive(Context context, Intent intent) {
        //Lint complains if we're not checking the intent action
        if (intent == null || !ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        boolean startOnBoot = PreferenceHelper.getStartOnBoot(context);
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
