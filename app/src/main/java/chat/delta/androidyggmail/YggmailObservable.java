package chat.delta.androidyggmail;

import java.util.HashSet;
import java.util.Observable;

public class YggmailObservable extends Observable {

    private static YggmailObservable instance;
    public enum Status {
        Stopped,
        Running,
        ShuttingDown,
        Error
    }

    public enum WifiStatus {
        Connected,
        Disconnected
    }
    public enum NetworkStatus {
        Connected,
        Disconnected
    }

    public HashSet<String> localPeers;
    public HashSet<String> publicPeers;

    private Status status = Status.Stopped;
    private WifiStatus wifiStatus = WifiStatus.Disconnected;
    private NetworkStatus networkStatus = NetworkStatus.Disconnected;

    private YggmailObservable() {
        localPeers = new HashSet<>();
        publicPeers = new HashSet<>();
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

    public void setNetworkStatus(NetworkStatus networkStatus) {
        if (networkStatus != this.networkStatus) {
            this.networkStatus = networkStatus;
            setChanged();
            notifyObservers();
        }
    }

    public void setWifiStatus(WifiStatus wifiStatus) {
        if (wifiStatus != this.wifiStatus) {
            this.wifiStatus = wifiStatus;
            setChanged();
            notifyObservers();
        }
    }

    public NetworkStatus getNetworkStatus() {
        return networkStatus;
    }

    public WifiStatus getWifiStatus() {
        return wifiStatus;
    }

    public Status getStatus() {
        return status;
    }
}
