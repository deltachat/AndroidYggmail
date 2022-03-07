package chat.delta.androidyggmail;

import static android.content.Intent.CATEGORY_DEFAULT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static chat.delta.androidyggmail.settings.PeerManager.EXTRA_SELECTED_PEERS;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getAccountName;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getConnectToPublicPeers;
import static chat.delta.androidyggmail.settings.PreferenceHelper.getMulticast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Observable;
import java.util.Observer;

import chat.delta.androidyggmail.YggmailObservable.NetworkStatus;
import chat.delta.androidyggmail.YggmailObservable.WifiStatus;
import chat.delta.androidyggmail.databinding.FragmentMainBinding;
import chat.delta.androidyggmail.settings.PeerManager;
import chat.delta.androidyggmail.settings.PreferenceHelper;

public class MainFragment extends Fragment implements Observer {

    private static final String TAG = MainFragment.class.getSimpleName();
    private FragmentMainBinding binding;
    private PeerManager peerManager;
    private PeerBroadcastReceiver broadcastReceiver;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        YggmailObservable.getInstance().addObserver(this);
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        peerManager = new PeerManager(view.getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_STARTED);
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_FINISHED);
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_FAILED);
        intentFilter.addAction(PeerManager.EVENT_LOADING_PEERS_SELECTED_PEERS_CHANGED);
        intentFilter.addCategory(CATEGORY_DEFAULT);
        broadcastReceiver = new PeerBroadcastReceiver();
        LocalBroadcastManager.getInstance(view.getContext()).registerReceiver(broadcastReceiver, intentFilter);

        updateUI();

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (YggmailObservable.getInstance().getStatus()) {
                    case Running:
                        YggmailServiceCommand.stopYggmail(MainFragment.this.getContext());
                        break;
                    case Stopped:
                        binding.buttonStart.setEnabled(false);
                        binding.buttonStart.setText(R.string.stop);
                        YggmailServiceCommand.startYggmail(MainFragment.this.getContext());
                        break;
                    default:
                        break;
                }
            }
        });
        if (PreferenceHelper.shouldUpdate(getContext())) {
            peerManager.fetchPeers(getContext());
        }
    }

    private void updateUI() {
        switch (YggmailObservable.getInstance().getStatus()) {
            case Error:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.restart);
                binding.title.setText(R.string.state_error);
                binding.tvLocalPeerCounter.setText(R.string.invalid);
                binding.tvPublicPeerCounter.setText(R.string.invalid);
                break;
            case Stopped:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.start);
                binding.title.setText(getAccountName(getContext()).isEmpty() ?
                        R.string.state_initial : R.string.state_off);
                binding.connectionDetailsContainer.setVisibility(INVISIBLE);
                break;
            case ShuttingDown:
                binding.buttonStart.setEnabled(false);
                binding.connectionDetailsContainer.setVisibility(INVISIBLE);
                break;
            case Running:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.stop);
                binding.title.setText(R.string.state_running);
                binding.connectionDetailsContainer.setVisibility(VISIBLE);
                binding.tvPublicPeerCounter.setText(String.valueOf(YggmailObservable.getInstance().getPublicPeerConnectionCount()));
                binding.tvLocalPeerCounter.setText(String.valueOf(YggmailObservable.getInstance().getLocalPeerConnectionCount()));
                binding.imageWifi.setImageResource(YggmailObservable.getInstance().getWifiStatus() == WifiStatus.Connected ? R.drawable.ic_wifi : R.drawable.ic_wifi_off);
                boolean hasInternetConnectivity = YggmailObservable.getInstance().getNetworkStatus() == NetworkStatus.Connected;
                binding.tvConnectivitySubtitle.setText(hasInternetConnectivity ? null : getString(R.string.no_internet));
                binding.imageConnectivity.setImageResource(hasInternetConnectivity ? R.drawable.ic_web : R.drawable.ic_web_off);
                String wifiSSID = YggmailObservable.getInstance().getWifiSsid();
                binding.wifiSsid.setText(wifiSSID == null ? getString(R.string.off) : wifiSSID);
                binding.localPeersContainer.setVisibility(PreferenceHelper.getMulticast(getContext()) ? VISIBLE : GONE);
                binding.publicPeersContainer.setVisibility(PreferenceHelper.getConnectToPublicPeers(getContext()) ? VISIBLE : GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Context context = getContext();

        if (context == null) {
            return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logs) {
            NavHostFragment.findNavController(MainFragment.this)
                    .navigate(R.id.action_MainFragment_to_LogFragment);
            return true;
        } else if (id == R.id.action_settings) {
            NavHostFragment.findNavController(MainFragment.this)
                    .navigate(R.id.action_MainFragment_to_SettingsFragment);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        YggmailObservable.getInstance().deleteObserver(this);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof YggmailObservable) {
            Util.runOnMain(this::updateUI);
        }
    }

    private class PeerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            View root = binding.snackbarCoordinator;
            if (root == null) { return; }
            switch (intent.getAction()) {
                case PeerManager.EVENT_LOADING_PEERS_STARTED:
                    Snackbar bar = Snackbar.make(root, R.string.loading_peers, Snackbar.LENGTH_INDEFINITE);
                    ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(R.id.snackbar_text).getParent();
                    ProgressBar item = new ProgressBar(context);
                    item.setIndeterminate(true);
                    contentLay.addView(item);
                    bar.show();
                    break;
                case PeerManager.EVENT_LOADING_PEERS_FAILED:
                    Snackbar failedBar = Snackbar.make(root, R.string.loading_failed, Snackbar.LENGTH_LONG);
                    failedBar.show();
                    break;
                case PeerManager.EVENT_LOADING_PEERS_SELECTED_PEERS_CHANGED:
                    int selectedPeers = intent.getIntExtra(EXTRA_SELECTED_PEERS, 0);
                    if (selectedPeers == 0 && getConnectToPublicPeers(getContext()) && !getMulticast(getContext())) {
                        YggmailServiceCommand.stopYggmail(context);
                    } else if (YggmailObservable.getInstance().getStatus() == YggmailObservable.Status.Running) {
                        YggmailServiceCommand.stopYggmail(context);
                        YggmailServiceCommand.startYggmail(context);
                    }
                    break;
                case PeerManager.EVENT_LOADING_PEERS_FINISHED:
                    Snackbar finishedBar = Snackbar.make(root, R.string.loading_finished, Snackbar.LENGTH_SHORT);
                    finishedBar.show();
                    PreferenceHelper.setLastUpdate(context, System.currentTimeMillis());
                    break;
                default:
                    break;
            }
        }
    }
}