package chat.delta.androidyggmail;

import static chat.delta.androidyggmail.settings.PreferenceHelper.getAccountName;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Observable;
import java.util.Observer;

import chat.delta.androidyggmail.databinding.FragmentMainBinding;

public class MainFragment extends Fragment implements Observer {

    private static final String TAG = MainFragment.class.getSimpleName();
    private FragmentMainBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        YggmailOberservable.getInstance().addObserver(this);
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       updateUI();

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (YggmailOberservable.getInstance().getStatus()) {
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
    }

    private void updateUI() {
        switch (YggmailOberservable.getInstance().getStatus()) {
            case Error:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.restart);
                binding.title.setText(R.string.state_error);
                break;
            case Stopped:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.start);
                binding.title.setText(getAccountName(getContext()).isEmpty() ?
                        R.string.state_initial : R.string.state_off);
                break;
            case ShuttingDown:
                binding.buttonStart.setEnabled(false);
                break;
            case Running:
                binding.buttonStart.setEnabled(true);
                binding.buttonStart.setText(R.string.stop);
                binding.title.setText(R.string.state_running);
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
        YggmailOberservable.getInstance().deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof YggmailOberservable) {
            updateUI();
        }
    }
}