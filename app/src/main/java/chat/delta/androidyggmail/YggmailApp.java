package chat.delta.androidyggmail;

import android.app.Application;

public class YggmailApp extends Application {

    private YggmailObservable yggmailObservable;

    @Override
    public void onCreate() {
        super.onCreate();
        this.yggmailObservable = YggmailObservable.getInstance();
    }
}
