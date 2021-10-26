package org.cyberta.settings;

import android.content.Context;

import androidx.annotation.NonNull;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PeerManager extends Observable {
    HashSet<String> selectedPeers = new HashSet<>();
    private HashMap<String, Peer> addressPeerMap = new HashMap<>();
    private HashMap<String, ArrayList<Peer>> peerMap = new HashMap<>();
    private ArrayList<Peer> displayList = new ArrayList<>();
    public static final String PEER_URL = "https://publicpeers.neilalexander.dev/publicnodes.json";
    private WeakReference<Context> contextRef;

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
                    parsePeerList(responseJson);
                    PreferenceHelper.setPublicPeers(context, responseJson);
                    updateDisplayList();
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

    public int getSelectedPeerCount(String countryKey) {
        int counter = 0;
        for (String address : selectedPeers) {
            Peer peer = addressPeerMap.get(address);
            if (peer.countryKey.equals(countryKey)) {
                counter++;
            }
        }
        return counter;
    }

    public void toggleHeader(Peer peer) {
        ArrayList<Peer> peers = peerMap.get(peer.countryKey);
        for (Peer p : peers) {
            p.showItem = !p.showItem;
        }

        updateDisplayList();
        PeerManager.this.setChanged();
        PeerManager.this.notifyObservers();
    }

    public HashMap<String, ArrayList<Peer>> fromJson(String json) throws JSONException {
        HashMap<String, ArrayList<Peer>> peerMap = new HashMap<>();

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
                }
                countryList.add(peer);
                addressPeerMap.put(peer.address, peer);
            }
        }
        removeDeprecatedSelectedPeers();
        return peerMap;
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

    private void removeDeprecatedSelectedPeers() {
        HashSet<String> tmp = new HashSet<>(selectedPeers);
        Collection<ArrayList<Peer>> countryLists = peerMap.values();
        for (ArrayList<Peer> countryList : countryLists) {
            for (Peer peer : countryList) {
                if (selectedPeers.contains(peer.address)) {
                    tmp.add(peer.address);
                }
            }
        }
        selectedPeers = tmp;
    }

    private void parsePeerList(String peersJson) throws JSONException {
        peerMap =  fromJson(peersJson);
    }
}
