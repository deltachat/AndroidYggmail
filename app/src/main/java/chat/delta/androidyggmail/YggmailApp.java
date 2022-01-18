package chat.delta.androidyggmail;

import android.app.Application;

public class YggmailApp extends Application {

    private YggmailOberservable yggmailOberservable;

    @Override
    public void onCreate() {
        super.onCreate();
        this.yggmailOberservable = YggmailOberservable.getInstance();
    }
}
