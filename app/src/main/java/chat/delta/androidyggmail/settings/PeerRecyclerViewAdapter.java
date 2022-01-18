package chat.delta.androidyggmail.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Observable;
import java.util.Observer;

import chat.delta.androidyggmail.Peer;
import chat.delta.androidyggmail.R;
import chat.delta.androidyggmail.Util;
import chat.delta.androidyggmail.databinding.PeerItemBinding;

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
        Peer peer = peerManager.getDisplayList().get(position);
        holder.mItem = peer;
        holder.checkBox.setImageResource(peer.isSelected ?
                R.drawable.ic_check_circle_outline :
                R.drawable.ic_checkbox_blank_circle_outline);
        holder.addressView.setText(peer.address);
        if (peer.showSectionHeader) {
            holder.header.setText(peer.countryKey.replace(".md", ""));
            int selectedCount = peerManager.getSelectedPeerCount(peer.countryKey);
            if (selectedCount > 0) {
                holder.headerCircleCount.setText(String.valueOf(selectedCount));
                holder.counterContainer.setVisibility(VISIBLE);
            } else {
                holder.counterContainer.setVisibility(GONE);
            }
        }
        holder.headerContainer.setVisibility(peer.showSectionHeader ? VISIBLE : GONE);
        holder.peerItemContainer.setVisibility(peer.showItem ? VISIBLE : GONE);
        holder.headerArrow.setImageResource(peer.showItem ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);

    }

    @Override
    public int getItemCount() {
        return peerManager.getDisplayList().size();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof PeerManager) {
            Util.runOnMain(() -> {
                notifyDataSetChanged();
                updateActionBar(peerManager.getContext());
            });
        }
    }

    void updateActionBar(Context context) {
        try {
            ActionBar actionBar = ((AppCompatActivity) (context)).getSupportActionBar();
            if (peerManager.selectedPeers.size() > 0) {
                actionBar.setTitle(context.getString(R.string.select_n_peers, peerManager.selectedPeers.size()));
            }  else {
                actionBar.setTitle(context.getString(R.string.select_peers));
            }
        } catch (NullPointerException | ClassCastException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final String TAG = ViewHolder.class.getSimpleName();
        public final AppCompatTextView addressView;
        public final ImageView checkBox;
        public final AppCompatTextView header;
        public final LinearLayoutCompat headerContainer;
        public final LinearLayoutCompat peerItemContainer;
        public final ImageView headerArrow;
        public final RelativeLayout counterContainer;
        public final AppCompatTextView headerCircleCount;

        public Peer mItem;

        public ViewHolder(PeerItemBinding binding) {
            super(binding.getRoot());
            addressView = binding.addressView;
            checkBox = binding.itemCheckbox;
            header = binding.header;
            headerContainer = binding.headerContainer;
            headerArrow = binding.headerArrow;
            peerItemContainer = binding.peerItemContainer;
            counterContainer = binding.counterContainer;
            headerCircleCount = binding.headerCircleCount;
            binding.peerItemContainer.setOnClickListener(this::onClickItemContainer);
            binding.headerContainer.setOnClickListener(this::onClickHeaderContainer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + addressView.getText() + "'";
        }


        public void onClickItemContainer(View v) {
            Log.d(TAG, "onClick " + getLayoutPosition() + " " + mItem.address);
            peerManager.toggleSelected(mItem);
            updateActionBar(v.getContext());
        }

        public void onClickHeaderContainer(View v) {
            Log.d(TAG, "onClick " + getLayoutPosition() + " " + mItem.address);
            peerManager.toggleHeader(mItem);
        }

    }
}
