package org.cyberta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DcBroadcastReceiver extends BroadcastReceiver {
    public static final String DC_REQUEST_ACCOUNT_DATA = "chat.delta.DC_REQUEST_ACCOUNT_DATA";
    private static final String TAG = DcBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DC_REQUEST_ACCOUNT_DATA.equals(intent.getAction())) {
            Log.d(TAG, "DC_REQUEST_ACCOUNT_DATA request received");
            YggmailServiceCommand.sendYggmailAccountData(context.getApplicationContext());
        }
    }
}
