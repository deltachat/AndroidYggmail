package org.cyberta.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.cyberta.MainActivity;
import org.cyberta.R;
import org.cyberta.databinding.FragmentPeerSelectionBinding;

import static android.content.Intent.CATEGORY_DEFAULT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class PeerSelectionFragment extends Fragment {

    private FragmentPeerSelectionBinding binding;
    private PeerRecyclerViewAdapter adapter;
    private PeerBroadcastReceiver broadcastReceiver;

    public PeerSelectionFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastReceiver = new PeerBroadcastReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeerSelectionBinding.inflate(inflater, container, false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_STARTED);
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_FINISHED);
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_FAILED);
        intentFilter.addCategory(CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);

        // Set the adapter
        Context context = binding.peerList.getContext();
        adapter = new PeerRecyclerViewAdapter(context);
        RecyclerView recyclerView = binding.peerList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        try {
            ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
            int selectedPeers = adapter.getPeerManager().selectedPeers.size();
            if (selectedPeers > 0) {
                actionBar.setTitle(getString(R.string.select_n_peers, selectedPeers));
            } else {
                actionBar.setTitle(getString(R.string.select_peers));
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return binding.getRoot();
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.getPeerManager().saveSelection();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

   private class PeerBroadcastReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
           switch (intent.getAction()) {
               case PeerManager.EVENT_LOADING_PEERS_STARTED:
                   if (adapter.getItemCount() == 0) {
                       binding.loadingContainer.setVisibility(VISIBLE);
                       binding.loadingProgress.setVisibility(VISIBLE);
                       binding.loadingDescription.setText(R.string.loading_peers);
                   }
                   break;
               case PeerManager.EVENT_LOADING_PEERS_FAILED:
                   if (adapter.getItemCount() == 0) {
                       binding.loadingContainer.setVisibility(VISIBLE);
                       binding.loadingProgress.setVisibility(INVISIBLE);
                       binding.loadingDescription.setText(R.string.loading_failed);
                   }
                   break;
               case PeerManager.EVENT_LOADING_PEERS_FINISHED:
                   binding.loadingContainer.setVisibility(GONE);
                   break;
               default:
                   break;
           }
       }
   }
}
