package org.cyberta;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Observable;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.cyberta.PreferenceHelper.PREF_PUBLIC_PEERS;
import static org.cyberta.PreferenceHelper.PREF_SELECTED_PEERS;

public class PeerManager extends Observable {
    HashSet<String> selectedPeers = new HashSet<>();
    ArrayList<Peer> peerList = new ArrayList<>();
    public static final String PEER_URL = "https://publicpeers.neilalexander.dev/publicnodes.json";
    private WeakReference<Context> contextRef;

    public PeerManager(Context context) {
        contextRef = new WeakReference<>(context);
        String peersJson = PreferenceHelper.getPublicPeers(context);
        try {
            selectedPeers = PreferenceHelper.getSelectedPeers(context);
            peerList = fromJson(peersJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchPeers(Context context) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(PEER_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseJson = response.body().string();
                    peerList = fromJson(responseJson);
                    PreferenceHelper.setPublicPeers(context, responseJson);
                    PeerManager.this.setChanged();
                    PeerManager.this.notifyObservers();
                } catch (NullPointerException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public ArrayList<Peer> fromJson(String json) throws JSONException {
        ArrayList<Peer> peerList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> countryKeys = jsonObject.keys();
        boolean showSectionHeader;
        while (countryKeys.hasNext()) {
            String countryKey = countryKeys.next();
            showSectionHeader = true;
            JSONObject countryObject = jsonObject.getJSONObject(countryKey);
            Iterator<String> addressKeys = countryObject.keys();
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
                if (selectedPeers.contains(peer.address)) {
                    peer.isSelected = true;
                }
                peerList.add(peer);
            }
        }
        removeDeprecatedSelectedPeers();
        return peerList;
    }

    private void removeDeprecatedSelectedPeers() {
        HashSet<String> tmp = new HashSet<>(selectedPeers);
        for (Peer peer : peerList) {
            if (selectedPeers.contains(peer.address)) {
                tmp.add(peer.address);
            }
        }
        selectedPeers = tmp;
    }

    public void saveSelection() {
        PreferenceHelper.setSelectedPeers(contextRef.get(), selectedPeers);
    }
}
