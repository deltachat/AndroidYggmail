package org.cyberta;

import java.util.Observable;

public class YggmailOberservable extends Observable {

    private static YggmailOberservable instance;
    public enum Status {
        Stopped,
        Running,
        ShuttingDown,
        Error
    }
    private Status status = Status.Stopped;

    private YggmailOberservable() {}
    public static YggmailOberservable getInstance() {
        if (instance == null) {
            instance = new YggmailOberservable();
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
