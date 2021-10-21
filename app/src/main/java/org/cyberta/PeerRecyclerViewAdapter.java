package org.cyberta;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.cyberta.databinding.PeerItemBinding;

import java.util.Observable;
import java.util.Observer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PeerRecyclerViewAdapter extends RecyclerView.Adapter<PeerRecyclerViewAdapter.ViewHolder> implements Observer {

    private final PeerManager peerManager;

    public PeerRecyclerViewAdapter(Context context) {
        peerManager = new PeerManager(context);
        peerManager.addObserver(this);
        peerManager.fetchPeers(context.getApplicationContext());
    }
    public PeerManager getPeerManager() {
        return peerManager;
    }

    @NonNull
    @Override
    public PeerRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeerRecyclerViewAdapter.ViewHolder(PeerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PeerRecyclerViewAdapter.ViewHolder holder, int position) {
        Peer peer = peerManager.peerList.get(position);
        holder.mItem = peer;
        holder.checkBox.setImageResource(peer.isSelected ?
                R.drawable.ic_check_circle_outline :
                R.drawable.ic_checkbox_blank_circle_outline);
        holder.addressView.setText(peer.address);
        holder.header.setVisibility(peer.showSectionHeader ? VISIBLE : GONE);
        holder.header.setText(peer.countryKey.replace(".md", ""));
    }

    @Override
    public int getItemCount() {
        return peerManager.peerList.size();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof PeerManager) {
            Util.runOnMain(this::notifyDataSetChanged);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final String TAG = ViewHolder.class.getSimpleName();
        public final TextView addressView;
        public final ImageView checkBox;
        public final TextView header;

        public Peer mItem;

        public ViewHolder(PeerItemBinding binding) {
            super(binding.getRoot());
            addressView = binding.addressView;
            checkBox = binding.itemCheckbox;
            header = binding.header;
            binding.peerItemContainer.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + addressView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick " + getLayoutPosition() + " " + mItem.address);
            peerManager.toggleSelected(mItem);
            try {
                ActionBar actionBar = ((AppCompatActivity) v.getContext()).
                        getSupportActionBar();
                if (peerManager.selectedPeers.size() > 0) {
                    actionBar.setTitle(v.getContext().getString(R.string.select_n_peers, peerManager.selectedPeers.size()));
                }  else {
                    actionBar.setTitle(v.getContext().getString(R.string.select_peers));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
