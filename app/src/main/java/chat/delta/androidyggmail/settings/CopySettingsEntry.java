package chat.delta.androidyggmail.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import chat.delta.androidyggmail.R;
import chat.delta.androidyggmail.Util;
import chat.delta.androidyggmail.databinding.CopySettingsEntryBinding;

public class CopySettingsEntry extends LinearLayoutCompat {

    private CopySettingsEntryBinding binding;
    public CopySettingsEntry(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public CopySettingsEntry(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CopySettingsEntry(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = layoutInflater.inflate(R.layout.copy_settings_entry, this, true);
        View view = rootView.findViewById(R.id.container);
        binding = CopySettingsEntryBinding.bind(view);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CopySettingsEntry);
            binding.copy.setVisibility(typedArray.getBoolean(R.styleable.CopySettingsEntry_showCopy, false) ? VISIBLE : INVISIBLE);
            binding.text.setText(typedArray.getString(R.styleable.CopySettingsEntry_text));
            binding.value.setText(typedArray.getString(R.styleable.CopySettingsEntry_value));
            typedArray.recycle();
        }
        binding.copy.setOnClickListener(v -> {
            String text = (String) binding.value.getText();
            Util.writeTextToClipboard(context, text);
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        });
    }

    public void setText(String text) {
        binding.text.setText(text);
    }

    public void setValue(String value) {
        binding.value.setText(value);
    }

    public void setShowCopy(boolean show) {
        binding.copy.setVisibility(show ? VISIBLE : INVISIBLE);
    }
}
