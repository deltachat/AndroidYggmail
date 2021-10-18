package org.cyberta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.cyberta.databinding.FragmentSettingsBinding;

import static org.cyberta.PreferenceHelper.PREF_LOOKUP_LOCAL_PEERS;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

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

        binding.switchLocalNetwork.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceHelper.putBoolean(getContext(), PREF_LOOKUP_LOCAL_PEERS, isChecked);
            if (YggmailOberservable.getInstance().getStatus() == YggmailOberservable.Status.Running) {
                YggmailServiceCommand.stopYggmail(getContext());
                YggmailServiceCommand.startYggmail(getContext());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}