package org.cyberta;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.cyberta.databinding.FragmentLogBinding;
import org.cyberta.placeholder.LogListContent.LogListItem;

import java.util.Observable;
import java.util.Observer;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LogListItem}.
 */
public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> implements Observer {

    private static final String TAG = LogRecyclerViewAdapter.class.getName();
    private LogObservable logObservable;

    public LogRecyclerViewAdapter() {
        logObservable = LogObservable.getInstance();
        logObservable.addObserver(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = logObservable.getLogs().get(position);
        holder.mContentView.setText(holder.mItem.content);
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
            Log.d(TAG, "logs updated ->  notify data set changed");
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public LogListItem mItem;

        public ViewHolder(FragmentLogBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}