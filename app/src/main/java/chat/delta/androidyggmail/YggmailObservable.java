package chat.delta.androidyggmail;

import java.util.HashSet;
import java.util.Observable;

public class YggmailObservable extends Observable {

    private static final String TAG = YggmailObservable.class.getSimpleName();
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

    private String wifiSsid;

    private final HashSet<PeerConnection> localPeers;
    private final HashSet<PeerConnection> publicPeers;

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

    public void addLocalPeerConnection(PeerConnection peerConnection) {
        if (localPeers.add(peerConnection)) {
            setChanged();
            notifyObservers();
        }
    }

    private void clearPeerConnections(){
        publicPeers.clear();
        localPeers.clear();
    }

    public void removeLocalPeerConnection(PeerConnection peerConnection) {
        if (localPeers.remove(peerConnection)) {
            setChanged();
            notifyObservers();
        }
    }

    public void removePublicPeerConnection(PeerConnection peerConnection) {
        if (publicPeers.remove(peerConnection)) {
            setChanged();
            notifyObservers();
        }
    }

    public int getLocalPeerConnectionCount() {
        return localPeers.size();
    }

    public int getPublicPeerConnectionCount() {
        return publicPeers.size();
    }

    public void addPublicPeerConnection(PeerConnection peerConnection) {
        if (publicPeers.add(peerConnection)) {
            setChanged();
            notifyObservers();
        }
    }

    public void setStatus(Status status) {
        if (status != this.status) {
            this.status = status;
            if (status == Status.Stopped) {
                clearPeerConnections();
            }
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

    public void setWifiStatus(WifiStatus wifiStatus, String wifiSsid) {
        if (wifiStatus != this.wifiStatus) {
            this.wifiStatus = wifiStatus;
            this.wifiSsid = wifiSsid;
            setChanged();
            notifyObservers();
        }
    }

    public String getWifiSsid() {
        return wifiSsid;
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
