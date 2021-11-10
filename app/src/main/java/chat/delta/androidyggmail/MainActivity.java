package chat.delta.androidyggmail;

import static android.content.Intent.CATEGORY_DEFAULT;
import static chat.delta.androidyggmail.InstallHelper.sendDeltaChatInstallIntent;
import static chat.delta.androidyggmail.YggmailService.SERVICE_ACTION_INSTALL_DC;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import chat.delta.androidyggmail.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static String ACTION_SHOW_MAIN_FRAGMENT;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private final BroadcastReceiver yggmailServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SERVICE_ACTION_INSTALL_DC.equals(intent.getAction())) {
                AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.install_title)
                        .setMessage(R.string.install_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.install_deltachat, (dialog, which) -> sendDeltaChatInstallIntent(context.getApplicationContext()))
                        .setNegativeButton(R.string.no, null)
                        .create();
                d.show();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(CATEGORY_DEFAULT);
        intentFilter.addAction(SERVICE_ACTION_INSTALL_DC);
        LocalBroadcastManager.getInstance(this).registerReceiver(yggmailServiceReceiver, intentFilter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(yggmailServiceReceiver);
    }
}