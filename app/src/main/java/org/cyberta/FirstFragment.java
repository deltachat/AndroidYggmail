package org.cyberta;

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

import org.cyberta.databinding.FragmentFirstBinding;

import java.util.Observable;
import java.util.Observer;

public class FirstFragment extends Fragment implements Observer {

    private static final String TAG = FirstFragment.class.getSimpleName();
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        YggmailOberservable.getInstance().addObserver(this);
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       updateUI();

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (YggmailOberservable.getInstance().getStatus()) {
                    case Running:
                        YggmailServiceCommand.stopYggmail(FirstFragment.this.getContext());
                        break;
                    case Stopped:
                        binding.buttonFirst.setEnabled(false);
                        binding.buttonFirst.setText(R.string.stop);
                        YggmailServiceCommand.startYggmail(FirstFragment.this.getContext());
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
                binding.buttonFirst.setEnabled(true);
                binding.buttonFirst.setText(R.string.restart);
                break;
            case Stopped:
                binding.buttonFirst.setEnabled(true);
                binding.buttonFirst.setText(R.string.start);
                break;
            case ShuttingDown:
                binding.buttonFirst.setEnabled(false);
                break;
            case Running:
                binding.buttonFirst.setEnabled(true);
                binding.buttonFirst.setText(R.string.stop);
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
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_SecondFragment_to_LogFragment);
            return true;
        } else if (id == R.id.action_settings) {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_SecondFragment_to_SettingsFragment);
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