package org.cyberta;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.cyberta.databinding.FragmentPeerSelectionBinding;


public class PeerSelectionFragment extends Fragment {

    private FragmentPeerSelectionBinding binding;
    private PeerRecyclerViewAdapter adapter;

    public PeerSelectionFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeerSelectionBinding.inflate(inflater, container, false);

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
    }
}
