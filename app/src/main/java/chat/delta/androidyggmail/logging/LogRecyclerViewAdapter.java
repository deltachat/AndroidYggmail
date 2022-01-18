package chat.delta.androidyggmail.logging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import chat.delta.androidyggmail.settings.PreferenceHelper;
import chat.delta.androidyggmail.databinding.LogItemBinding;

import java.util.Observable;
import java.util.Observer;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LogItem}.
 */
public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> implements Observer {

    private static final String TAG = LogRecyclerViewAdapter.class.getName();
    private LogObservable logObservable;
    private boolean showTimestamps;
    private boolean showLogTags;

    public LogRecyclerViewAdapter(Context context) {
        logObservable = LogObservable.getInstance();
        logObservable.addObserver(this);
        showTimestamps = PreferenceHelper.getShowTimestamps(context);
        showLogTags = PreferenceHelper.getShowLogTags(context);
    }

    public void setShowTimeStamps(boolean show) {
        if (show == this.showTimestamps) {
            return;
        }
        this.showTimestamps = show;
        notifyDataSetChanged();
    }

    public void setShowLogTags(boolean show) {
        if (show == this.showLogTags) {
            return;
        }
        this.showLogTags = show;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LogItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = logObservable.getLogs().get(position);
        holder.mContentView.setText(holder.mItem.toString(showTimestamps, showLogTags));
    }


    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        logObservable.deleteObserver(this);
    }

    @Override
    public int getItemCount() {
        return this.logObservable.getLogs().size();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof LogObservable) {
            this.logObservable = (LogObservable) o;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public LogItem mItem;

        public ViewHolder(LogItemBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}