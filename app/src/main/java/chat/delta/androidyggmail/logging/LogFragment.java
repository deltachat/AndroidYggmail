package chat.delta.androidyggmail.logging;

import static chat.delta.androidyggmail.logging.FileLogger.DEBUG_LOG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import chat.delta.androidyggmail.settings.PreferenceHelper;
import chat.delta.androidyggmail.R;
import chat.delta.androidyggmail.Util;
import chat.delta.androidyggmail.YggmailServiceCommand;

/**
 * A fragment representing a list of log entries.
 */
public class LogFragment extends Fragment {

    private static final String TAG = LogFragment.class.getName();

    private LogRecyclerViewAdapter recyclerViewAdapter;
    private boolean showTimestamps;
    private boolean showLogTags;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogFragment() {
    }

    @SuppressWarnings("unused")
    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            showTimestamps = PreferenceHelper.getShowTimestamps(context);
            showLogTags = PreferenceHelper.getShowLogTags(context);
            RecyclerView recyclerView = (RecyclerView) view;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            recyclerViewAdapter = new LogRecyclerViewAdapter(context);
            recyclerView.setAdapter(recyclerViewAdapter);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logs, menu);
        menu.findItem(R.id.action_show_timestamps).setTitle(showTimestamps ?
                R.string.hide_timestamps : R.string.show_timestamps);
        menu.findItem(R.id.action_show_log_tags).setTitle(showLogTags ?
                R.string.hide_log_tags : R.string.show_log_tags);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Context context = getContext();

        if (context == null) {
            return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_copy) {
            String logs = LogObservable.getInstance().getLogsAsString(showTimestamps, showLogTags);
            Util.writeTextToClipboard(context, logs);
            Toast.makeText(context.getApplicationContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_share) {
            File file = new File(context.getCacheDir() + DEBUG_LOG);
            Uri contentUri = FileProvider.getUriForFile(context, "chat.delta.androidyggmail.fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, "text/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_app_to_share)));
            }
        } else if (id == R.id.action_show_timestamps) {
            showTimestamps = !showTimestamps;
            recyclerViewAdapter.setShowTimeStamps(showTimestamps);
            PreferenceHelper.setShowTimestamps(getContext(), showTimestamps);
            item.setTitle(showTimestamps ? R.string.hide_timestamps : R.string.show_timestamps);
        } else if (id == R.id.action_show_log_tags) {
            showLogTags = !showLogTags;
            recyclerViewAdapter.setShowLogTags(showLogTags);
            PreferenceHelper.setShowLogTags(getContext(), showLogTags);
            item.setTitle(showLogTags ? R.string.hide_log_tags : R.string.show_log_tags);
        } else if (id == R.id.action_clear_log) {
            LogObservable.getInstance().clearLog();
            YggmailServiceCommand.clearLog(this.getContext());
        }
        return super.onOptionsItemSelected(item);
    }


}
