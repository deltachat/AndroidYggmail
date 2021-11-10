package chat.delta.androidyggmail.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import chat.delta.androidyggmail.R;
import chat.delta.androidyggmail.databinding.PeerItemBinding;


public class PeerListItemView extends LinearLayoutCompat {

    public PeerListItemView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PeerListItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PeerListItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = layoutInflater.inflate(R.layout.peer_item, this, true);
        PeerItemBinding.bind(rootView);
    }

}
