package org.cyberta;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class YggmailService extends Service {
    public YggmailService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}