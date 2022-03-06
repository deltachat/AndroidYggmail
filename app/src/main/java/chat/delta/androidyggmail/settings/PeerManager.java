package chat.delta.androidyggmail.settings;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;

import chat.delta.androidyggmail.Peer;
import chat.delta.androidyggmail.YggmailOberservable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PeerManager extends Observable {
    public static final String PEER_URL = "https://publicpeers.neilalexander.dev/publicnodes.json";
    public static final String EVENT_LOADING_PEERS_STARTED = "EVENT_LOADING_PEERS_STARTED";
    public static final String EVENT_LOADING_PEERS_FINISHED = "EVENT_LOADING_PEERS_FINISHED";
    public static final String EVENT_LOADING_PEERS_SELECTED_PEERS_CHANGED = "EVENT_LOADING_PEERS_SELECTED_PEERS_CHANGED";
    public static final String EXTRA_SELECTED_PEERS = "EXTRA_SELECTED_PEERS";
    public static final String EVENT_LOADING_PEERS_FAILED = "EVENT_LOADING_PEERS_FAILED";
    private static final String TAG = PeerManager.class.getSimpleName();

    HashSet<String> selectedPeers = new HashSet<>();
    private HashMap<String, Peer> addressPeerMap = new HashMap<>();
    private HashMap<String, ArrayList<Peer>> peerMap = new HashMap<>();
    private ArrayList<Peer> displayList = new ArrayList<>();
    private final WeakReference<Context> contextRef;

    public PeerManager(Context context) {
        contextRef = new WeakReference<>(context);
        String peersJson = PreferenceHelper.getPublicPeers(context);
        try {
            selectedPeers = PreferenceHelper.getSelectedPeers(context);
            parsePeerList(peersJson);
            updateDisplayList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchPeers(Context context) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(getEvent(EVENT_LOADING_PEERS_STARTED));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(PEER_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                localBroadcastManager.sendBroadcast(getEvent(EVENT_LOADING_PEERS_FAILED));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseJson = response.body().string();
                    boolean hasSelectedPeerSizeChanged = parsePeerList(responseJson);
                    PreferenceHelper.setPublicPeers(context, responseJson);
                    updateDisplayList();
                    if (hasSelectedPeerSizeChanged) {
                        Intent intent = getEvent(EVENT_LOADING_PEERS_SELECTED_PEERS_CHANGED);
                        intent.putExtra(EXTRA_SELECTED_PEERS, selectedPeers.size());
                        localBroadcastManager.sendBroadcast(intent);
                    }
                    localBroadcastManager.sendBroadcast(getEvent(EVENT_LOADING_PEERS_FINISHED));
                    PeerManager.this.setChanged();
                    PeerManager.this.notifyObservers();
                } catch (NullPointerException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Intent getEvent(String event) {
        Intent intent = new Intent(event);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public void toggleSelected(Peer peer) {
        if (selectedPeers.contains(peer.address)) {
            selectedPeers.remove(peer.address);
        } else {
            selectedPeers.add(peer.address);
        }
        peer.isSelected = !peer.isSelected;

        PeerManager.this.setChanged();
        PeerManager.this.notifyObservers();
    }

    public int getSelectedPeerCount(String countryKey) {
        int counter = 0;
        for (String address : selectedPeers) {
            Peer peer = addressPeerMap.get(address);
            if (peer != null && peer.countryKey.equals(countryKey)) {
                counter++;
            }
        }
        return counter;
    }

    public void toggleHeader(Peer peer) {
        ArrayList<Peer> peers = peerMap.get(peer.countryKey);
        if (peers == null) {
            return;
        }
        for (Peer p : peers) {
            p.showItem = !p.showItem;
        }

        updateDisplayList();
        PeerManager.this.setChanged();
        PeerManager.this.notifyObservers();
    }

    public Pair<HashMap<String, ArrayList<Peer>>, Boolean> fromJson(String json) throws JSONException {
        HashMap<String, ArrayList<Peer>> peerMap = new HashMap<>();
        HashSet<String> tmpSelectedPeers = new HashSet<>();

        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> countryKeys = jsonObject.keys();
        boolean showSectionHeader;
        while (countryKeys.hasNext()) {
            String countryKey = countryKeys.next();
            showSectionHeader = true;
            JSONObject countryObject = jsonObject.getJSONObject(countryKey);
            Iterator<String> addressKeys = countryObject.keys();
            ArrayList<Peer> countryList = new ArrayList<>();
            peerMap.put(countryKey,countryList);

            while (addressKeys.hasNext()) {
                String addressKey = addressKeys.next();
                JSONObject peerObject = countryObject.getJSONObject(addressKey);
                Peer peer = Peer.fromJson(countryKey, addressKey, peerObject);
                if (!peer.up) {
                    continue;
                }

                if (showSectionHeader) {
                    peer.showSectionHeader = true;
                    showSectionHeader = false;
                }
                Peer previousPeerInstance = addressPeerMap.get(peer.address);
                if (previousPeerInstance != null) {
                    peer.showItem = previousPeerInstance.showItem;
                }
                if (selectedPeers.contains(peer.address)) {
                    peer.isSelected = true;
                    tmpSelectedPeers.add(peer.address);
                }
                countryList.add(peer);
                addressPeerMap.put(peer.address, peer);
            }
        }
        boolean selectedPeerSizeChanged = selectedPeers.size() != tmpSelectedPeers.size();
        selectedPeers = tmpSelectedPeers;
        return new Pair<>(peerMap, selectedPeerSizeChanged);
    }

    public ArrayList<Peer> getDisplayList() {
        return displayList;
    }

    private void updateDisplayList() {
        ArrayList<Peer> displayList = new ArrayList<>();
        for (String countryKey : peerMap.keySet()) {
            ArrayList<Peer> countryList = peerMap.get(countryKey);
            if (countryList == null || countryList.size() == 0) {
                continue;
            }
            Peer firstCountryListItem = countryList.get(0);
            if (firstCountryListItem.showItem) {
                displayList.addAll(countryList);
            } else {
                displayList.add(firstCountryListItem);
            }
        }
        this.displayList = displayList;
    }

    public void saveSelection() {
        PreferenceHelper.setSelectedPeers(contextRef.get(), selectedPeers);
    }

    private boolean parsePeerList(String peersJson) throws JSONException {
        Pair<HashMap<String, ArrayList<Peer>>, Boolean> result = fromJson(peersJson);
        peerMap =  result.first;
        return result.second;
    }

    public @Nullable Context getContext() {
        return contextRef.get();
    }
}
