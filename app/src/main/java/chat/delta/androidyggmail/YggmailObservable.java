package chat.delta.androidyggmail;

import java.util.Observable;

public class YggmailObservable extends Observable {

    private static YggmailObservable instance;
    public enum Status {
        Stopped,
        Running,
        ShuttingDown,
        Error
    }
    private Status status = Status.Stopped;

    private YggmailObservable() {
    }

    public static YggmailObservable getInstance() {
        if (instance == null) {
            instance = new YggmailObservable();
        }
        return instance;
    }

    public void setStatus(Status status) {
        if (status != this.status) {
            this.status = status;
            setChanged();
            notifyObservers();
        }
    }

    public Status getStatus() {
        return status;
    }
}
