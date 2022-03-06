package chat.delta.androidyggmail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import chat.delta.androidyggmail.logging.LogObservable;

public class NetworkStateManager {
    private final static String TAG = "[ Connectivity ]";
    private final ConnectivityManager connectivityManager;
    private final NetworkStateCallback networkStateCallback;
    private final WifiStateCallback wifiStateCallback;

    public NetworkStateManager(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkStateCallback = new NetworkStateCallback();
        connectivityManager.registerNetworkCallback(registerInternetConnectivity(), networkStateCallback);
        wifiStateCallback = new WifiStateCallback();
        connectivityManager.registerNetworkCallback(registerWifiNetwork(), wifiStateCallback);
    }

    public void unregister() {
        connectivityManager.unregisterNetworkCallback(wifiStateCallback);
        connectivityManager.unregisterNetworkCallback(networkStateCallback);
    }

    public NetworkRequest registerWifiNetwork() {
        return new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
    }

    public NetworkRequest registerInternetConnectivity() {
        return new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
    }

    public static class NetworkStateCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            LogObservable.getInstance().addLog(TAG, "Internet connection available");
            YggmailObservable.getInstance().setNetworkStatus(YggmailObservable.NetworkStatus.Connected);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            LogObservable.getInstance().addLog(TAG, "Internet connection lost");
            YggmailObservable.getInstance().setNetworkStatus(YggmailObservable.NetworkStatus.Disconnected);
        }
    }

    public static class WifiStateCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            LogObservable.getInstance().addLog(TAG, "Wifi connected");
            YggmailObservable.getInstance().setWifiStatus(YggmailObservable.WifiStatus.Connected);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            LogObservable.getInstance().addLog(TAG, "Wifi disconnected");
            YggmailObservable.getInstance().setWifiStatus(YggmailObservable.WifiStatus.Disconnected);
        }
    }
}
