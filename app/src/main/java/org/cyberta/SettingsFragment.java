package org.cyberta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.cyberta.databinding.FragmentSettingsBinding;

import static org.cyberta.PreferenceHelper.getConnectToPublicPeers;
import static org.cyberta.PreferenceHelper.getMulticast;
import static org.cyberta.PreferenceHelper.getStartOnBoot;
import static org.cyberta.PreferenceHelper.setConnectToPublicPeers;
import static org.cyberta.PreferenceHelper.setMulticast;
import static org.cyberta.PreferenceHelper.setStartOnBoot;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PeerManager peerManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        peerManager = new PeerManager(getContext());

        binding.switchEnableOnboot.setChecked(getStartOnBoot(getContext()));
        binding.switchEnableOnboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setStartOnBoot(getContext(), isChecked);
        });
        binding.switchLocalNetwork.setChecked(getMulticast(getContext()));
        binding.switchLocalNetwork.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setMulticast(getContext(), isChecked);
        });
        binding.switchPublicPeers.setChecked(getConnectToPublicPeers(getContext()));
        binding.switchPublicPeers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setConnectToPublicPeers(getContext(), isChecked);
            binding.selectPeers.setEnabled(isChecked);
        });
        binding.selectPeers.setText(getString(R.string.select_n_peers, peerManager.selectedPeers.size()));
        binding.selectPeers.setOnClickListener(v -> {
            NavHostFragment.findNavController(SettingsFragment.this)
                    .navigate(R.id.action_SettingsFragment_to_PeerSelectionFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}