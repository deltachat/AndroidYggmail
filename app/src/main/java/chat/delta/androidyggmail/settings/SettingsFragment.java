package chat.delta.androidyggmail.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getConnectToPublicPeers;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getMulticast;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getStartOnBoot;
import static chat.delta.androidyggmail.settings.PreferenceHelper.setConnectToPublicPeers;
import static chat.delta.androidyggmail.settings.PreferenceHelper.setMulticast;
import static chat.delta.androidyggmail.settings.PreferenceHelper.setStartOnBoot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import chat.delta.androidyggmail.R;
import chat.delta.androidyggmail.databinding.FragmentSettingsBinding;

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
        binding.switchEnableOnboot.setOnCheckedChangeListener((buttonView, isChecked) ->
                setStartOnBoot(getContext(), isChecked));
        binding.switchLocalNetwork.setChecked(getMulticast(getContext()));
        binding.switchLocalNetwork.setOnCheckedChangeListener((buttonView, isChecked) ->
                setMulticast(getContext(), isChecked));
        binding.switchPublicPeers.setChecked(getConnectToPublicPeers(getContext()));
        binding.switchPublicPeers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setConnectToPublicPeers(getContext(), isChecked);
            binding.selectPeers.setEnabled(isChecked);
        });
        binding.selectPeers.setText(getString(R.string.select_n_peers, peerManager.selectedPeers.size()));
        binding.selectPeers.setOnClickListener(v ->
                NavHostFragment.findNavController(SettingsFragment.this)
                .navigate(R.id.action_SettingsFragment_to_PeerSelectionFragment));
        binding.selectPeers.setEnabled(getConnectToPublicPeers(getContext()));

        binding.switchCustomClient.setChecked(PreferenceHelper.useCustomMailClient(getContext()));
        binding.switchCustomClient.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceHelper.setUseCustomMailClient(getContext(), isChecked);
            binding.serverDetailContainer.setVisibility(isChecked ? VISIBLE : GONE);
        });

        binding.serverDetailContainer.setVisibility(binding.switchCustomClient.isChecked() ? VISIBLE : GONE);
        String accountName = PreferenceHelper.getAccountName(getContext());
        if (!accountName.isEmpty()) {
            accountName += "@yggmail";
            binding.username.setShowCopy(true);
        } else {
            accountName = getContext().getString(R.string.start_to_create_user);
            binding.username.setShowCopy(false);
        }
        binding.username.setValue(accountName);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}